package main;


import main.service.CurrencyConversion;
import main.service.impl.CurrencyConversionImpl;

import java.util.ArrayList;
import java.util.List;


public class Application {

    private final CurrencyConversion currencyConversion;

    public Application() {
        currencyConversion = new CurrencyConversionImpl();
    }

    public void run(List<String> testCases) {
        int iterator = 0;
        while (iterator < testCases.size()) {
            double result = currencyConversion.getResult(testCases.get(iterator));
            System.out.println("Case: " + ++iterator + " -> Expression: " + testCases.get(iterator - 1) + ", result: " + result);
        }

    }


}
