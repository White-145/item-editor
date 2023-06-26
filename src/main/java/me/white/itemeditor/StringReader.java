package me.white.itemeditor;

public class StringReader {
    final private String STR;
    final private int LENGTH;
    private int i;

    public StringReader(String str) {
        STR = str;
        LENGTH = str.length();
    }

    public boolean canRead() {
        return i >= 0 && i < LENGTH;
    }

    private void next() {
        i += 1;
    }

    private void next(int i) {
        this.i += i;
    }

    public char peek() {
        if (!canRead()) return '\0';
        return STR.charAt(i);
    }

    public char peek(int i) {
        this.i += i;
        char ch = peek();
        this.i -= i;
        return ch;
    }

    public String peeks(int i) {
        StringBuilder sb = new StringBuilder();
        for (int j = 0; j < i; ++j) {
            if (!canRead()) break;
            sb.append(peek(j));
        }
        return sb.toString();
    }

    public void skip() {
        next();
    }

    public void skip(int i) {
        next(i);
    }

    public char read() {
        char result = peek();
        next();
        return result;
    }

    public int peekHex() {
        char ch = peek();
        if (!((ch >= '0' && ch <= '9') || (ch >= 'a' && ch <= 'f') || (ch >= 'A' && ch <= 'F'))) {
            // error
        }
        if (ch >= '0' && ch <= '9') return ch - '0';
        if (ch >= 'a' && ch <= 'f') return ch - 'a' + 10;
        if (ch >= 'A' && ch <= 'F') return ch - 'A' + 10;
        return 0;
    }

    public int readHex() {
        int result = peekHex();
        skip();
        return result;
    }

    public boolean isHex() {
        char ch = Character.toLowerCase(peek());
        return (ch >= '0' && ch <= '9') || (ch >= 'a' && ch <= 'f');
    }

    public String readEscaped() {
        char result;
        if (peek() == '\\') skip();
        switch (peek()) {
            case 'n':
                skip();
                return "\n";
            case 'u':
                skip();
                result = 0;
                for (int i = 0; i < 4; ++i) {
                    if (!isHex()) {
                        this.i -= i;
                        return "\\u";
                    }
                    result += Math.pow(16, 3 - i) * readHex();
                }
                return String.valueOf(result);
            case 'x':
                skip();
                result = 0;
                for (int i = 0; i < 2; ++i) {
                    if (!isHex()) {
                        this.i -= i;
                        return "\\x";
                    }
                    result += Math.pow(16, 1 - i) * readHex();
                }
                return String.valueOf(result);
            case '\\':
                return String.valueOf(read());
            case '&':
                skip();
                return "&";
            default:
                if (canRead()) return "\\" + String.valueOf(read());
                return "\\";
        }
    }
}
