package coronado.model;

import com.google.common.collect.Lists;
import coronado.api.model.StockResponse;
import coronado.model.api.AccountHistoryResponse;
import coronado.api.model.AccountHoldingsResponse;
import org.codehaus.jackson.annotate.JsonIgnore;
import play.Logger;
import play.db.ebean.Model;

import javax.persistence.*;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

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
    private String symbol;
    private final String cusip;
    private String description;
    private String securityType;
    private List<QuoteHistory> quotes;


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

    public List<QuoteHistory> getQuotes() {
        return quotes;
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

    public void loadQuotes() {
        if("CS".equals(getSecurityType())) {
            quotes = Lists.newArrayList();
            quotes.addAll(QuoteHistory.find.where().eq("symbol", getSymbol())
                    .between("date", getOpenDate(), getCloseDate()).orderBy("date").findList());
        }
    }

    @JsonIgnore
    public String getQuoteSymbol() throws ParseException {
      if("CS".equals(getSecurityType())) {
          return this.getSymbol();
      } else if("OPT".equals(getSecurityType())) {
          final OptionContract optionContract = new OptionContract(this.getDescription());
          return optionContract.getOccSymbol();
      }
      throw new IllegalStateException("The security of this position is unrecognized");
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
        close(partialCloseValue, transaction.getDate());
        return null;
    }

    public double rectifyHolding(final AccountHoldingsResponse holding) {
        if(isClosed()) {
            return 0;
        }

        if(symbol == null) {
            Logger.info("Update symbol data: " + holding.getSym());
            if(description == null) {
                description = holding.getDesc();
            }
            if(securityType == null) {
                securityType = holding.getSectyp();
            }
            symbol = holding.getSym();
            this.update();
        }

        final double partialQuantity = Math.min(getShares(), holding.getQuantity());
        final double closeValue = holding.getMarketValue() * partialQuantity/holding.getQuantity();
        close(closeValue, new Date());
        return partialQuantity;
    }

    public void rectifyHolding(final StockResponse quote) {
        if(isClosed()) {
            return;
        }
        final double value = quote.getPrice().doubleValue();
        close(value*getShares(), new Date());
    }

    // TODO Issue will occur if the position has already been closed.
    public Position split(final String outputCusip, final double splitRatio) {
        if(outputCusip.equals(this.getCusip())) {
            shares *= splitRatio;
            return null;
        }
        final double newShares = this.getShares()*splitRatio - this.getShares();
        final double newCostBasis = this.getCostBasis() * newShares / (this.getShares()*splitRatio);
        costBasis -= newCostBasis;
        return new Position(newShares, newCostBasis, getOpenDate(), null, outputCusip, null, null);
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
