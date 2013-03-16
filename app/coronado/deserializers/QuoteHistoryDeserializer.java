package coronado.deserializers;

import com.google.common.collect.Lists;
import com.google.gson.*;
import coronado.api.model.StockResponse;
import coronado.model.QuoteHistory;

import java.lang.reflect.Type;
import java.util.List;

public class QuoteHistoryDeserializer implements
        JsonDeserializer<QuoteHistory.ListOfQuoteHistory> {
    @Override
    public QuoteHistory.ListOfQuoteHistory deserialize(
            final JsonElement json, final Type typeOfT,
            final JsonDeserializationContext context) {
        JsonObject jObj = (JsonObject) json;
        JsonObject timeseriesJson = jObj.getAsJsonObject("response")
                .getAsJsonObject("timeseries");
        JsonElement quoteObj = timeseriesJson.getAsJsonObject("series").get("data");
        String symbol = timeseriesJson.get("symbol").getAsString();

        List<QuoteHistory> history = Lists.newArrayList();
        if(quoteObj.isJsonArray()) {
            JsonArray quotes = quoteObj.getAsJsonArray();
            for (JsonElement quote : quotes) {
                history.add(addQuote(context, quote, symbol));
            }
        } else {
            history.add(addQuote(context, quoteObj, symbol));
        }
        return new QuoteHistory.ListOfQuoteHistory(history);
    }

    private QuoteHistory addQuote(final JsonDeserializationContext context, final JsonElement quoteJson,
                                  final String symbol) {
        QuoteHistory quote = context.deserialize(quoteJson,
                QuoteHistory.class);
        quote.setSymbol(symbol);
        return quote;
    }
}
