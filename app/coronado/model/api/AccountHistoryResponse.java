package coronado.model.api;

import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.util.Date;
import java.util.List;

@Entity
public class AccountHistoryResponse extends Model {
    @Id
    private Long id;
    private String activity;
    private double amount;
    private Date date;
    private String cusip;
    @Transient
    private String desc;
    @Transient
    private String symbol;
    @Transient
    private String type;
    @Transient
    private double quantity;

    public String getActivity() {
        return activity;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getCusip() {
        return cusip;
    }

    public void setCusip(String cusip) {
        this.cusip = cusip;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return String.format("{ activity: %1$s, amount: %2$s, date: %3$tm-%3$td-%3$tY, symbol: %4$s, cusip: %5$s, quantity: %6$s,\n%7$s }",
                getActivity(), getAmount(), getDate(), getSymbol(), getCusip(), getQuantity(), getDesc());
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public static Model.Finder<Long,AccountHistoryResponse> find = new Model.Finder<Long,AccountHistoryResponse>(
            Long.class, AccountHistoryResponse.class
    );

    public static class ListOfAccountHistories {
        private final List<AccountHistoryResponse> transactions;

        public ListOfAccountHistories(final List<AccountHistoryResponse> transactions) {
            this.transactions = transactions;
        }

        public List<AccountHistoryResponse> get() {
            return transactions;
        }
    }
}
