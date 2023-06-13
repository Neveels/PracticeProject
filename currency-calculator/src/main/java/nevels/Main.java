package nevels;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        List<String> testCases = new ArrayList<>(){{
            add("toDollars(120р + toRubles($120 + toDollars(120р)))");
            add("toDollars(737р + toRubles($85,4))");
            add("toRubles($85,4) + toRubles($85,4)");
            add("toRubles($120) + toRubles($120)");
        }};
        new Application().run(testCases);
    }
}