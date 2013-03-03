package coronado.model;

import com.google.common.collect.Sets;
import play.db.ebean.Model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import java.util.Set;

@Entity
public class PositionTag extends Model {
    @Id
    private Long id;
    private final String tag;

    @ManyToMany(cascade = CascadeType.ALL)
    private final Set<Position> positions;

    public PositionTag(final String tag) {
        this.tag = tag;
        positions = Sets.newHashSet();
    }

    public String getTag() {
        return tag;
    }

    public Set<Position> getPositions() {
        return positions;
    }

    public static Finder<Long,PositionTag> find = new Finder<Long,PositionTag>(
            Long.class, PositionTag.class
    );
}
