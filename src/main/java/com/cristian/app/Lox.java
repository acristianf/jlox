package com.cristian.app;

import com.cristian.app.lox.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Lox {
    private static boolean hadError;

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("USAGE: lox <source>");
            System.exit(64);
        } else if (args.length == 1) {
            runFile(args[0]);
        } else {
            runPrompt();
        }
    }

    private static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        for (; ; ) {
            System.out.print("> ");
            String line = reader.readLine();
            if (line == null) break;
            run(line);
        }
    }

    private static void runFile(String file) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(file));
        run(new String(bytes, Charset.defaultCharset()));
        if (hadError) System.exit(65);
    }

    private static void run(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scan();
        tokens.forEach(System.out::println);
        Parser parser = new Parser(tokens);
        Expr expr = parser.parse();
        if (hadError) return;
        System.out.println(new AstPrinter().print(expr));
    }

    public static void error(int line, String msg) {
        report(line, "", msg);
    }

    public static void error(Token token, String msg) {
        if (token.getType() == TokenType.EOF) {
            report(token.getLine(), " at end", msg);
        } else {
            report(token.getLine(), " at '" + token.getLexeme() + "'", msg);
        }
    }

    private static void report(int line, String where, String msg) {
        System.err.println("[line " + line + "] Error" + where + ":" + msg);
        hadError = true;
    }
}
