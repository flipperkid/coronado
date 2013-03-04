package coronado.model;

import coronado.model.api.AccountHistoryResponse;
import coronado.api.model.AccountHoldingsResponse;
import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Date;

@Entity
public class Position extends Model {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private double shares;
    private double costBasis;
    private double closeValue;
    private final Date openDate;
    private Date closeDate;
    private boolean closed;
    private final String symbol;
    private final String cusip;
    private final String description;
    private final String securityType;

    public Position(final double shares, final double costBasis, final Date openDate,
                    final String symbol, final String cusip, final String description, final String securityType) {
        this.shares = shares;
        this.costBasis = costBasis;
        this.closeValue = 0.0;
        this.openDate = openDate;
        this.closeDate = null;
        this.closed = false;
        this.symbol = symbol;
        this.cusip = cusip;
        this.description = description;
        this.securityType = securityType;
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
        return closed;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getCusip() {
        return cusip;
    }

    public String getDescription() {
        return description;
    }

    public String getSecurityType() {
        return securityType;
    }

    public void close(final double closeValue, final Date closeDate) {
        this.closeValue = closeValue;
        this.closeDate = closeDate;
        closed = true;
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
                    getSymbol(), getCusip(), getDescription(), getSecurityType());
            partialClose.close(partialCloseValue, transaction.getDate());
            return partialClose;
        }
        closeValue = partialCloseValue;
        closeDate = transaction.getDate();
        closed = true;
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
        closed = true;
        return partialQuantity;
    }

    @Override
    public String toString() {
        return String.format("{ id: %1$s, info: %7$s %8$s %9$s %10$s, cost basis: %2$s, close value: %3$s, " +
                "open date: %4$tm-%4$td-%4$tY, close date: %5$tm-%5$td-%5$tY, is closed: %6$s",
                getId(), getCostBasis(), getCloseValue(), getOpenDate(), getCloseDate(), isClosed(), getSymbol(),
                getSecurityType(), getDescription(), getCusip());
    }

    public static Finder<Long,Position> find = new Finder<Long,Position>(
            Long.class, Position.class
    );
};
