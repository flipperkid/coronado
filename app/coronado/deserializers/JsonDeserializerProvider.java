package coronado.deserializers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import coronado.model.api.AccountHistoryResponse;
import coronado.api.model.AccountHoldingsResponse;
import coronado.api.model.OptionContractResponse;

public class JsonDeserializerProvider {
	public static Gson getJsonDeserializer() {
		GsonBuilder gb = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		gb.registerTypeAdapter(OptionContractResponse.ListOfOptionContracts.class,
				new OptionContractDeserializer());
        gb.registerTypeAdapter(AccountHistoryResponse.ListOfAccountHistories.class,
                new AccountHistoryDeserializer());
        gb.registerTypeAdapter(AccountHoldingsResponse.ListOfAccountHoldings.class,
                new AccountHoldingsDeserializer());
        return gb.create();
	}
}
