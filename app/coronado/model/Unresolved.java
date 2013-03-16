package coronado.model;

import org.codehaus.jackson.annotate.JsonTypeName;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value="U")
@JsonTypeName("U")
public class Unresolved extends AbstractResolution {

}
