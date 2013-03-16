package coronado.model;

import coronado.model.api.AccountHistoryResponse;
import play.db.ebean.Model;

import javax.persistence.*;
import java.util.Date;

@Entity
public class Bookkeeping extends Model {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private final Date date;
    private final String symbol;
    private final String cusip;
    private final String description;
    private final double amount;
    private final double quantity;

    @OneToOne(cascade = CascadeType.ALL)
    private AbstractResolution resolution;

    public Bookkeeping(final AccountHistoryResponse transaction) {
        date = transaction.getDate();
        symbol = transaction.getSymbol();
        cusip = transaction.getCusip();
        description = transaction.getDesc();
        amount = transaction.getAmount();
        quantity = transaction.getQuantity();
        resolution = new Unresolved();
    }

    public Long getId() {
        return id;
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

    public String getDescription() {
        return description;
    }

    public double getAmount() {
        return amount;
    }

    public double getQuantity() {
        return quantity;
    }

    public AbstractResolution getResolution() {
        return resolution;
    }

    public static Finder<Long,Bookkeeping> find = new Finder<Long,Bookkeeping>(
            Long.class, Bookkeeping.class
    );
}
