package coronado.model;

import com.google.common.collect.Sets;
import play.db.ebean.Model;

import javax.persistence.*;
import java.util.Set;

@Entity
public class PositionTag extends Model {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private final String tag;

    @ManyToMany(cascade = CascadeType.REMOVE)
    private final Set<Position> positions;

    public PositionTag(final String tag) {
        this.tag = tag;
        positions = Sets.newHashSet();
    }

    public Long getId() {
        return id;
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
