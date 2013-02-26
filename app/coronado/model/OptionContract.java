package coronado.model;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Date;

import coronado.api.model.OptionContractResponse;

import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class OptionContract extends Model {
    public enum OptionType {
        PUT('P'),
        CALL('C');

        private final char identifier;

        private OptionType(final char identifier) {
            this.identifier = identifier;
        }

        public char getIdentifier() {
            return identifier;
        }
    }

    @Id
    private Long id;
    private int contractSize;
    private int openInterest;
    private String exchange;
    private final String underlyingSymbol;
    private final Date expirationDate;
    private final BigDecimal strikePrice;
    private final OptionType optionType;

    public OptionContract(final String underlyingSymbol,
                          final Date expirationDate, final BigDecimal strikePrice, final OptionType optionType) {
        this.underlyingSymbol = underlyingSymbol;
        this.expirationDate = expirationDate;
        this.strikePrice = strikePrice;
        this.optionType = optionType;
    }

    public void setFromApi(final OptionContractResponse response) {
        contractSize = response.getContractSize();
        exchange = response.getExchange();
        openInterest = response.getOpeninterest();
    }

    public Long getId() {
        return id;
    }

    public String getOccSymbol() {
        DecimalFormat nf = new DecimalFormat("00000000");
        String priceStr = nf.format(getStrikePrice().multiply(new BigDecimal(1000)));
        return String.format("%1$s%2$ty%2$tm%2$td%3$c%4$s", getUnderlyingSymbol(),
                expirationDate, getOptionType().getIdentifier(), priceStr);
    }

    public String getExpirationDateTkFormat() {
        return String.format("%1$tY%1$tm%1$td", expirationDate);
    }

    public int getContractSize() {
        return contractSize;
    }

    public int getOpenInterest() {
        return openInterest;
    }

    public String getExchange() {
        return exchange;
    }

    public String getUnderlyingSymbol() {
        return underlyingSymbol;
    }

    public BigDecimal getStrikePrice() {
        return strikePrice;
    }

    public OptionType getOptionType() {
        return optionType;
    }

    public static Finder<Long,OptionContract> find = new Finder<Long,OptionContract>(
            Long.class, OptionContract.class
    );
};
