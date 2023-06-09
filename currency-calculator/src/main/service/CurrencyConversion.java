package main.service;

public interface CurrencyConversion {
    String roundOffTheFinalValue(double value);

    String getResult(String expression);
}
