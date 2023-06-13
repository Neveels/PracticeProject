package nevels;

import nevels.service.CurrencyConversion;
import nevels.service.impl.CurrencyConversionImpl;

import java.util.List;

public class Application {

    private final CurrencyConversion currencyConversion;

    public Application() {
        currencyConversion = new CurrencyConversionImpl();
    }

    public void run(List<String> testCases) {
        int iterator = 0;
        while (iterator < testCases.size()) {
            String result = currencyConversion.getResult(testCases.get(iterator));
            System.out.println("Case: " + ++iterator + " -> Expression: " + testCases.get(iterator - 1) + ", result: " + result);
        }
    }

}
