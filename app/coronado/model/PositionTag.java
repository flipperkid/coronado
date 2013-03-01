package coronado.model;

import play.db.ebean.Model;

import java.util.Date;

public class PositionTag extends Model {
    private final String tag;

    public PositionTag(final String tag) {
        this.tag = tag;
    }
}
