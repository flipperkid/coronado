package coronado.analysis;

import com.google.common.collect.Lists;
import coronado.api.TradeKingProxy;
import coronado.model.api.AccountHistoryResponse;
import coronado.api.model.AccountHoldingsResponse;
import coronado.model.InvestmentSlice;
import coronado.model.Position;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PositionAnalyzer {
    final private TradeKingProxy apiProxy;

    public PositionAnalyzer(final TradeKingProxy apiProxy) {
        this.apiProxy = apiProxy;
    }

    public List<InvestmentSlice> getInvestmentSlices() throws Exception {
        List<InvestmentSlice> slices = Lists.newArrayList();
        List<Position> positions = this.loadHistory();
        System.out.println(positions);
        this.rectifyHoldings(positions);
        for (Position cPos : positions) {
            slices.addAll(cPos.getSlices());
        }
        return slices;
    }

    public void downloadHistory() throws Exception {
        List<AccountHistoryResponse> accountHistories = apiProxy.getHistory();
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
        List<Position> positions = Lists.newArrayList();
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

    private List<Position> loadHistory() {
        return Position.find.all();
    }

    private void handleTrade(AccountHistoryResponse transaction, List<Position> positions) {
        boolean processedTransaction = AccountHistoryResponse.find.where()
                .eq("date", transaction.getDate())
                .eq("amount", transaction.getAmount())
                .eq("cusip", transaction.getCusip())
                .eq("activity", transaction.getActivity())
                .findRowCount() > 0;
        if(processedTransaction) {
            return;
        }
        transaction.save();

        if(transaction.getQuantity() > 0) {
            positions.add(new Position(transaction.getQuantity(), transaction.getAmount(), transaction.getDate(),
                    transaction.getSymbol(), transaction.getCusip(), transaction.getDesc(), transaction.getType()));
        } else {
            double remainingQuantity = -1*transaction.getQuantity();
            List<Position> revPos = Lists.reverse(positions);
            for(Position p : revPos) {
                if(remainingQuantity > 0 && p.getCusip().equals(transaction.getCusip())) {
                    remainingQuantity -= p.close(transaction, remainingQuantity);
                }
            }
            if(remainingQuantity > 0) {
                System.out.println("Warning too much sold: " + remainingQuantity + " " + transaction.getCusip());
            }
        }
    }

    private void rectifyHoldings(List<Position> positions) throws InterruptedException, OAuthExpectationFailedException,
            OAuthCommunicationException, OAuthMessageSignerException, IOException {
        List<AccountHoldingsResponse> holdings = apiProxy.getHoldings();
        for(AccountHoldingsResponse holding : holdings) {
            rectifyHolding(holding, positions);
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
            System.out.println("Warning unmatched holdings: " + remainingQuantity + " " + holding.getCusip());
        }
    }
}
