package coronado.analysis;

import com.google.common.collect.Lists;
import coronado.api.TradeKingProxy;
import coronado.api.model.AccountHistoryResponse;
import coronado.api.model.AccountHoldingsResponse;
import coronado.model.InvestmentSlice;
import coronado.model.Position;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PositionAnalyzer {
    final private List<Position> positions;
    final private TradeKingProxy apiProxy;

    public PositionAnalyzer(final TradeKingProxy apiProxy) {
        this.apiProxy = apiProxy;
        positions = Lists.newArrayList();
    }

    public List<InvestmentSlice> getInvestmentSlices() throws Exception {
        List<InvestmentSlice> slices = Lists.newArrayList();
        List<Position> positions = this.getHistory();
        for (Position cPos : positions) {
            slices.addAll(cPos.getSlices());
        }
        return slices;
    }

    public List<Position> getHistory() throws Exception {
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
        for(AccountHistoryResponse transaction : accountHistories) {
            if("Trade".equals(transaction.getActivity()) || "Expired".equals(transaction.getActivity())) {
                handleTrade(transaction);
            } else {
                System.out.println(transaction);
            }
        }
        List<AccountHoldingsResponse> holdings = apiProxy.getHoldings();
        for(AccountHoldingsResponse holding : holdings) {
            rectifyHolding(holding);
        }

        return positions;
    }

    private void handleTrade(AccountHistoryResponse transaction) {
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

    private void rectifyHolding(AccountHoldingsResponse holding) {
        double remainingQuantity = holding.getQuantity();
        for(Position p : positions) {
            if(remainingQuantity > 0 && p.getCusip().equals(holding.getCusip())) {
                remainingQuantity -= p.rectifyHolding(holding);
            }
        }
        if(remainingQuantity != 0) {
            System.out.println("Warning unmatched holdings: " + remainingQuantity + " " + holding.getCusip());
        }
    }
}
