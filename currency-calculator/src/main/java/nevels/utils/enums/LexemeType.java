package nevels.utils.enums;

import nevels.exception.BusinessException;

import java.util.Arrays;
import java.util.Objects;

public enum LexemeType {
    LEFT_BRACKET("("),
    RIGHT_BRACKET(")"),
    OP_PLUS("+"),
    OP_MINUS("-"),
    TO_DOLLARS("toDollars"),
    TO_RUBLES("toRubles"),
    NUMBER(""),
    EOF("");
    private final String lexemeType;

    LexemeType(String lexemeType) {
        this.lexemeType = lexemeType;
    }

    public String getString() {
        return lexemeType;
    }

    public static LexemeType getFromChar(String string) {
        return Arrays.stream(LexemeType.values())
                .filter(lexemeType -> Objects.equals(lexemeType.lexemeType, string))
                .findFirst()
                .orElseThrow(() -> new BusinessException(String.format("Value %s not found", string)));
    }

}
