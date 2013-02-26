package coronado.analysis;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.junit.Test;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;

import coronado.api.TradeKingProxy;
import coronado.api.model.OptionContractResponse;
import coronado.model.OptionContract;

public class OptionChainAnalysisTest {

	@Test
	public void testChain() throws Exception {
		TradeKingProxy apiProxy = new TradeKingProxy();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		Date date = formatter.parse("2013-03-16");
		OptionContract oc = new OptionContract("VXX", date, new BigDecimal(0),
				OptionContract.OptionType.CALL);

		List<OptionContractResponse> contracts = apiProxy.getOptionChain(oc);
		Collections.sort(contracts, new Comparator<OptionContractResponse>() {
			@Override
			public int compare(final OptionContractResponse o1,
					final OptionContractResponse o2) {
				if (o1.getStrikePrice() == o2.getStrikePrice()) {
					return 0;
				}
				return o1.getStrikePrice() < o2.getStrikePrice() ? -1 : 1;
			}
		});
		double stdDev = 6;
		double mean = 22.99;
		AbstractRealDistribution priceModel = new NormalDistribution(mean,
				stdDev);
		AbstractRealDistribution priceModelDer = new NormalDistribution(0,
				stdDev);
		for (OptionContractResponse contract : contracts) {
			double impVol = contract.getImpliedVolatility();
			double strike = contract.getStrikePrice();
			double ask = contract.getAsk();

			double evVol = stdDev * stdDev * priceModel.density(strike);
			double evCurr = (mean - strike)
					* priceModelDer.cumulativeProbability(mean - strike);
			double value = (evVol + evCurr) / ask - 1.0;

			if (value > 0) {
				System.out.println(String.format(
						"Strike: %.2f ask: %.2f value: %.2f imp vol: %.2f",
						strike, ask, value, impVol));
			}
		}
	}

	@Test
	public void testBestInChain() throws Exception {
		TradeKingProxy apiProxy = new TradeKingProxy();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		Date date = formatter.parse("2013-03-16");
		OptionContract oc = new OptionContract("VXX", date, new BigDecimal(0),
				OptionContract.OptionType.CALL);

		List<OptionContractResponse> contracts = apiProxy.getOptionChain(oc);
		Collections.sort(contracts, new Comparator<OptionContractResponse>() {
			@Override
			public int compare(final OptionContractResponse o1,
					final OptionContractResponse o2) {
				if (o1.getImpliedVolatility() == o2.getImpliedVolatility()) {
					return 0;
				}
				return o1.getImpliedVolatility() < o2.getImpliedVolatility() ? -1
						: 1;
			}
		});

		boolean notFound = true;
		double mean = 22.99;
		double impVol = Iterables.getFirst(Iterables.filter(Collections2
				.transform(contracts,
						new Function<OptionContractResponse, Double>() {
					@Override
					public Double apply(
							final OptionContractResponse contract) {
						return contract.getImpliedVolatility();
					}
				}), new Predicate<Double>() {
			@Override
			public boolean apply(final Double impliedVolativlity) {
				return impliedVolativlity.doubleValue() > 0;
			}
		}), new Double(0.1));
		for (double stdDev = Math.sqrt(39.0 / 251.0) * mean * impVol; stdDev < mean
				&& notFound; stdDev += 0.1) {
			AbstractRealDistribution priceModel = new NormalDistribution(mean,
					stdDev);
			AbstractRealDistribution priceModelDer = new NormalDistribution(0,
					stdDev);
			for (OptionContractResponse contract : contracts) {
				double strike = contract.getStrikePrice();
				double ask = contract.getAsk();

				double evVol = stdDev * stdDev * priceModel.density(strike);
				double evCurr = (mean - strike)
						* priceModelDer.cumulativeProbability(mean - strike);
				double value = (evVol + evCurr) / ask - 1.0;

				if (value > 0) {
					System.out.println(String.format(
							"Strike: %.2f ask: %.2f value: %.2f std dev: %.2f",
							strike, ask, value, stdDev));
				}
				if (value > 0.2) {
					notFound = false;
				}
			}
		}
	}
}
