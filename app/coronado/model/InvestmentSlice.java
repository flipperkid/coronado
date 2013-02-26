package coronado.model;

import play.db.ebean.Model;

import java.util.Date;

public class InvestmentSlice extends Model {
    private final double costBasis;
    private final double profitLoss;
    private final Date startDate;
    private final Date endDate;
    private final String symbol;
    private final String cusip;
    private final String desc;
    private final String type;

    public InvestmentSlice(final double costBasis, final double profitLoss, final Date startDate, final Date endDate,
                           final String symbol, final String cusip, final String desc, final String type) {
        this.costBasis = costBasis;
        this.profitLoss = profitLoss;
        this.startDate = startDate;
        this.endDate = endDate;
        this.symbol = symbol;
        this.cusip = cusip;
        this.desc = desc;
        this.type = type;
    }

    @Override
    public String toString() {
        return String.format("{ symbol: %1$s, profitLoss: %2$s, costBasis: %3$s, startDate: " +
                "%4$tm-%4$td-%4$tY, endDate: %5$tm-%5$td-%5$tY }",
                getSymbol(), getProfitLoss(), getCostBasis(), getStartDate(), getEndDate());
    }

    public double getCostBasis() {
        return costBasis;
    }

    public double getProfitLoss() {
        return profitLoss;
    }

    public Date getStartDate() {
        return startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getCusip() {
        return cusip;
    }

    public String getDesc() {
        return desc;
    }

    public String getType() {
        return type;
    }
}
