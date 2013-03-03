package coronado.model;

import coronado.model.api.AccountHistoryResponse;
import coronado.api.model.AccountHoldingsResponse;
import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

@Entity
public class Position extends Model {
    @Id
    private Long id;
    private double shares;
    private double costBasis;
    private double closeValue;
    private final Date openDate;
    private Date closeDate;
    private boolean isClosed;
    private final String symbol;
    private final String cusip;
    private final String desc;
    private final String type;

    public Position(final double shares, final double costBasis, final Date openDate,
                    final String symbol, final String cusip, final String desc, final String type) {
        this.shares = shares;
        this.costBasis = costBasis;
        this.closeValue = 0.0;
        this.openDate = openDate;
        this.closeDate = null;
        this.isClosed = false;
        this.symbol = symbol;
        this.cusip = cusip;
        this.desc = desc;
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public double getShares() {
        return shares;
    }

    public double getCostBasis() {
        return costBasis;
    }

    public double getCloseValue() {
        return closeValue;
    }

    public Date getOpenDate() {
        return openDate;
    }

    public Date getCloseDate() {
        return closeDate;
    }

    public boolean isClosed() {
        return isClosed;
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

    public void close(final double closeValue, final Date closeDate) {
        this.closeValue = closeValue;
        this.closeDate = closeDate;
        isClosed = true;
    }

    public Position close(final AccountHistoryResponse transaction, final double remainingQuantity) {
        final double closeQuantity = Math.min(remainingQuantity, getShares());
        if(closeQuantity <= 0) {
            return null;
        }
        final double partialCloseValue = transaction.getAmount() * closeQuantity / (-1*transaction.getQuantity());
        if(closeQuantity < getShares()) {
            final double partialCostBasis = getCostBasis() * closeQuantity / getShares();
            costBasis -= partialCostBasis;
            shares -= closeQuantity;
            final Position partialClose = new Position(closeQuantity, partialCostBasis, getOpenDate(),
                    getSymbol(), getCusip(), getDesc(), getType());
            partialClose.close(partialCloseValue, transaction.getDate());
            return partialClose;
        }
        closeValue = partialCloseValue;
        closeDate = transaction.getDate();
        isClosed = true;
        return null;
    }

    public double rectifyHolding(AccountHoldingsResponse holding) {
        if(isClosed()) {
            return 0;
        }
        final double partialQuantity = Math.min(getShares(), holding.getQuantity());
        final double closeValue = holding.getMarketValue() * partialQuantity/holding.getQuantity();
        this.closeValue = closeValue;
        closeDate = new Date();
        isClosed = true;
        return partialQuantity;
    }

    @Override
    public String toString() {
        return String.format("{ id: %1$s, info: %7$s %8$s %9$s %10$s, cost basis: %2$s, close value: %3$s, " +
                "open date: %4$tm-%4$td-%4$tY, close date: %5$tm-%5$td-%5$tY, is closed: %6$s",
                getId(), getCostBasis(), getCloseValue(), getOpenDate(), getCloseDate(), isClosed(), getSymbol(),
                getType(), getDesc(), getCusip());
    }

    public static Finder<Long,Position> find = new Finder<Long,Position>(
            Long.class, Position.class
    );
};
