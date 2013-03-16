package coronado.deserializers;

import com.google.common.collect.Lists;
import com.google.gson.*;
import coronado.api.model.AccountHoldingsResponse;
import coronado.api.model.StockResponse;

import java.lang.reflect.Type;
import java.util.List;

public class StockQuoteDeserializer implements
        JsonDeserializer<StockResponse.ListOfStocks> {
    @Override
    public StockResponse.ListOfStocks deserialize(
            final JsonElement json, final Type typeOfT,
            final JsonDeserializationContext context) {
        JsonObject jObj = (JsonObject) json;
        JsonElement stockObj = jObj.getAsJsonObject("response")
                .getAsJsonObject("quotes").get("quote");

        List<StockResponse> quotes = Lists.newArrayList();
        if(stockObj.isJsonArray()) {
            JsonArray holdings = stockObj.getAsJsonArray();
            for (JsonElement holding : holdings) {
                quotes.add(addQuote(context, holding));
            }
        } else {
            quotes.add(addQuote(context, stockObj));
        }
        return new StockResponse.ListOfStocks(quotes);
    }

    private StockResponse addQuote(final JsonDeserializationContext context, JsonElement quoteJson) {
        StockResponse quote = context.deserialize(quoteJson,
                StockResponse.class);
        return quote;
    }
}
