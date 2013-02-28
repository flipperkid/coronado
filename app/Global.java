import coronado.analysis.PositionAnalyzer;
import coronado.api.SecretKeys;
import coronado.api.TradeKingProxy;
import coronado.model.Position;
import play.*;
import play.libs.Akka;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

public class Global extends GlobalSettings {

    @Override
    public void onStart(Application app) {
        preload();
    }

    private void preload() {
       Akka.system().scheduler().scheduleOnce(Duration.create(50, TimeUnit.MILLISECONDS), new Runnable() {
            @Override
            public void run() {
                final TradeKingProxy tkp;
                try {
                    tkp = new TradeKingProxy(new SecretKeys());
                    PositionAnalyzer pa = new PositionAnalyzer(tkp);
                    pa.downloadHistory();
                } catch (Exception e) {
                    Logger.error("Unable to initialize connection to TradeKing.");
                }
            }
        }, Akka.system().dispatcher());
    }

}