package controllers;

import coronado.analysis.PositionAnalyzer;
import coronado.api.SecretKeys;
import coronado.api.TradeKingProxy;
import coronado.model.Bookkeeping;
import coronado.model.PositionTag;
import coronado.model.SplitResolution;
import org.codehaus.jackson.JsonNode;
import play.Logger;
import play.mvc.*;
import play.libs.Json;

public class BookkeepingController extends Controller {
  
    public static Result index() {
        return ok(Json.toJson(Bookkeeping.find.all()));
    }

    public static Result create() {
        return forbidden();
    }

    public static Result read(final long id) {
        return ok(Json.toJson(Bookkeeping.find.byId(id)));
    }

    public static Result update(final long id) throws Exception {
        JsonNode json = request().body().asJson();
        Bookkeeping record = Json.fromJson(json, Bookkeeping.class);
        PositionAnalyzer analyzer = new PositionAnalyzer(new TradeKingProxy(new SecretKeys()));
        if(analyzer.resolveBookkeeping(record)) {
            record.update();
            return ok(Json.toJson(record));
        }
        return badRequest();
    }

    public static Result delete(final long id) {
        Bookkeeping record = Bookkeeping.find.byId(id);
        record.delete();
        return noContent();
    }
}
