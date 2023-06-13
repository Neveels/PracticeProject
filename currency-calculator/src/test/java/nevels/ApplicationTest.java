package nevels;

import nevels.exception.BusinessException;
import nevels.service.CurrencyConversion;
import nevels.service.impl.CurrencyConversionImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class ApplicationTest {
    private static CurrencyConversion currencyConversion;

    @BeforeAll
    public static void setUp() {
        currencyConversion = new CurrencyConversionImpl();
    }

    @Test
    public void testCalculateTotalToDollars() {
        assertEquals("120.75", currencyConversion.getResult("toDollars(120р + toRubles($120 + toDollars(120р)))"));
        assertEquals("87.69", currencyConversion.getResult("toDollars(737р + toRubles($85,4))"));
    }

    @Test
    public void testCalculateTotalToRubles() {
        assertEquals("54878.04", currencyConversion.getResult("toRubles($85,4) + toRubles($85,4)"));
        assertEquals("77112.00", currencyConversion.getResult("toRubles($120) + toRubles($120)"));
    }

    @Test
    public void testCalculateTotalToDollarsNegative() {
        assertNotEquals("1230.75", currencyConversion.getResult("toDollars(120р + toRubles($120 + toDollars(120р)))"));
        assertNotEquals("13.75", currencyConversion.getResult("toDollars(120р + toRubles($120 + toDollars(120р)))"));
    }

    @Test
    public void testCalculateTotalToRublesNegative() {
        assertNotEquals("3.75", currencyConversion.getResult("toRubles($85,4) + toRubles($85,4)"));
        assertNotEquals("321.75", currencyConversion.getResult("toRubles($120) + toRubles($120)"));
    }


    @Test
    public void isThereIncorrectBracketPriority() {
        assertThrows(BusinessException.class,
                () -> currencyConversion.getResult("toDollars((120р + toRubles($120 + toDollars(120р)))"));
    }

    @Test
    public void isPresentIncorrectSymbols() {
        assertThrows(BusinessException.class,
                () -> currencyConversion.getResult("((!f120р + toRubles($120 + toDollars(120р)!)"));
    }

}