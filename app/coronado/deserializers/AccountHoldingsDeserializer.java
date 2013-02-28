package coronado.deserializers;

import com.google.common.collect.Lists;
import com.google.gson.*;
import coronado.api.model.AccountHoldingsResponse;

import java.lang.reflect.Type;
import java.util.List;

public class AccountHoldingsDeserializer implements
        JsonDeserializer<AccountHoldingsResponse.ListOfAccountHoldings> {
    @Override
    public AccountHoldingsResponse.ListOfAccountHoldings deserialize(
            final JsonElement json, final Type typeOfT,
            final JsonDeserializationContext context) {
        JsonObject jObj = (JsonObject) json;
        JsonElement holdingObj = jObj.getAsJsonObject("response")
                .getAsJsonObject("accountholdings").get("holding");

        List<AccountHoldingsResponse> accountHoldings = Lists.newArrayList();
        if(holdingObj.isJsonArray()) {
            JsonArray holdings = holdingObj.getAsJsonArray();
            for (JsonElement holding : holdings) {
                accountHoldings.add(addHolding(context, holding));
            }
        } else {
            accountHoldings.add(addHolding(context, holdingObj));
        }
        return new AccountHoldingsResponse.ListOfAccountHoldings(accountHoldings);
    }

    private AccountHoldingsResponse addHolding(final JsonDeserializationContext context, JsonElement holding) {
        AccountHoldingsResponse contract = context.deserialize(holding,
                AccountHoldingsResponse.class);
        JsonObject instrument = holding.getAsJsonObject().getAsJsonObject("instrument");
        JsonElement cusip = instrument.get("cusip");
        if(cusip != null) {
            contract.setCusip(cusip.getAsString());
        }
        JsonElement desc = instrument.get("desc");
        if(desc != null) {
            contract.setDesc(desc.getAsString());
        }
        return contract;
    }
}
