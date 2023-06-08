package main.service;

public interface CurrencyConversion {
    double roundOffTheFinalValue(double value);

    double getResult(String expression);
}
