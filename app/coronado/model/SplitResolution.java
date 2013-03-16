package coronado.model;

import org.codehaus.jackson.annotate.JsonTypeName;

import javax.persistence.*;
import java.util.AbstractMap;
import java.util.Map;

@Entity
@DiscriminatorValue(value="S")
@JsonTypeName("S")
public class SplitResolution extends AbstractResolution {
    private final String parentCusip;
    private final double splitRatio;

    public SplitResolution(final String parentCusip, final double splitRatio) {
        this.parentCusip = parentCusip;
        this.splitRatio = splitRatio;
    }

    public String getParentCusip() {
        return parentCusip;
    }

    public double getSplitRatio() {
        return splitRatio;
    }

    public Map.Entry<Position,Double> resolve(final Position position, final String outputCusip) {
        if(position.getCusip().equals(parentCusip)) {
            final double startAmount = position.getShares();
            final Position spinoff = position.split(outputCusip, splitRatio);
            final double endAmount = position.getShares();
            return new AbstractMap.SimpleEntry<Position, Double>(spinoff, endAmount-startAmount);
        }
        return null;
    }
}
