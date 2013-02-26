package coronado.deserializers;

import java.lang.reflect.Type;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import coronado.api.model.OptionContractResponse;

public class OptionContractDeserializer implements
JsonDeserializer<OptionContractResponse.ListOfOptionContracts> {
	@Override
	public OptionContractResponse.ListOfOptionContracts deserialize(
			final JsonElement json, final Type typeOfT,
			final JsonDeserializationContext context) {
		JsonObject jObj = (JsonObject) json;
		JsonElement quoteObj = jObj.getAsJsonObject("response")
				.getAsJsonObject("quotes").get("quote");

		List<OptionContractResponse> optionContracts = Lists.newArrayList();
		if(quoteObj.isJsonArray()) {
			JsonArray quotes = quoteObj.getAsJsonArray();
			for (JsonElement quote : quotes) {
				OptionContractResponse contract = context.deserialize(quote,
						OptionContractResponse.class);
				if(contract.isStandard()) {
					optionContracts.add(contract);
				}
			}
		} else {
			OptionContractResponse contract = context.deserialize(quoteObj,
					OptionContractResponse.class);
			optionContracts.add(contract);
		}
		return new OptionContractResponse.ListOfOptionContracts(optionContracts);
	}
}
