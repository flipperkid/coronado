package coronado.deserializers;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.lang.reflect.Type;
import java.util.List;

import coronado.model.api.AccountHistoryResponse;

public class AccountHistoryDeserializer implements
        JsonDeserializer<AccountHistoryResponse.ListOfAccountHistories> {
    @Override
    public AccountHistoryResponse.ListOfAccountHistories deserialize(
            final JsonElement json, final Type typeOfT,
            final JsonDeserializationContext context) {
        JsonObject jObj = (JsonObject) json;
        JsonElement transactionObj = jObj.getAsJsonObject("response")
                .getAsJsonObject("transactions").get("transaction");

        List<AccountHistoryResponse> accountHistories = Lists.newArrayList();
        if(transactionObj.isJsonArray()) {
            JsonArray transactions = transactionObj.getAsJsonArray();
            for (JsonElement transaction : transactions) {
                accountHistories.add(addTransaction(context, transaction));
            }
        } else {
            accountHistories.add(addTransaction(context, transactionObj));
        }
        return new AccountHistoryResponse.ListOfAccountHistories(accountHistories);
    }

    private AccountHistoryResponse addTransaction(final JsonDeserializationContext context, JsonElement transaction) {
        AccountHistoryResponse contract = context.deserialize(transaction,
                AccountHistoryResponse.class);
        JsonObject trans = transaction.getAsJsonObject().getAsJsonObject("transaction");
        JsonElement quantity = trans.get("quantity");
        if(quantity != null) {
            contract.setQuantity(quantity.getAsDouble());
        }
        JsonElement cusip = trans.getAsJsonObject("security").get("cusip");
        if(cusip != null) {
            contract.setCusip(cusip.getAsString());
        }
        JsonElement secType = trans.getAsJsonObject("security").get("sectyp");
        if(secType != null) {
            contract.setType(secType.getAsString());
        }
        return contract;
    }
}
