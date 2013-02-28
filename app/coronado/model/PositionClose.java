package coronado.model;

import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

@Entity
public class PositionClose extends Model {
    @Id
    private Long id;
    private double quantity;
    private double amount;
    private Date date;

    public PositionClose(final double quantity, final double amount, final Date date) {
        this.quantity = quantity;
        this.amount = amount;
        this.date = date;
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

    @Override
    public String toString() {
        return String.format("{ quantity: %1$s, amount: %2$s, date: %3$tm-%3$td-%3$tY }",
                getQuantity(), getAmount(), getDate());
    }

    public static Finder<Long,PositionClose> find = new Finder<Long,PositionClose>(
            Long.class, PositionClose.class
    );
};
