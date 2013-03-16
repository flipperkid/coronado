package coronado.model;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import play.db.ebean.Model;

import javax.persistence.*;
import java.util.Map;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=JsonTypeInfo.As.PROPERTY, property="type")
@JsonSubTypes({
        @JsonSubTypes.Type(value=Unresolved.class, name="U"),
        @JsonSubTypes.Type(value=SplitResolution.class, name="S")
})
public abstract class AbstractResolution  extends Model {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    public Long getId() {
        return id;
    }

    public Map.Entry<Position,Double> resolve(final Position position, final String outputCusip) {
        throw new UnsupportedOperationException("No resolution implemented for this bookkeeping");
    }
}
