package coronado.api.model;

import java.util.Date;
import java.util.List;

public class AccountHoldingsResponse {
    private double costbasis;
    private double gainloss;
    private double marketvalue;
    private double qty;
    private String cusip;
    private String sym;
    private String sectyp;
    private String desc;

    @Override
    public String toString() {
        return String.format("{ cusip: %5$s, cost basis: %1$s, gain/loss: %2$s, market value: %3$s, quantity: %4$s }",
                getCostBasis(), getGainLoss(), getMarketValue(), getQuantity(), getCusip());
    }

    public double getCostBasis() {
        return costbasis;
    }

    public void setCostBasis(double costbasis) {
        this.costbasis = costbasis;
    }

    public double getGainLoss() {
        return gainloss;
    }

    public void setGainLoss(double gainloss) {
        this.gainloss = gainloss;
    }

    public double getMarketValue() {
        return marketvalue;
    }

    public void setMarketValue(double marketvalue) {
        this.marketvalue = marketvalue;
    }

    public double getQuantity() {
        return qty;
    }

    public void setQuantity(double qty) {
        this.qty = qty;
    }

    public String getCusip() {
        return cusip;
    }

    public void setCusip(String cusip) {
        this.cusip = cusip;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getSym() {
        return sym;
    }

    public void setSym(String sym) {
        this.sym = sym;
    }

    public String getSectyp() {
        return sectyp;
    }

    public void setSectyp(String sectyp) {
        this.sectyp = sectyp;
    }

    public static class ListOfAccountHoldings {
        private final List<AccountHoldingsResponse> holdings;

        public ListOfAccountHoldings(final List<AccountHoldingsResponse> holdings) {
            this.holdings = holdings;
        }

        public List<AccountHoldingsResponse> get() {
            return holdings;
        }
    }
}
