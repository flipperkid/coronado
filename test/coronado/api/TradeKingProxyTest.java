package coronado.api;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import coronado.api.model.AccountHistoryResponse;
import coronado.api.model.AccountHoldingsResponse;
import org.junit.Test;

import com.google.common.collect.Lists;

import coronado.api.model.OptionContractResponse;
import coronado.api.model.StockResponse;
import coronado.model.OptionContract;

public class TradeKingProxyTest {

	@Test
	public void testGetContractSizes() throws Exception {
		TradeKingProxy apiProxy = new TradeKingProxy(new SecretKeys());
		List<String> occSyms = Lists.newArrayList();
		occSyms.add("AAPL121214C00605000");
		occSyms.add("AAPL121214C00600000");

		List<OptionContractResponse> contracts = apiProxy.getContractSizes(occSyms);
		assertEquals(2, contracts.size());
	}

	@Test
	public void testGetOneContractSize() throws Exception {
		TradeKingProxy apiProxy = new TradeKingProxy(new SecretKeys());
		List<String> occSyms = Lists.newArrayList();
		occSyms.add("AAPL021214C00600000");

		List<OptionContractResponse> contracts = apiProxy.getContractSizes(occSyms);
		assertEquals(1, contracts.size());
	}

	@Test
	public void testGetOptionChain() throws Exception {
		TradeKingProxy apiProxy = new TradeKingProxy(new SecretKeys());
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		Date date = formatter.parse("2013-03-16");
		OptionContract oc = new OptionContract("VXX", date, new BigDecimal(0), OptionContract.OptionType.CALL);

		List<OptionContractResponse> contracts = apiProxy.getOptionChain(oc);
		assert(contracts.size() > 0);
	}

	@Test
	public void testGetPrice() throws Exception {
		TradeKingProxy apiProxy = new TradeKingProxy(new SecretKeys());
		List<String> syms = Lists.newArrayList();
		syms.add("AAPL");
		syms.add("IBM");

		List<StockResponse> stocks = apiProxy.getStockPrice(syms);
		assertEquals(2, stocks.size());
	}

    @Test
    public void testGetHistory() throws Exception {
        TradeKingProxy apiProxy = new TradeKingProxy(new SecretKeys());
        List<AccountHistoryResponse> accountHistories = apiProxy.getHistory();
        System.out.println(accountHistories);
    }

    @Test
    public void testGetHoldings() throws Exception {
        TradeKingProxy apiProxy = new TradeKingProxy(new SecretKeys());
        List<AccountHoldingsResponse> accountHoldings = apiProxy.getHoldings();
        System.out.println(accountHoldings);
    }
}
