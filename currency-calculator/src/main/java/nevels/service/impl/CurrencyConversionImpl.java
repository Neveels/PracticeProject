package nevels.service.impl;

import nevels.config.ApplicationConfig;
import nevels.exception.BusinessException;
import nevels.service.CurrencyConversion;
import nevels.utils.Container;
import nevels.utils.Lexeme;
import nevels.utils.LexemeBuffer;
import nevels.utils.enums.LexemeType;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static nevels.utils.enums.LexemeType.TO_DOLLARS;
import static nevels.utils.enums.LexemeType.TO_RUBLES;


public class CurrencyConversionImpl implements CurrencyConversion {

    private final List<Container> containers;
    private final ApplicationConfig applicationConfig;
    private final Set<String> MAIN_CHARACTERS = Set.of("(", ")", "+", "-");
    private final Set<String> WHITESPACE_CHARACTERS = Set.of(" ", "р", "$");
    private final Set<String> DIGITS = Set.of("1", "2", "3", "4", "5", "6", "7", "8", "9", "0");
    private final int DOLLARS_LENGTH = TO_DOLLARS.getString().length();
    private final int RUBLES_LENGTH = TO_RUBLES.getString().length();
    public final String UNEXPECTED_CHARACTER_EXCEPTION = "Unexpected character %s ";
    public final String UNEXPECTED_TOKEN_EXCEPTION = "Unexpected token %s at position %d";

    public CurrencyConversionImpl() {
        containers = new ArrayList<>();
        applicationConfig = new ApplicationConfig();
    }

    @Override
    public String getResult(String expression) {
        List<Lexeme> lexemes = lexAnalyze(expression);
        LexemeBuffer lexemeBuffer = new LexemeBuffer(lexemes);
        double resultOfExpr = expr(lexemeBuffer);
        return roundFinalValue(resultOfExpr);
    }


    private List<Lexeme> lexAnalyze(String expText) {
        List<Lexeme> lexemes = new ArrayList<>();
        int position = 0;
        while (position < expText.length()) {
            String symbol = String.valueOf(expText.charAt(position));
            if (MAIN_CHARACTERS.contains(symbol)) {
                lexemes.add(new Lexeme(LexemeType.getFromChar(symbol), symbol));
                position++;
            } else {
                if (symbol.equals("t")) {
                    String dollars = expText.substring(position, position + DOLLARS_LENGTH);
                    String rubles = expText.substring(position, position + RUBLES_LENGTH);
                    if (dollars.equals(TO_DOLLARS.getString())) {
                        lexemes.add(new Lexeme(TO_DOLLARS, dollars));
                        position += DOLLARS_LENGTH;
                    } else {
                        lexemes.add(new Lexeme(TO_RUBLES, rubles));
                        position += RUBLES_LENGTH;
                    }
                    continue;
                }
                if (symbol.equals("$")) {
                    position++;
                }
                symbol = String.valueOf(expText.charAt(position));
                if (DIGITS.contains(symbol)) {
                    StringBuilder stringBuilder = new StringBuilder();
                    do {
                        stringBuilder.append(symbol);
                        position++;
                        if (position >= expText.length()) {
                            break;
                        }
                        symbol = String.valueOf(expText.charAt(position));
                    } while (DIGITS.contains(symbol) || symbol.equals(","));
                    lexemes.add(new Lexeme(LexemeType.NUMBER, stringBuilder.toString()));
                } else {
                    if (WHITESPACE_CHARACTERS.contains(symbol)) {
                        position++;// If we found white space, just skip it
                        continue;
                    }
                    throw new BusinessException(String.format(UNEXPECTED_CHARACTER_EXCEPTION, symbol));
                }
            }
        }
        lexemes.add(new Lexeme(LexemeType.EOF, ""));
        return lexemes;
    }

    private double expr(LexemeBuffer lexemes) {
        Lexeme lexeme = lexemes.next();
        if (lexeme.type == LexemeType.EOF) {
            return 0;
        } else {
            lexemes.back();
            return plusminus(lexemes);
        }
    }

    private double plusminus(LexemeBuffer lexemes) {
        double value = factor(lexemes);
        while (true) {
            Lexeme lexeme = lexemes.next();
            switch (lexeme.type) {
                case OP_PLUS -> {
                    value += factor(lexemes);
                }
                case OP_MINUS -> {
                    value -= factor(lexemes);
                }
                default -> {
                    lexemes.back();
                    return value;
                }
            }
        }
    }

    private double factor(LexemeBuffer lexemes) {
        while (true) {
            Lexeme lexeme = lexemes.next();
            switch (lexeme.type) {
                case NUMBER -> {
                    final int INDEX_OF_LAST_ELEMENT = containers.size() - 1;
                    Container container = containers.get(INDEX_OF_LAST_ELEMENT);
                    String doubleNumber = lexeme.value.replace(',', '.');
                    double number = Double.parseDouble(doubleNumber);
                    containers.set(INDEX_OF_LAST_ELEMENT, new Container(container.getKey(), container.getValue() + number));
                    return number;
                }
                case LEFT_BRACKET -> {
                    double value = expr(lexemes);
                    lexeme = lexemes.next();
                    if (lexeme.type != LexemeType.RIGHT_BRACKET) {
                        throw new BusinessException(String.format(
                                UNEXPECTED_TOKEN_EXCEPTION,
                                lexeme.value,
                                lexemes.getPosition())
                        );
                    }
                    LexemeType key = containers.get(containers.size() - 1).getKey();
                    //to rubles
                    if (key.equals(TO_RUBLES)) {
                        if (containers.size() == 1) {
                            value = toRubles(containers.get(0).getValue());
                            containers.remove(0);
                            return value;
                        }
                        Container containerWithRubles = containers.get(containers.size() - 1);
                        containers.remove(containers.size() - 1);
                        double rubles = toRubles(containerWithRubles.getValue());
                        containerWithRubles.setValue(rubles);
                        setValueToLastPosition(containerWithRubles.getValue());
                    }
                    //to dollars
                    if (key.equals(TO_DOLLARS)) {
                        if (containers.size() == 1) {
                            value = toDollars(containers.get(0).getValue());
                            containers.remove(0);
                            return value;
                        }
                        Container containerWithDollars = containers.get(containers.size() - 1);
                        double dollars = toDollars(containerWithDollars.getValue());
                        containerWithDollars.setValue(dollars);
                        containers.remove(containers.size() - 1);

                        setValueToLastPosition(containerWithDollars.getValue());
                    }
                    return value;
                }
                case TO_DOLLARS -> containers.add(new Container(TO_DOLLARS, 0));
                case TO_RUBLES -> containers.add(new Container(TO_RUBLES, 0));
                default -> throw new BusinessException(String.format(
                        UNEXPECTED_TOKEN_EXCEPTION,
                        lexeme.value,
                        lexemes.getPosition())
                );
            }
        }
    }

    private void setValueToLastPosition(double containerWithDollars) {
        Container lastContainerAfterRemoveTheLastOne = containers.get(containers.size() - 1);
        containers.set(containers.size() - 1,
                new Container(
                        lastContainerAfterRemoveTheLastOne.getKey(),
                        lastContainerAfterRemoveTheLastOne.getValue() + containerWithDollars
                )
        );
    }

    private double toRubles(double value) {
        return value * exchangeRate();
    }

    private double toDollars(double value) {
        return value / exchangeRate();
    }

    private double exchangeRate() {
        return applicationConfig.readFromFile();
    }

    private String roundFinalValue(double value) {
        return String.format("%.2f", value);
    }

}
