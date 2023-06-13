package nevels;

import nevels.service.CurrencyConversion;
import nevels.service.impl.CurrencyConversionImpl;

import java.util.List;

public class Application {

    private final CurrencyConversion currencyConversion;

    public Application() {
        currencyConversion = new CurrencyConversionImpl();
    }

    public void run(String string) {
        System.out.println(currencyConversion.getResult(string));
    }

}
