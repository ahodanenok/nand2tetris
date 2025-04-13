package ahodanenok.nand2tetris.jack;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JackTokenizerTest {

    @ParameterizedTest
    @CsvSource(textBlock = """
        {,             LEFT_BRACE
        },             RIGHT_BRACE
        (,             LEFT_PAREN
        ),             RIGHT_PAREN
        [,             LEFT_BRACKET
        ],             RIGHT_BRACKET
        .,             DOT
        ',',           COMMA
        ;,             SEMICOLON
        +,             PLUS
        -,             MINUS
        *,             STAR
        /,             SLASH
        &,             AMPERSAND
        |,             VBAR
        <,             LEFT_ANGLE
        >,             RIGHT_ANGLE
        =,             EQUAL
        ~,             TILDE,
        class,         CLASS
        constructor,   CONSTRUCTOR
        function,      FUNCTION
        method,        METHOD
        field,         FIELD
        static,        STATIC
        var,           VAR
        int,           INT
        char,          CHAR
        boolean,       BOOLEAN
        void,          VOID
        true,          TRUE
        false,         FALSE
        null,          NULL
        this,          THIS
        let,           LET
        do,            DO
        if,            IF
        else,          ELSE
        while,         WHILE
        return,        RETURN
        """)
    public void testReadToken_Generic(String code, TokenType tokenType) {
        JackTokenizer tokenizer = new JackTokenizer(code);
        checkToken(tokenizer, tokenType, code);
        checkNoMoreTokens(tokenizer);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "a", "X", "_foo", "x_y_z", "hello", "camelCase"
    })
    public void testReadToken_Identifier(String code) {
        JackTokenizer tokenizer = new JackTokenizer(code);
        checkToken(tokenizer, TokenType.IDENTIFIER, code);
        checkNoMoreTokens(tokenizer);
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
        "", ''
        "123", '123'
        '"hello, world"', 'hello, world'
        "UPPERCASE", 'UPPERCASE'
        "a b c d e", 'a b c d e'
        """)
    public void testReadToken_String(String code, String str) {
        JackTokenizer tokenizer = new JackTokenizer(code);
        checkStringToken(tokenizer, TokenType.STRING, code, str);
        checkNoMoreTokens(tokenizer);
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
        0, 0
        1, 1,
        567, 567,
        1234, 1234
        32767, 32767
        """)
    public void testReadToken_Integer(String code, int n) {
        JackTokenizer tokenizer = new JackTokenizer(code);
        checkIntegerToken(tokenizer, TokenType.INTEGER, code, n);
        checkNoMoreTokens(tokenizer);
    }

    @Test
    public void testReadChunk_1() {
        JackTokenizer tokenizer = new JackTokenizer("""
            (y + size) < 254) & ((x + size) < 510
        """);
        checkToken(tokenizer, TokenType.LEFT_PAREN, "(");
        checkToken(tokenizer, TokenType.IDENTIFIER, "y");
        checkToken(tokenizer, TokenType.PLUS, "+");
        checkToken(tokenizer, TokenType.IDENTIFIER, "size");
        checkToken(tokenizer, TokenType.RIGHT_PAREN, ")");
        checkToken(tokenizer, TokenType.LEFT_ANGLE, "<");
        checkIntegerToken(tokenizer, TokenType.INTEGER, "254", 254);
        checkToken(tokenizer, TokenType.RIGHT_PAREN, ")");
        checkToken(tokenizer, TokenType.AMPERSAND, "&");
        checkToken(tokenizer, TokenType.LEFT_PAREN, "(");
        checkToken(tokenizer, TokenType.LEFT_PAREN, "(");
        checkToken(tokenizer, TokenType.IDENTIFIER, "x");
        checkToken(tokenizer, TokenType.PLUS, "+");
        checkToken(tokenizer, TokenType.IDENTIFIER, "size");
        checkToken(tokenizer, TokenType.RIGHT_PAREN, ")");
        checkToken(tokenizer, TokenType.LEFT_ANGLE, "<");
        checkIntegerToken(tokenizer, TokenType.INTEGER, "510", 510);
        checkNoMoreTokens(tokenizer);
    }

    @Test
    public void testReadChunk_2() {
        JackTokenizer tokenizer = new JackTokenizer("""
            field int x, y; // screen location of the square's top-left corner
            field int size; // length of this square, in pixels
        """);
        checkToken(tokenizer, TokenType.FIELD, "field");
        checkToken(tokenizer, TokenType.INT, "int");
        checkToken(tokenizer, TokenType.IDENTIFIER, "x");
        checkToken(tokenizer, TokenType.COMMA, ",");
        checkToken(tokenizer, TokenType.IDENTIFIER, "y");
        checkToken(tokenizer, TokenType.SEMICOLON, ";");
        checkToken(tokenizer, TokenType.FIELD, "field");
        checkToken(tokenizer, TokenType.INT, "int");
        checkToken(tokenizer, TokenType.IDENTIFIER, "size");
        checkToken(tokenizer, TokenType.SEMICOLON, ";");
        checkNoMoreTokens(tokenizer);
    }

    @Test
    public void testReadChunk_3() {
        JackTokenizer tokenizer = new JackTokenizer("""
            /** Runs the game: handles the user's inputs and moves the square accordingly */
            method void run() {
                /* the key currently pressed by the user */
                var char key;
        """);
        checkToken(tokenizer, TokenType.METHOD, "method");
        checkToken(tokenizer, TokenType.VOID, "void");
        checkToken(tokenizer, TokenType.IDENTIFIER, "run");
        checkToken(tokenizer, TokenType.LEFT_PAREN, "(");
        checkToken(tokenizer, TokenType.RIGHT_PAREN, ")");
        checkToken(tokenizer, TokenType.LEFT_BRACE, "{");
        checkToken(tokenizer, TokenType.VAR, "var");
        checkToken(tokenizer, TokenType.CHAR, "char");
        checkToken(tokenizer, TokenType.IDENTIFIER, "key");
        checkToken(tokenizer, TokenType.SEMICOLON, ";");
        checkNoMoreTokens(tokenizer);
    }

    private void checkToken(JackTokenizer tokenizer, TokenType tokenType, String tokenValue) {
        assertTrue(tokenizer.advance());
        assertEquals(tokenType, tokenizer.tokenType());
        assertEquals(tokenValue, tokenizer.tokenValue());
        assertEquals(-1, tokenizer.integer());
        assertNull(tokenizer.string());
    }

    private void checkStringToken(JackTokenizer tokenizer, TokenType tokenType, String tokenValue, String str) {
        assertTrue(tokenizer.advance());
        assertEquals(tokenType, tokenizer.tokenType());
        assertEquals(tokenValue, tokenizer.tokenValue());
        assertEquals(-1, tokenizer.integer());
        assertEquals(str, tokenizer.string());
    }

    private void checkIntegerToken(JackTokenizer tokenizer, TokenType tokenType, String tokenValue, int n) {
        assertTrue(tokenizer.advance());
        assertEquals(tokenType, tokenizer.tokenType());
        assertEquals(tokenValue, tokenizer.tokenValue());
        assertEquals(n, tokenizer.integer());
        assertNull(tokenizer.string());
    }

    private void checkNoMoreTokens(JackTokenizer tokenizer) {
        assertFalse(tokenizer.advance());
        assertNull(tokenizer.tokenType());
        assertNull(tokenizer.tokenValue());
        assertEquals(-1, tokenizer.integer());
        assertNull(tokenizer.string());
    }
}
