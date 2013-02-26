package coronado;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import coronado.api.TradeKingProxy;
import coronado.api.model.OptionContractResponse;
import coronado.model.OptionContract;
import coronado.model.OptionContract.OptionType;

public class SymbolProvider {
	private final TradeKingProxy apiProxy;
	private final List<String> symbols;
	private final Map<String, OptionContract> contractsMap;

	public SymbolProvider(final List<String> symbols, final TradeKingProxy apiProxy) {
		this.apiProxy = apiProxy;
		this.symbols = symbols;
		contractsMap = Maps.newHashMap();
	}

	public void refreshSymbolPool() throws OAuthMessageSignerException,
	OAuthExpectationFailedException, OAuthCommunicationException,
	IOException, InterruptedException {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

		getContractsMap().clear();
		List<String> occSymbols = Lists.newArrayList();
		for(String symbol : symbols) {
			JsonArray prices = apiProxy.getOptionStrikePrices(symbol);
			JsonArray dates = apiProxy.getOptionExpirationDates(symbol);

			Iterator<JsonElement> dateIter = dates.iterator();
			while (dateIter.hasNext()) {
				String dateStr = dateIter.next().getAsString();
				try {
					Date date = formatter.parse(dateStr);
					Iterator<JsonElement> priceIter = prices.iterator();
					while (priceIter.hasNext()) {
						BigDecimal price = priceIter.next().getAsBigDecimal();

						for (OptionType optionType : OptionType.values()) {
							OptionContract currentOption = new OptionContract(
									symbol, date, price, optionType);
							String nextSym = currentOption.getOccSymbol();
							occSymbols.add(nextSym);
							getContractsMap().put(nextSym, currentOption);
						}
					}
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
		}

		List<OptionContractResponse> responses = apiProxy
				.getContractSizes(occSymbols);
		for (OptionContractResponse response : responses) {
			OptionContract contract = getContractsMap().get(
					response.getSymbol());
			contract.setFromApi(response);
		}

		Set<String> keys = Sets.newHashSet(getContractsMap().keySet());
		for (String key : keys) {
			OptionContract contract = getContractsMap().get(key);
			if (contract.getOpenInterest() < 500) {
				getContractsMap().remove(key);
			}
		}
	}

	public Map<String, OptionContract> getContractsMap() {
		return contractsMap;
	}
}
