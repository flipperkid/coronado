package coronado.analysis;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import coronado.api.TradeKingProxy;
import coronado.api.model.AccountHoldingsResponse;
import coronado.api.model.StockResponse;
import coronado.model.Bookkeeping;
import coronado.model.HistorySequence;
import coronado.model.Position;
import coronado.model.QuoteHistory;
import coronado.model.api.AccountHistoryResponse;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import play.Logger;

import java.io.IOException;
import java.text.ParseException;
import java.util.*;

public class HistoryLoader {
    final private TradeKingProxy apiProxy;

    public HistoryLoader(final TradeKingProxy apiProxy) {
        this.apiProxy = apiProxy;
    }

    public void downloadHistory() {
        final List<Position> positions = Position.find.all();
        final List<HistorySequence> allHistorySeqs = HistorySequence.find.all();
        for (Position cPos : positions) {
            if("OPT".equals(cPos.getSecurityType())) {
                continue;
            }
            try {
                final String symbol = cPos.getQuoteSymbol();
                Queue<HistorySequence> sequencesToProcess = Queues.newArrayDeque();
                sequencesToProcess.offer(new HistorySequence(symbol, cPos.getOpenDate(), cPos.getCloseDate()));
                while(!sequencesToProcess.isEmpty()) {
                    final HistorySequence newSeq = sequencesToProcess.poll();
                    for(HistorySequence existingSeq : allHistorySeqs) {
                        final HistorySequence splitSeq = newSeq.constrain(existingSeq);
                        if(splitSeq != null) {
                            sequencesToProcess.add(splitSeq);
                        }
                    }
                    if(newSeq.isValid()) {
                        Logger.info("Downloading quote history sequence: " + newSeq);
                        final List<QuoteHistory> quotes = apiProxy.getHistoricData(newSeq);
                        for (QuoteHistory quote : quotes) {
                            quote.save();
                        }
                        newSeq.save();
                        allHistorySeqs.add(newSeq);
                    }
                }
            } catch (ParseException e) {
                Logger.error("Issue parsing expiration date " + cPos.getDescription() + " " + cPos.getCusip());
                e.printStackTrace();
            } catch (InterruptedException e) {
                Logger.error("Issue connecting to TradeKing " + cPos.getDescription() + " " + cPos.getCusip());
                e.printStackTrace();
            } catch (OAuthExpectationFailedException e) {
                Logger.error("Issue connecting to TradeKing " + cPos.getDescription() + " " + cPos.getCusip());
                e.printStackTrace();
            } catch (OAuthCommunicationException e) {
                Logger.error("Issue connecting to TradeKing " + cPos.getDescription() + " " + cPos.getCusip());
                e.printStackTrace();
            } catch (OAuthMessageSignerException e) {
                Logger.error("Issue connecting to TradeKing " + cPos.getDescription() + " " + cPos.getCusip());
                e.printStackTrace();
            } catch (IOException e) {
                Logger.error("Issue connecting to TradeKing " + cPos.getDescription() + " " + cPos.getCusip());
                e.printStackTrace();
            }
        }
    }
}
