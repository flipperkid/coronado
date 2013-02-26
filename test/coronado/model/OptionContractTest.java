package coronado.model;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Test;

import coronado.model.OptionContract.OptionType;

public class OptionContractTest {

	@Test
	public void testGetOccSymbol() throws Exception {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		Date date = formatter.parse("2014-01-18");
		OptionContract contract = new OptionContract("IBM", date,
				new BigDecimal(200), OptionType.PUT);
		String sym = contract.getOccSymbol();
		assertEquals("IBM140118P00200000", sym);
	}
}
