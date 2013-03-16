package coronado.model;

import org.joda.time.DateTime;
import play.db.ebean.Model;

import javax.persistence.*;
import java.util.Date;

@Entity
public class HistorySequence extends Model {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private final String symbol;
    private Date startDate;
    private Date endDate;

    public HistorySequence(final String symbol, final Date startDate, final Date endDate) {
        this.symbol = symbol;
        this.startDate = startDate;
        this.endDate = endDate;
        if(this.endDate == null) {
            this.endDate = new DateTime().toDateMidnight().toDate();
        }
    }

    @Override
    public String toString() {
        return String.format("{ id: %1$s, symbol: %2$s, start date: %3$tm-%3$td-%3$tY, end date: %4$tm-%4$td-%4$tY",
                id, getSymbol(), getStartDate(), getEndDate());
    }

    public static Finder<Long,HistorySequence> find = new Finder<Long,HistorySequence>(
            Long.class, HistorySequence.class
    );

    public String getSymbol() {
        return symbol;
    }

    public Date getStartDate() {
        return startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    /**
     * @return A new sequence if a split is necessary.
     */
    public HistorySequence constrain(final HistorySequence seq) {
        if(!seq.getSymbol().equals(getSymbol())) {
            return null;
        }
        if(seq.getStartDate().before(getEndDate()) &&
                (seq.getEndDate().after(getEndDate()) || seq.getEndDate().equals(getEndDate()))) {
            endDate = seq.getStartDate();
        }
        if((seq.getStartDate().before(getStartDate()) || seq.getStartDate().equals(getStartDate())) &&
                seq.getEndDate().after(getStartDate())) {
            startDate = seq.getEndDate();
        }
        if(this.getStartDate().before(seq.getStartDate()) && getEndDate().after(seq.getEndDate())) {
            HistorySequence splitSeq = new HistorySequence(getSymbol(), getStartDate(), seq.getStartDate());
            startDate = seq.getEndDate();
            return splitSeq;
        }
        return null;
    }

    public boolean isValid() {
        return this.getStartDate().before(this.getEndDate());
    }
}
