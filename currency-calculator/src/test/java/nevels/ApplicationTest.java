package nevels;

import nevels.service.CurrencyConversion;
import nevels.service.impl.CurrencyConversionImpl;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

public class ApplicationTest {
    private CurrencyConversion currencyConversion;

    @Before
    public void setUp() {
        currencyConversion = new CurrencyConversionImpl();
    }

    @Test
    public void testCalculateTotal() {
        String result = currencyConversion.getResult("toDollars(120р + toRubles($120 + toDollars(120р)))");
        assertEquals(result, "120.75");
    }
}