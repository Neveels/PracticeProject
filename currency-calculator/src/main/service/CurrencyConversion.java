package main.service;

public interface CurrencyConversion {
    String roundFinalValue(double value);

    String getResult(String expression);
}
