package coronado.analysis;

import coronado.api.SecretKeys;
import coronado.api.TradeKingProxy;
import org.junit.Test;

public class PositionAnalyzerTest {

	@Test
	public void testDownloadHistory() throws Exception {
		TradeKingProxy apiProxy = new TradeKingProxy(new SecretKeys());
		PositionAnalyzer pa = new PositionAnalyzer(apiProxy);
        pa.downloadHistory();
    }

    @Test
    public void testGetInvestmentSlices() throws Exception {
        final TradeKingProxy tkp = new TradeKingProxy(new SecretKeys());
        PositionAnalyzer pa = new PositionAnalyzer(tkp);
        pa.downloadHistory();
        System.out.println(pa.getInvestmentSlices());
    }
}
