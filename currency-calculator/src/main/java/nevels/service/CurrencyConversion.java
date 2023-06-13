package nevels.service;

public interface CurrencyConversion {
    String roundFinalValue(double value);

    String getResult(String expression);
}
