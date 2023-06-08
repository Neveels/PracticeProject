package main;


import main.config.ApplicationConfig;
import main.service.Container;
import main.service.Lexeme;
import main.service.LexemeBuffer;
import main.service.LexemeType;

import java.util.ArrayList;
import java.util.List;


public class Application {

    static List<Container> lexemeTypeIntegerMap = new ArrayList<>();

    public Application() {

    }

    public void run(List<String> testCases) {
        int iterator = 0;
        while (iterator < testCases.size()) {
            List<Lexeme> lexemes = lexAnalyze(testCases.get(iterator));
            LexemeBuffer lexemeBuffer = new LexemeBuffer(lexemes);
            double expr = expr(lexemeBuffer);
            double finalValue = Math.round(expr * 100.0) / 100.0;
            System.out.println("Case: " + ++iterator + " -> Expression: " + testCases.get(iterator - 1) + ", result: " + finalValue);
        }

    }

    public static List<Lexeme> lexAnalyze(String expText) {
        List<Lexeme> lexemes = new ArrayList<>();
        int position = 0;
        while (position < expText.length()) {
            char charater = expText.charAt(position);
            switch (charater) {
                case '(':
                    lexemes.add(new Lexeme(LexemeType.LEFT_BRACKET, charater));
                    position++;
                    continue;
                case ')':
                    lexemes.add(new Lexeme(LexemeType.RIGHT_BRACKET, charater));
                    position++;
                    continue;
                case '+':
                    lexemes.add(new Lexeme(LexemeType.OP_PLUS, charater));
                    position++;
                    continue;
                case '-':
                    lexemes.add(new Lexeme(LexemeType.OP_MINUS, charater));
                    position++;
                    continue;
                case 't':
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
                    continue;
                default:
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
        lexemes.add(new Lexeme(LexemeType.EOF, ""));
        return lexemes;
    }

    public static double expr(LexemeBuffer lexemes) {
        Lexeme lexeme = lexemes.next();
        if (lexeme.type == LexemeType.EOF) {
            return 0;
        } else {
            lexemes.back();
            return plusminus(lexemes);
        }
    }

    public static double plusminus(LexemeBuffer lexemes) {
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

    public static double factor(LexemeBuffer lexemes) {
        while (true) {
            Lexeme lexeme = lexemes.next();
            switch (lexeme.type) {
                case NUMBER -> {
                    final int INDEX_OF_LAST_ELEMENT = lexemeTypeIntegerMap.size() - 1;
                    Container container = lexemeTypeIntegerMap.get(INDEX_OF_LAST_ELEMENT);
                    String doubleNumber = lexeme.value.replace(',', '.');
                    double number = Double.parseDouble(doubleNumber);
                    lexemeTypeIntegerMap.set(INDEX_OF_LAST_ELEMENT, new Container(container.getKey(), container.getValue() + number));
                    return number;
                }
                case LEFT_BRACKET -> {
                    double value = expr(lexemes);
                    lexeme = lexemes.next();
                    if (lexeme.type != LexemeType.RIGHT_BRACKET) {
                        throw new RuntimeException("Unexpected token: " + lexeme.value
                                + " at position: " + lexemes.getPosition());
                    }
                    LexemeType key = lexemeTypeIntegerMap.get(lexemeTypeIntegerMap.size() - 1).getKey();

                    //to rubles
                    if (key.equals(LexemeType.toRubles)) {
                        if (lexemeTypeIntegerMap.size() == 1) {
                            value = toRubles(lexemeTypeIntegerMap.get(0).getValue());
                            lexemeTypeIntegerMap.remove(0);
                            return value;
                        }
                        Container containerWithRubles = lexemeTypeIntegerMap.get(lexemeTypeIntegerMap.size() - 1);
                        lexemeTypeIntegerMap.remove(lexemeTypeIntegerMap.size() - 1);
                        double rubles = toRubles(containerWithRubles.getValue());
                        containerWithRubles.setValue(rubles);
                        lexemeTypeIntegerMap.set(lexemeTypeIntegerMap.size() - 1,
                                new Container(
                                        lexemeTypeIntegerMap.get(lexemeTypeIntegerMap.size() - 1).getKey(),
                                        lexemeTypeIntegerMap.get(lexemeTypeIntegerMap.size() - 1).getValue() + containerWithRubles.getValue()
                                )
                        );
//                        System.out.println("Container with rubles: key - " + containerWithRubles.getKey() + ", value - " + containerWithRubles.getValue() + "р");
                    }
                    //to dollars
                    if (key.equals(LexemeType.toDollars)) {
                        if (lexemeTypeIntegerMap.size() == 1) {
                            value = toDollars(lexemeTypeIntegerMap.get(0).getValue());
                            lexemeTypeIntegerMap.remove(0);
                            return value;
                        }
                        Container containerWithDollars = lexemeTypeIntegerMap.get(lexemeTypeIntegerMap.size() - 1);
                        double dollars = toDollars(containerWithDollars.getValue());
                        containerWithDollars.setValue(dollars);
                        lexemeTypeIntegerMap.remove(lexemeTypeIntegerMap.size() - 1);
                        lexemeTypeIntegerMap.set(lexemeTypeIntegerMap.size() - 1,
                                new Container(
                                        lexemeTypeIntegerMap.get(lexemeTypeIntegerMap.size() - 1).getKey(),
                                        lexemeTypeIntegerMap.get(lexemeTypeIntegerMap.size() - 1).getValue() + containerWithDollars.getValue()
                                )
                        );
//                        System.out.println("Container with dollars: key - " + containerWithDollars.getKey() + ", value - " + containerWithDollars.getValue() + "$");
                    }
                    return value;
                }
                case toDollars -> lexemeTypeIntegerMap.add(new Container(LexemeType.toDollars, 0));
                case toRubles -> lexemeTypeIntegerMap.add(new Container(LexemeType.toRubles, 0));
                default -> throw new RuntimeException("Unexpected token: " + lexeme.value
                        + " at position: " + lexemes.getPosition());
            }
        }
    }

    static double toRubles(double value) {
        return value * exchangeRate();
    }

    static double toDollars(double value) {
        return value / exchangeRate();
    }


    static double exchangeRate() {
        ApplicationConfig applicationConfig = new ApplicationConfig();
        return applicationConfig.readFromFile();
    }
}
