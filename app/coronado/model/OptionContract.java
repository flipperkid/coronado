package coronado.model;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import coronado.api.model.OptionContractResponse;

import play.db.ebean.Model;

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

    public OptionContract(final String description) throws ParseException {
        String[] tokens = description.split("[ ]+");
        if(tokens.length != 6) {
            throw new IllegalArgumentException("Malformed option description: " + description);
        }
        this.underlyingSymbol = tokens[0];
        SimpleDateFormat formatter = new SimpleDateFormat("MMM d yyyy");
        this.expirationDate = formatter.parse(tokens[1] + " " + tokens[2] + " " + tokens[3]);
        this.strikePrice = new BigDecimal(tokens[4]);
        this.optionType = OptionType.valueOf(tokens[5].toUpperCase());
    }

    public void setFromApi(final OptionContractResponse response) {
        contractSize = response.getContractSize();
        exchange = response.getExchange();
        openInterest = response.getOpeninterest();
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
};
