package coronado.analysis;

import com.google.common.collect.Lists;
import coronado.api.TradeKingProxy;
import coronado.api.model.StockResponse;
import coronado.model.Bookkeeping;
import coronado.model.api.AccountHistoryResponse;
import coronado.api.model.AccountHoldingsResponse;
import coronado.model.Position;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import play.Logger;

import java.io.IOException;
import java.util.*;

public class PositionAnalyzer {
    final private TradeKingProxy apiProxy;

    public PositionAnalyzer(final TradeKingProxy apiProxy) {
        this.apiProxy = apiProxy;
    }

    public List<Position> getPositions() throws Exception {
        final List<Position> positions = this.loadHistory();
        this.rectifyHoldings(positions);
        return positions;
    }

    public void downloadHistory() throws Exception {
        final List<AccountHistoryResponse> accountHistories = apiProxy.getHistory();
        Collections.sort(accountHistories, new Comparator<AccountHistoryResponse>() {
            public int compare(AccountHistoryResponse o1, AccountHistoryResponse o2) {
                if (o1.getDate().equals(o2.getDate())) {
                    if(o1.getQuantity() == o2.getQuantity()) return 0;
                    if(o1.getQuantity() < o2.getQuantity()) return 1;
                    return -1;
                }
                return o1.getDate().compareTo(o2.getDate());
            }
        });
        final List<Position> positions = Lists.newArrayList();
        for(AccountHistoryResponse transaction : accountHistories) {
            final boolean processedTransaction = AccountHistoryResponse.find.where()
                    .eq("date", transaction.getDate())
                    .eq("amount", transaction.getAmount())
                    .eq("cusip", transaction.getCusip())
                    .eq("activity", transaction.getActivity())
                    .findRowCount() > 0;
            if(processedTransaction) {
                continue;
            }

            boolean handled = false;
            if("Trade".equals(transaction.getActivity()) || "Expired".equals(transaction.getActivity())) {
                handled = handleTrade(transaction, positions);
            } else if("Bookkeeping".equals(transaction.getActivity()) && transaction.getQuantity() > 0) {
                handled = handleBookkeeping(transaction);
            } else if("Dividend".equals(transaction.getActivity())) {
                //                System.out.println(transaction);
            }
            if(handled) {
                transaction.save();
            }
        }
        for(Position cPos : positions) {
            cPos.save();
        }
    }

    private boolean handleTrade(AccountHistoryResponse transaction, List<Position> positions) {
        Logger.info("Handling new trade: " + transaction);
        if(transaction.getQuantity() > 0) {
            positions.add(new Position(transaction.getQuantity(), -1*transaction.getAmount(), transaction.getDate(),
                    transaction.getSymbol(), transaction.getCusip(), transaction.getDesc(), transaction.getType()));
        } else {
            final List<Position> newPositions = Lists.newArrayList();
            double remainingQuantity = -1*transaction.getQuantity();
            final List<Position> revPos = Lists.reverse(positions);
            for(Position p : revPos) {
                if(remainingQuantity > 0 && p.getCusip().equals(transaction.getCusip())) {
                    if(!p.isClosed()) {
                        Position closedPartialPosition = p.close(transaction, remainingQuantity);
                        if(closedPartialPosition != null) {
                            remainingQuantity -= closedPartialPosition.getShares();
                            newPositions.add(closedPartialPosition);
                        } else {
                            remainingQuantity -= p.getShares();
                        }
                    }
                }
            }
            positions.addAll(newPositions);
            if(remainingQuantity > 0) {
                Logger.error("Too much sold: " + remainingQuantity + " " + transaction.getDesc() + " "
                        + transaction.getCusip());
                /*
                 * NOTE Don't save if this happens because saving will squash the error (would be easy to miss)
                 * In this case it will constantly re-occur.
                 */
                return false;
            }
        }
        return true;
    }

    private boolean handleBookkeeping(AccountHistoryResponse transaction) {
        Logger.info("Handling new bookkeeping: " + transaction);
        final Bookkeeping newBookkeeping = new Bookkeeping(transaction);
        newBookkeeping.save();
        return true;
    }

    public boolean resolveBookkeeping(Bookkeeping bookkeeping) {
        double newSharesRemaining = bookkeeping.getQuantity();
        final Date date = bookkeeping.getDate();
        final String cusip = bookkeeping.getCusip();
        final List<Position> positions = this.loadHistory();
        final List<Position> alteredPositions = Lists.newArrayList();
        for(Position cPos : positions) {
            if(cPos.getOpenDate().before(date) && (!cPos.isClosed() || cPos.getCloseDate().after(date))) {
                Map.Entry<Position, Double> result = bookkeeping.getResolution().resolve(cPos, cusip);
                if(result != null) {
                    final Position newPosition = result.getKey();
                    final double shareChange = result.getValue();
                    if(newPosition != null && shareChange != 0) {
                        Logger.error(
                                "Resolution unexpectedly created a new position and altered and existing share count");
                        return false;
                    } else if(newPosition != null) {
                        alteredPositions.add(newPosition);
                        alteredPositions.add(cPos);
                        newSharesRemaining -= newPosition.getShares();
                    } else if(result.getValue() != 0) {
                        alteredPositions.add(cPos);
                        newSharesRemaining -= shareChange;
                    }
                }
            }
        }

        if(newSharesRemaining != 0) {
            Logger.error("Bookkeeping invalid: " + cusip + " " + bookkeeping.getDescription() + " amount off "
                + newSharesRemaining + " " + bookkeeping.getResolution());
            return false;
        }
        for(Position cPos : alteredPositions) {
            cPos.save();
        }
        return true;
    }

    private void rectifyHoldings(List<Position> positions) throws InterruptedException, OAuthExpectationFailedException,
            OAuthCommunicationException, OAuthMessageSignerException, IOException {
        final List<AccountHoldingsResponse> holdings = apiProxy.getHoldings();
        for(AccountHoldingsResponse holding : holdings) {
            rectifyHolding(holding, positions);
        }
        for(Position cPos : positions) {
            if(!cPos.isClosed()) {
                Logger.warn("Position not closed or held: " + cPos.getDescription() + " " + cPos.getCusip());
                List<StockResponse> currStockValue = apiProxy.getStockPrice(Lists.newArrayList(cPos.getSymbol()));
                if(currStockValue.size() != 1) {
                    Logger.error("Unable to get the current value!");
                    cPos.close(cPos.getCostBasis(), new Date());
                } else {
                    Logger.info("Resolving based on current stock price.");
                    rectifyHolding(currStockValue.get(0), cPos);
                }
            }
        }
    }

    private void rectifyHolding(AccountHoldingsResponse holding, List<Position> positions) {
        double remainingQuantity = holding.getQuantity();
        for(Position p : positions) {
            if(remainingQuantity > 0
                    && (p.getCusip().equals(holding.getCusip())
                        || ("?".equals(holding.getCusip()) && p.getDescription().equals(holding.getDesc())))) {
                remainingQuantity -= p.rectifyHolding(holding);
            }
        }
        if(remainingQuantity != 0) {
            Logger.error("Unmatched holdings: " + remainingQuantity + " " + holding.getDesc() + " "
                    + holding.getCusip());
        }
    }

    private void rectifyHolding(StockResponse value, Position position) {
        position.rectifyHolding(value);
    }

    private List<Position> loadHistory() {
        return Position.find.all();
    }
}
