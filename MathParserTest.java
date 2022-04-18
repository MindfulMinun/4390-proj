import java.util.regex.Matcher;
import java.util.regex.Pattern;

// javac MathParserTest.java && java MathParserTest
public class MathParserTest {
    public static void main(String[] args) {
        System.out.println(MathParserTest.performMath("8 + 2"));
        System.out.println(MathParserTest.performMath("8 - 2"));
        System.out.println(MathParserTest.performMath("8 * 2"));
        System.out.println(MathParserTest.performMath("8 / 2"));
        System.out.println(MathParserTest.performMath("1.5 * 2"));
        System.out.println(MathParserTest.performMath("3 / 2"));
    }

    public static double performMath(String expression) {
        // Compile a pattern that matches a simple arithmetic expression
        // This pattern has three groups:
        //     A number (with or without a decimal)
        //     An operator (+, -, *, or /)
        //     Another number (with or without a decimal)
        Pattern ptrn = Pattern.compile("\\s*(\\d+\\.?\\d*)\\s*([\\+\\-\\*\\/])\\s*(\\d+\\.?\\d*)\\s*");
        Matcher mtch = ptrn.matcher(expression);
        
        // Match the expression against the pattern
        // The pattern must match the entire expression, so nothing before or after the expression is allowed (except whitespace)
        boolean hasMatch = mtch.matches();
        // We could throw an error here, but I'm just returning zero for now.
        if (!hasMatch) return 0;

        // Get the groups from the match corresponding to the numbers and operators and parse them.
        double a = Double.parseDouble(mtch.group(1));
        String op = mtch.group(2);
        double b = Double.parseDouble(mtch.group(3));

        // Perform the operation.
        switch (op) {
            case "+": return a + b;
            case "-": return a - b;
            case "*": return a * b;
            case "/": return a / b;
            // If the operator is not one of the four above, we have an error.
            // We could also throw an error here.
            default: return 0;
        }
    }
}
