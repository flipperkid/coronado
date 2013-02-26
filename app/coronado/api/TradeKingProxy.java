package coronado.api;

import java.io.IOException;
import java.util.List;

import coronado.api.model.AccountHistoryResponse;
import coronado.api.model.AccountHoldingsResponse;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import coronado.api.model.OptionContractResponse;
import coronado.api.model.StockResponse;
import coronado.deserializers.JsonDeserializerProvider;
import coronado.model.OptionContract;

public class TradeKingProxy {
	private final OAuthHttpClient client;
	private final JsonParser jsonParser;
	private final Gson jsonDeserializer;
    private final SecretKeys keys;

	public TradeKingProxy(final SecretKeys keys) throws Exception {
		jsonDeserializer = JsonDeserializerProvider.getJsonDeserializer();
		jsonParser = new JsonParser();
		client = new OAuthHttpClient(keys);
        this.keys = keys;
	}

	public List<OptionContractResponse> getContractSizes(
			final List<String> symbols) throws OAuthMessageSignerException,
			OAuthExpectationFailedException, OAuthCommunicationException,
			IOException, InterruptedException {
		final String url = "market/ext/quotes.json?fids=contract_size,openinterest";

		final int blockSize = 500;
		int startIndex = 0;
		List<OptionContractResponse> contracts = Lists.newArrayList();
		do {
			int endIndex = Math.min(startIndex + blockSize, symbols.size());
			List<String> currentSymbolSet = symbols.subList(startIndex,
					endIndex);
			startIndex = endIndex;
			final SyncRequest request = new SyncRequest(url, client);
			request.setContent("symbols="
					+ Joiner.on(",").join(currentSymbolSet));

			final String response = request.send();
            OptionContractResponse.ListOfOptionContracts optionContracts = jsonDeserializer
					.fromJson(response,
							OptionContractResponse.ListOfOptionContracts.class);
			contracts.addAll(optionContracts.get());
		} while (startIndex < symbols.size());
		return contracts;
	}

	public List<OptionContractResponse> getOptionChain(final OptionContract contract)
			throws OAuthMessageSignerException, OAuthExpectationFailedException,
			OAuthCommunicationException, IOException, InterruptedException {
		final String contractType = contract.getOptionType().toString().toLowerCase();
		final String expirationDate = contract.getExpirationDateTkFormat();
		final String url = "market/options/search.json?fids=ask,strikeprice,op_delivery,imp_volatility,last&symbol=" +
				contract.getUnderlyingSymbol();
		final String query = "&query=xdate-eq%3A" + expirationDate +
				"%20AND%20put_call-eq%3A" + contractType;
		final SyncRequest request = new SyncRequest(url + query, client);

		final String response = request.send();
        OptionContractResponse.ListOfOptionContracts optionContracts = jsonDeserializer
				.fromJson(response,
						OptionContractResponse.ListOfOptionContracts.class);
		return optionContracts.get();
	}

	public JsonArray getOptionStrikePrices(final String symbol)
			throws OAuthMessageSignerException,
			OAuthExpectationFailedException, OAuthCommunicationException,
			IOException, InterruptedException {
		final String url = "market/options/strikes.json?symbol=" + symbol;
		final SyncRequest request = new SyncRequest(url, client);

		final String response = request.send();
		final JsonObject rootResponse = (JsonObject) jsonParser.parse(response);
		return rootResponse.getAsJsonObject("response")
				.getAsJsonObject("prices").getAsJsonArray("price");
	}

	public JsonArray getOptionExpirationDates(final String symbol)
			throws OAuthMessageSignerException,
			OAuthExpectationFailedException, OAuthCommunicationException,
			IOException, InterruptedException {
		final String url = "market/options/expirations.json?symbol=" + symbol;
		final SyncRequest request = new SyncRequest(url, client);

		final String response = request.send();
		final JsonObject rootResponse = (JsonObject) jsonParser.parse(response);
		return rootResponse.getAsJsonObject("response")
				.getAsJsonObject("expirationdates").getAsJsonArray("date");
	}

	public List<StockResponse> getStockPrice(final List<String> symbols)
			throws OAuthMessageSignerException,
			OAuthExpectationFailedException, OAuthCommunicationException,
			IOException, InterruptedException {
		final String url = "market/ext/quotes.json?fids=last,ask,bid";
		final SyncRequest request = new SyncRequest(url, client);
		request.setContent("symbols=" + Joiner.on(",").join(symbols));

		final String response = request.send();
		StockResponse.ListOfStocks stocks = jsonDeserializer.fromJson(response,
				StockResponse.ListOfStocks.class);
		return stocks.get();
	}

    public List<AccountHistoryResponse> getHistory()
            throws OAuthMessageSignerException,
            OAuthExpectationFailedException, OAuthCommunicationException,
            IOException, InterruptedException {
        final String url = "accounts/" + keys.ACCOUNT_NUMBER + "/history.json?range=all&transactions=all";
        final SyncRequest request = new SyncRequest(url, client);

        final String response = request.send();
        AccountHistoryResponse.ListOfAccountHistories accountHistories = jsonDeserializer
                .fromJson(response,
                        AccountHistoryResponse.ListOfAccountHistories.class);
        return accountHistories.get();
    }

    public List<AccountHoldingsResponse> getHoldings()
            throws OAuthMessageSignerException,
            OAuthExpectationFailedException, OAuthCommunicationException,
            IOException, InterruptedException {
        final String url = "accounts/" + keys.ACCOUNT_NUMBER + "/holdings.json";
        final SyncRequest request = new SyncRequest(url, client);

        final String response = request.send();
        AccountHoldingsResponse.ListOfAccountHoldings holdings = jsonDeserializer
                .fromJson(response,
                        AccountHoldingsResponse.ListOfAccountHoldings.class);
        return holdings.get();
    }

    public String getHistoricData()
            throws OAuthMessageSignerException,
            OAuthExpectationFailedException, OAuthCommunicationException,
            IOException, InterruptedException {
        final String url = "market/historical/search.json?symbols=AAPL&interval=daily&startdate=2013-01-01&enddate=2013-02-24";
        final SyncRequest request = new SyncRequest(url, client);

        final String response = request.send();
        return response;
    }}
