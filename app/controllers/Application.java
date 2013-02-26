package controllers;

import coronado.analysis.PositionAnalyzer;
import coronado.api.SecretKeys;
import coronado.api.TradeKingProxy;
import play.*;
import play.mvc.*;
import play.libs.Json;

import views.html.*;

public class Application extends Controller {
  
    public static Result index() {
        return ok(index.render("Portfolio Analysis"));
    }

    public static Result transactions() throws Exception {
        final TradeKingProxy tkp = new TradeKingProxy(new SecretKeys());
        return ok(Json.toJson(tkp.getHistory()));
    }

    public static Result investmentSlices() throws Exception {
        final TradeKingProxy tkp = new TradeKingProxy(new SecretKeys());
        PositionAnalyzer pa = new PositionAnalyzer(tkp);
        return ok(Json.toJson(pa.getInvestmentSlices()));
    }
}
