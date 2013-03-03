package coronado.analysis;

import com.google.common.collect.Lists;
import coronado.api.TradeKingProxy;
import coronado.model.api.AccountHistoryResponse;
import coronado.api.model.AccountHoldingsResponse;
import coronado.model.Position;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import play.Logger;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
            if("Trade".equals(transaction.getActivity()) || "Expired".equals(transaction.getActivity())) {
                handleTrade(transaction, positions);
            } else {
                //System.out.println(transaction);
            }
        }
        for(Position cPos : positions) {
            cPos.save();
        }
    }

    private void handleTrade(AccountHistoryResponse transaction, List<Position> positions) {
        final boolean processedTransaction = AccountHistoryResponse.find.where()
                .eq("date", transaction.getDate())
                .eq("amount", transaction.getAmount())
                .eq("cusip", transaction.getCusip())
                .eq("activity", transaction.getActivity())
                .findRowCount() > 0;
        if(processedTransaction) {
            return;
        }
        Logger.info("Handling new transaction: " + transaction);

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
                return;
            }
        }
        transaction.save();
    }

    private void rectifyHoldings(List<Position> positions) throws InterruptedException, OAuthExpectationFailedException,
            OAuthCommunicationException, OAuthMessageSignerException, IOException {
        final List<AccountHoldingsResponse> holdings = apiProxy.getHoldings();
        for(AccountHoldingsResponse holding : holdings) {
            rectifyHolding(holding, positions);
        }
        for(Position cPos : positions) {
            if(!cPos.isClosed()) {
                Logger.error("Position not closed or held: " + cPos.getDesc() + " " + cPos.getCusip());
            }
        }
    }

    private void rectifyHolding(AccountHoldingsResponse holding, List<Position> positions) {
        double remainingQuantity = holding.getQuantity();
        for(Position p : positions) {
            if(remainingQuantity > 0
                    && (p.getCusip().equals(holding.getCusip())
                        || ("?".equals(holding.getCusip()) && p.getDesc().equals(holding.getDesc())))) {
                remainingQuantity -= p.rectifyHolding(holding);
            }
        }
        if(remainingQuantity != 0) {
            Logger.error("Unmatched holdings: " + remainingQuantity + " " + holding.getDesc() + " "
                    + holding.getCusip());
        }
    }

    private List<Position> loadHistory() {
        return Position.find.all();
    }
}
