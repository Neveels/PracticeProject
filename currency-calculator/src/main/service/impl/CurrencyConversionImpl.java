package main.service.impl;

import main.config.ApplicationConfig;
import main.service.CurrencyConversion;
import main.utils.Container;
import main.utils.Lexeme;
import main.utils.LexemeBuffer;
import main.utils.enums.LexemeType;

import java.util.ArrayList;
import java.util.List;

public class CurrencyConversionImpl implements CurrencyConversion {

    private final List<Container> containers;
    private final ApplicationConfig applicationConfig;

    public CurrencyConversionImpl() {
        containers = new ArrayList<>();
        applicationConfig = new ApplicationConfig();
    }

    @Override
    public String roundFinalValue(double value) {
        return String.format("%.2f", value);
    }

    @Override
    public String getResult(String expression) {
        List<Lexeme> lexemes = lexAnalyze(expression);
        LexemeBuffer lexemeBuffer = new LexemeBuffer(lexemes);
        double resultOfExpr = expr(lexemeBuffer);
        return this.roundFinalValue(resultOfExpr);
    }


    private List<Lexeme> lexAnalyze(String expText) {
        List<Lexeme> lexemes = new ArrayList<>();
        int position = 0;
        while (position < expText.length()) {
            char charater = expText.charAt(position);
            switch (charater) {
                case '(' -> {
                    lexemes.add(new Lexeme(LexemeType.LEFT_BRACKET, charater));
                    position++;
                }
                case ')' -> {
                    lexemes.add(new Lexeme(LexemeType.RIGHT_BRACKET, charater));
                    position++;
                }
                case '+' -> {
                    lexemes.add(new Lexeme(LexemeType.OP_PLUS, charater));
                    position++;
                }
                case '-' -> {
                    lexemes.add(new Lexeme(LexemeType.OP_MINUS, charater));
                    position++;
                }
                case 't' -> {
                    final int TO_DOLLARS_LENGTH = 9;
                    final int TO_RUBLES_LENGTH = 8;
                    String dollars = expText.substring(position, position + TO_DOLLARS_LENGTH);
                    String rubles = expText.substring(position, position + TO_RUBLES_LENGTH);
                    if (dollars.equals(LexemeType.toDollars.toString())) {
                        lexemes.add(new Lexeme(LexemeType.toDollars, dollars));
                        position += TO_DOLLARS_LENGTH;
                    } else {
                        lexemes.add(new Lexeme(LexemeType.toRubles, rubles));
                        position += TO_RUBLES_LENGTH;
                    }
                }
                default -> {
                    if (charater == '$') {
                        position++;
                    }
                    charater = expText.charAt(position);
                    if (charater <= '9' && charater >= '0') {
                        StringBuilder stringBuilder = new StringBuilder();
                        do {
                            stringBuilder.append(charater);
                            position++;
                            if (position >= expText.length()) {
                                break;
                            }
                            charater = expText.charAt(position);
                        } while (charater <= '9' && charater >= '0' || charater == ',');
                        lexemes.add(new Lexeme(LexemeType.NUMBER, stringBuilder.toString()));
                    } else {
                        if (charater != ' ' && charater != 'р' && charater != '$') {
                            throw new RuntimeException("Unexpected character: " + charater);
                        }
                        position++;// If we found white space, just skip it
                    }
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
                        throw new RuntimeException("Unexpected token: " + lexeme.value
                                + " at position: " + lexemes.getPosition());
                    }
                    LexemeType key = containers.get(containers.size() - 1).getKey();
                    //to rubles
                    if (key.equals(LexemeType.toRubles)) {
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
                    if (key.equals(LexemeType.toDollars)) {
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
                case toDollars -> containers.add(new Container(LexemeType.toDollars, 0));
                case toRubles -> containers.add(new Container(LexemeType.toRubles, 0));
                default -> throw new RuntimeException("Unexpected token: " + lexeme.value
                        + " at position: " + lexemes.getPosition());
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

}
