package coronado.api.model;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.List;
import java.util.Locale;

public class OptionContractResponse {
	private int contract_size;
	private String openinterest;
	private String exch;
	private String symbol;
	private double ask;
	private double strikeprice;
	private char op_delivery;
	private double imp_volatility;
	private double last;

	public double getImpliedVolatility() {
		return imp_volatility;
	}

	public double getLastPrice() {
		return last;
	}

	public boolean isStandard() {
		return op_delivery == 'S';
	}

	public int getContractSize() {
		return contract_size;
	}

	public double getStrikePrice() {
		return strikeprice;
	}

	public String getSymbol() {
		return symbol;
	}

	public String getExchange() {
		return exch;
	}

	public double getAsk() {
		return ask;
	}

	public int getOpeninterest() {
		NumberFormat nf = NumberFormat.getInstance(Locale.US);
		try {
			return nf.parse(openinterest).intValue();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}

	@Override
	public String toString() {
		return String.format("{ symbol: %1s, contract size: %2s, ask: %3s, exchange: %4s, open interest: %5s }",
				symbol, contract_size, ask, exch, openinterest);
	}

	public static class ListOfOptionContracts {
		private final List<OptionContractResponse> quotes;

		public ListOfOptionContracts(final List<OptionContractResponse> quotes) {
			this.quotes = quotes;
		}

		public List<OptionContractResponse> get() {
			return quotes;
		}
	}
}
