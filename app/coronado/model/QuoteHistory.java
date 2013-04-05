package coronado.model;

import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Date;
import java.util.List;

@Entity
public class QuoteHistory extends Model {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private double high;
    private double low;
    private double open;
    private double close;
    private String symbol;
    private Date date;

    @Override
    public String toString() {
        return String.format(
            "{ symbol: %1$s, date: %2$tm-%2$td-%2$tY, high: %3$.2f, low: %4$.2f, open: %5$.2f, close: %6$.2f }",
                getSymbol(), getDate(), getHigh(), getLow(), getOpen(), getClose());
    }

    public double getHigh() {
        return high;
    }

    public double getLow() {
        return low;
    }

    public double getOpen() {
        return open;
    }

    public double getClose() {
        return close;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(final String symbol) {
        this.symbol = symbol;
    }

    public Date getDate() {
        return date;
    }

    public void split(final double splitRatio) {
        open /= splitRatio;
        close /= splitRatio;
        high /= splitRatio;
        low /= splitRatio;
    }

    public static Finder<Long,QuoteHistory> find = new Finder<Long,QuoteHistory>(
            Long.class, QuoteHistory.class
    );

    public static class ListOfQuoteHistory {
        private final List<QuoteHistory> history;

        public ListOfQuoteHistory(final List<QuoteHistory> history) {
            this.history = history;
        }

        public List<QuoteHistory> get() {
            return history;
        }
    }
}
