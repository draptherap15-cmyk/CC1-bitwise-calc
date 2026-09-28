import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * A comprehensive Bitwise and Arithmetic Expression Calculator in Java.
 * Supports tokenization, recursive descent parsing, custom operator precedence,
 * and robust error handling without external libraries.
 */
public class AdvancedBitwiseCalculator {

    // ==========================================
    // 1. LEXER / TOKENIZER COMPONENTS
    // ==========================================

    enum TokenType {
        NUMBER, OPERATOR, LPAREN, RPAREN, EOF
    }

    static class Token {
        TokenType type;
        String text;

        public Token(TokenType type, String text) {
            this.type = type;
            this.text = text;
        }

        @Override
        public String toString() {
            return "[" + type + ": " + text + "]";
        }
    }

    static class Lexer {
        private final String input;
        private int pos = 0;

        public Lexer(String input) {
            this.input = input;
        }

        public List<Token> tokenize() {
            List<Token> tokens = new ArrayList<>();

            while (pos < input.length()) {
                char c = input.charAt(pos);

                // Ignore whitespace
                if (Character.isWhitespace(c)) {
                    pos++;
                    continue;
                }

                // Numbers (Decimal integers)
                if (Character.isDigit(c)) {
                    int start = pos;
                    while (pos < input.length() && Character.isDigit(input.charAt(pos))) {
                        pos++;
                    }
                    tokens.add(new Token(TokenType.NUMBER, input.substring(start, pos)));
                    continue;
                }

                // Word operators (AND, OR, XOR, NOT, NAND, NOR, XNOR)
                if (Character.isLetter(c)) {
                    int start = pos;
                    while (pos < input.length() && Character.isLetter(input.charAt(pos))) {
                        pos++;
                    }
                    tokens.add(new Token(TokenType.OPERATOR, input.substring(start, pos).toUpperCase()));
                    continue;
                }

                // Parentheses and single-character symbols
                if (c == '(') { tokens.add(new Token(TokenType.LPAREN, "(")); pos++; continue; }
                if (c == ')') { tokens.add(new Token(TokenType.RPAREN, ")")); pos++; continue; }
                if (c == '+') { tokens.add(new Token(TokenType.OPERATOR, "+")); pos++; continue; }
                if (c == '-') { tokens.add(new Token(TokenType.OPERATOR, "-")); pos++; continue; }
                if (c == '*') { tokens.add(new Token(TokenType.OPERATOR, "*")); pos++; continue; }
                if (c == '/') { tokens.add(new Token(TokenType.OPERATOR, "/")); pos++; continue; }
                if (c == '%') { tokens.add(new Token(TokenType.OPERATOR, "%")); pos++; continue; }
                if (c == '&') { tokens.add(new Token(TokenType.OPERATOR, "&")); pos++; continue; }
                if (c == '|') { tokens.add(new Token(TokenType.OPERATOR, "|")); pos++; continue; }
                if (c == '^') { tokens.add(new Token(TokenType.OPERATOR, "^")); pos++; continue; }
                if (c == '~') { tokens.add(new Token(TokenType.OPERATOR, "~")); pos++; continue; }

                // Multi-character shift operators (<<, >>, >>>)
                if (c == '<' && pos + 1 < input.length() && input.charAt(pos + 1) == '<') {
                    tokens.add(new Token(TokenType.OPERATOR, "<<"));
                    pos += 2;
                    continue;
                }
                if (c == '>') {
                    if (pos + 2 < input.length() && input.charAt(pos + 1) == '>' && input.charAt(pos + 2) == '>') {
                        tokens.add(new Token(TokenType.OPERATOR, ">>>"));
                        pos += 3;
                        continue;
                    } else if (pos + 1 < input.length() && input.charAt(pos + 1) == '>') {
                        tokens.add(new Token(TokenType.OPERATOR, ">>"));
                        pos += 2;
                        continue;
                    }
                }

                throw new RuntimeException("Unexpected character in expression: " + c);
            }

            tokens.add(new Token(TokenType.EOF, ""));
            return tokens;
        }
    }

    // ==========================================
    // 2. PARSER & EVALUATOR (Recursive Descent)
    // ==========================================

    static class Parser {
        private final List<Token> tokens;
        private int pos = 0;

        public Parser(List<Token> tokens) {
            this.tokens = tokens;
        }

        private Token peek() {
            return tokens.get(pos);
        }

        private Token consume() {
            return tokens.get(pos++);
        }

        private boolean match(String op) {
            Token t = peek();
            if (t.type == TokenType.OPERATOR && t.text.equals(op)) {
                pos++;
                return true;
            }
            return false;
        }

        public int parse() {
            int result = parseNandNorXnor();
            if (peek().type != TokenType.EOF) {
                throw new RuntimeException("Unexpected operator or value: " + peek().text);
            }
            return result;
        }

        // Precedence Level 0: NAND, NOR, XNOR (Lowest Precedence)
        private int parseNandNorXnor() {
            int left = parseOr();
            while (true) {
                if (match("NAND")) {
                    int right = parseOr();
                    left = ~(left & right);
                } else if (match("NOR")) {
                    int right = parseOr();
                    left = ~(left | right);
                } else if (match("XNOR")) {
                    int right = parseOr();
                    left = ~(left ^ right);
                } else {
                    break;
                }
            }
            return left;
        }

        // Precedence Level 1: OR, |
        private int parseOr() {
            int left = parseXor();
            while (true) {
                if (match("OR") || match("|")) {
                    int right = parseXor();
                    left = left | right;
                } else {
                    break;
                }
            }
            return left;
        }

        // Precedence Level 2: XOR, ^
        private int parseXor() {
            int left = parseAnd();
            while (true) {
                if (match("XOR") || match("^")) {
                    int right = parseAnd();
                    left = left ^ right;
                } else {
                    break;
                }
            }
            return left;
        }

        // Precedence Level 3: AND, &
        private int parseAnd() {
            int left = parseShift();
            while (true) {
                if (match("AND") || match("&")) {
                    int right = parseShift();
                    left = left & right;
                } else {
                    break;
                }
            }
            return left;
        }

        // Precedence Level 4: Shift operators (<<, >>, >>>)
        private int parseShift() {
            int left = parseAddSub();
            while (true) {
                if (match("<<")) {
                    int right = parseAddSub();
                    left = left << right;
                } else if (match(">>")) {
                    int right = parseAddSub();
                    left = left >> right;
                } else if (match(">>>")) {
                    int right = parseAddSub();
                    left = left >>> right;
                } else {
                    break;
                }
            }
            return left;
        }

        // Precedence Level 5: Addition and Subtraction (+, -)
        private int parseAddSub() {
            int left = parseMulDivMod();
            while (true) {
                if (match("+")) {
                    int right = parseMulDivMod();
                    left = left + right;
                } else if (match("-")) {
                    int right = parseMulDivMod();
                    left = left - right;
                } else {
                    break;
                }
            }
            return left;
        }

        // Precedence Level 6: Multiplication, Division, Modulus (*, /, %)
        private int parseMulDivMod() {
            int left = parseUnary();
            while (true) {
                if (match("*")) {
                    int right = parseUnary();
                    left = left * right;
                } else if (match("/")) {
                    int right = parseUnary();
                    if (right == 0) {
                        throw new ArithmeticException("Division by zero");
                    }
                    left = left / right;
                } else if (match("%")) {
                    int right = parseUnary();
                    if (right == 0) {
                        throw new ArithmeticException("Division by zero");
                    }
                    left = left % right;
                } else {
                    break;
                }
            }
            return left;
        }

        // Precedence Level 7: Unary operators (-, NOT, ~)
        private int parseUnary() {
            if (match("-")) {
                return -parseUnary();
            } else if (match("NOT") || match("~")) {
                return ~parseUnary();
            }
            return parsePrimary();
        }

        // Precedence Level 8: Primary (Numbers, Parentheses) (Highest Precedence)
        private int parsePrimary() {
            Token t = peek();
            if (t.type == TokenType.NUMBER) {
                consume();
                try {
                    return Integer.parseInt(t.text);
                } catch (NumberFormatException e) {
                    throw new RuntimeException("Invalid number: " + t.text);
                }
            } else if (t.type == TokenType.LPAREN) {
                consume();
                int val = parseNandNorXnor();
                if (peek().type != TokenType.RPAREN) {
                    throw new RuntimeException("Missing closing parenthesis");
                }
                consume(); // consume ')'
                return val;
            } else if (t.type == TokenType.EOF) {
                throw new RuntimeException("Unexpected end of expression");
            } else {
                throw new RuntimeException("Unexpected operator or value: " + t.text);
            }
        }
    }

    // ==========================================
    // 3. MAIN APPLICATION & CONSOLE INTERFACE
    // ==========================================

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("==================================================");
        System.out.println("   ADVANCED BITWISE & ARITHMETIC EXPRESSION CALCULATOR");
        System.out.println("==================================================");
        System.out.println("Type any mathematical or bitwise expression.");
        System.out.println("Type 'clear' to reset, or 'exit' to quit.\n");

        while (true) {
            System.out.print("Enter expression: ");
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("exit")) {
                System.out.println("Exiting calculator. Goodbye!");
                break;
            }

            if (input.equalsIgnoreCase("clear")) {
                System.out.println("[Calculator cleared]\n");
                continue;
            }

            if (input.isEmpty()) {
                continue;
            }

            try {
                // Step 1: Tokenize
                Lexer lexer = new Lexer(input);
                List<Token> tokens = lexer.tokenize();

                // Step 2: Parse & Evaluate
                Parser parser = new Parser(tokens);
                int result = parser.parse();

                // Step 3: Display formatted results
                printResults(input, result);

            } catch (ArithmeticException e) {
                System.out.println("Error: " + e.getMessage() + "\n");
            } catch (RuntimeException e) {
                System.out.println("Error: " + e.getMessage() + "\n");
            }
        }

        scanner.close();
    }

    // ==========================================
    // 4. FORMATTING UTILITIES
    // ==========================================

    public static void printResults(String expression, int result) {
        System.out.println("\n====================================");
        System.out.println("Expression: " + expression);
        System.out.println("Decimal:    " + result);
        System.out.println("Binary:     " + format32BitBinary(result));
        System.out.println("Hex:        0x" + Integer.toHexString(result).toUpperCase());
        System.out.println("====================================\n");
    }

    public static String format32BitBinary(int value) {
        String binary = Integer.toBinaryString(value);
        if (binary.length() < 32) {
            StringBuilder sb = new StringBuilder();
            while (sb.length() + binary.length() < 32) {
                sb.append('0');
            }
            sb.append(binary);
            binary = sb.toString();
        }
        // Group into 4-bit nibbles for readability
        return binary.replaceAll("(.{4})", "$1 ").trim();
    }
}