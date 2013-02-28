package coronado.model;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import coronado.model.api.AccountHistoryResponse;
import coronado.api.model.AccountHoldingsResponse;
import play.db.ebean.Model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.Date;
import java.util.List;

@Entity
public class Position extends Model {
    @Id
    private Long id;
    private double quantity;
    private double openQuantity;
    private double amount;
    private double profitLoss;
    private Date date;
    private String symbol;
    private String cusip;
    private String desc;
    private String type;

    @OneToMany(cascade = CascadeType.ALL)
    private final List<PositionClose> closes;

    public Position(final double quantity, final double amount, final Date date, final String symbol,
                    final String cusip, final String desc, final String type) {
        this.quantity = quantity;
        this.openQuantity = quantity;
        this.amount = amount;
        this.setProfitLoss(amount);
        this.date = date;
        this.symbol = symbol;
        this.cusip = cusip;
        this.setDesc(desc);
        this.setType(type);
        closes = Lists.newArrayList();
    }

    public Long getId() {
        return id;
    }

    public double getQuantity() {
        return quantity;
    }

    public double getAmount() {
        return amount;
    }

    public Date getDate() {
        return date;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getCusip() {
        return cusip;
    }

    public double getOpenQuantity() {
        return openQuantity;
    }

    public void setOpenQuantity(double openQuantity) {
        this.openQuantity = openQuantity;
    }

    public double getProfitLoss() {
        return profitLoss;
    }

    public void setProfitLoss(double profitLoss) {
        this.profitLoss = profitLoss;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<InvestmentSlice> getSlices() {
        double remainingProfitLoss = this.getProfitLoss();
        double remainingCostBasis = -1 * this.getAmount();
        List<InvestmentSlice> slices = Lists.newArrayList();
        for (PositionClose close : closes) {
            double costBasis = -1 * this.getAmount()*close.getQuantity()/this.getQuantity();
            remainingProfitLoss -= (close.getAmount() - costBasis);
            remainingCostBasis -= costBasis;
            slices.add(new InvestmentSlice(costBasis, close.getAmount() - costBasis,
                    this.getDate(), close.getDate(), this.getSymbol(), this.getCusip(), this.getDesc(), this.getType()));
        }
        if(this.getOpenQuantity() > 0) {
            slices.add(new InvestmentSlice(remainingCostBasis, remainingProfitLoss, this.getDate(), new Date(),
                    this.getSymbol(), this.getCusip(), this.getDesc(), this.getType()));
        }
        return slices;
    }

    public double close(final AccountHistoryResponse transaction, final double remainingQuantity) {
        final double closeQuantity = Math.min(remainingQuantity, getOpenQuantity());
        if(closeQuantity > 0) {
            double closeAmount = transaction.getAmount();
            if(closeQuantity < -1 * transaction.getQuantity()) {
                closeAmount = -1 * transaction.getAmount() * closeQuantity / transaction.getQuantity();
            }
            setOpenQuantity(getOpenQuantity() - closeQuantity);
            setProfitLoss(profitLoss + closeAmount);
            closes.add(new PositionClose(closeQuantity, closeAmount, transaction.getDate()));
        }
        return closeQuantity;
    }

    public double rectifyHolding(AccountHoldingsResponse holding) {
        final double rectifyQuantity = Math.min(getOpenQuantity(), holding.getQuantity());
        double rectifyValue = holding.getMarketValue() * rectifyQuantity/holding.getQuantity();
        setProfitLoss(profitLoss + rectifyValue);
        return rectifyQuantity;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(String.format("{ quantity: %1$s, amount: %2$s, date: %3$tm-%3$td-%3$tY, " +
                "symbol: %4$s, cusip: %5$s,",
                getQuantity(), getAmount(), getDate(), getSymbol(), getCusip()));
        if(!closes.isEmpty()) {
            sb.append(" closes:[\n\t");
            Joiner joiner = Joiner.on(",\n\t");
            sb.append(joiner.join(closes));
            sb.append("\n], ");
        } else {
            sb.append("\n");
        }
        sb.append(String.format("open shares: %1s, profit/loss: %2s }", getOpenQuantity(), getProfitLoss()));
        return sb.toString();
    }

    public static Finder<Long,Position> find = new Finder<Long,Position>(
            Long.class, Position.class
    );
};
