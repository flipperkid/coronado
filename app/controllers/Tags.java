package controllers;

import coronado.model.PositionTag;
import org.codehaus.jackson.JsonNode;
import play.mvc.*;
import play.libs.Json;

public class Tags extends Controller {
  
    public static Result index() {
        return ok(Json.toJson(PositionTag.find.all()));
    }

    @BodyParser.Of(BodyParser.Json.class)
    public static Result create() {
        JsonNode json = request().body().asJson();
        PositionTag newTag = Json.fromJson(json, PositionTag.class);
        newTag.save();
        return created(Json.toJson(newTag));
    }

    public static Result read(final long id) {
        return ok(Json.toJson(PositionTag.find.byId(id)));
    }

    public static Result update(final long id) {
        JsonNode json = request().body().asJson();
        PositionTag newTag = Json.fromJson(json, PositionTag.class);
        newTag.update();
        return ok(Json.toJson(newTag));
    }

    public static Result delete(final long id) {
        PositionTag tag = PositionTag.find.byId(id);
        tag.delete();
        return noContent();
    }
}
