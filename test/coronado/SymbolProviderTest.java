package coronado;

import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import coronado.api.SecretKeys;
import org.junit.Test;

import com.google.common.collect.Lists;

import coronado.api.TradeKingProxy;
import coronado.model.OptionContract;

public class SymbolProviderTest {

	@Test
	public void testRefreshSymbolPool() throws Exception {
		List<String> symList = Lists.newArrayList();
		//		symList.add("SONC");
		//		symList.add("ABT");
		//		symList.add("PG");
		//		symList.add("JNJ");
		//		symList.add("TSLA");
		//		symList.add("YELP");
		//		symList.add("MSFT");
		//		symList.add("GOOG");
		//		symList.add("NFLX");
		//		symList.add("AMZN");
		symList.add("AAPL");
		SymbolProvider sp = new SymbolProvider(symList, new TradeKingProxy(new SecretKeys()));
		sp.refreshSymbolPool();
		Map<String, OptionContract> contracts = sp.getContractsMap();
		assertTrue("Only " + contracts.size() + " contracts found",
				contracts.size() > 200);
	}
}
