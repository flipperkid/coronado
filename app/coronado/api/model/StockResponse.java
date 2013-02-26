package coronado.api.model;

import java.math.BigDecimal;
import java.util.List;

public class StockResponse {
	private BigDecimal ask;
	private BigDecimal bid;
	private BigDecimal last;
	private String exch;
	private String symbol;

	public String getSymbol() {
		return symbol;
	}

	public String getExchange() {
		return exch;
	}

	public BigDecimal getAsk() {
		return ask;
	}

	public BigDecimal getPrice() {
		return last;
	}

	public BigDecimal getBid() {
		return bid;
	}

	public static class ListOfStocks {
		private final List<StockResponse> quotes;

		public ListOfStocks(final List<StockResponse> quotes) {
			this.quotes = quotes;
		}

		public List<StockResponse> get() {
			return quotes;
		}
	}
}
