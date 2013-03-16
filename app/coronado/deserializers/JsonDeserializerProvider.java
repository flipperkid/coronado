package coronado.deserializers;

import com.google.gson.*;

import coronado.api.model.StockResponse;
import coronado.model.QuoteHistory;
import coronado.model.api.AccountHistoryResponse;
import coronado.api.model.AccountHoldingsResponse;
import coronado.api.model.OptionContractResponse;

import java.lang.reflect.Type;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

public class JsonDeserializerProvider {
	public static Gson getJsonDeserializer() {
		GsonBuilder gb = new GsonBuilder();
		gb.registerTypeAdapter(OptionContractResponse.ListOfOptionContracts.class,
				new OptionContractDeserializer());
        gb.registerTypeAdapter(AccountHistoryResponse.ListOfAccountHistories.class,
                new AccountHistoryDeserializer());
        gb.registerTypeAdapter(AccountHoldingsResponse.ListOfAccountHoldings.class,
                new AccountHoldingsDeserializer());
        gb.registerTypeAdapter(StockResponse.ListOfStocks.class,
                new StockQuoteDeserializer());
        gb.registerTypeAdapter(QuoteHistory.ListOfQuoteHistory.class,
                new QuoteHistoryDeserializer());

        gb.registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
            public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                final String dateString = json.getAsString();
                SimpleDateFormat longFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                longFormat.setLenient(false);
                Date longDate = longFormat.parse(dateString, new ParsePosition(0));
                if(longDate != null) {
                    return longDate;
                }
                SimpleDateFormat shortFormat = new SimpleDateFormat("yyyy-MM-dd");
                shortFormat.setLenient(false);
                return shortFormat.parse(dateString, new ParsePosition(0));
            }
        });
        return gb.create();
	}
}
