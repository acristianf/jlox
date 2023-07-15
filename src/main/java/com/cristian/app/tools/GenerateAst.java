package com.cristian.app.tools;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class GenerateAst {
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("USAGE: generate_ast <output dir>");
            System.exit(64);
        }
        String outputDir = args[0];
        defineAst(outputDir, "Expr", Arrays.asList(
                "Assign : Token identifier, Expr value",
                "Logical : Expr left, Token operator, Expr right",
                "Binary : Expr left, Token operator, Expr right",
                "Grouping : Expr expression",
                "Literal : Object value",
                "Unary : Token operator, Expr right",
                "Variable : Token identifier",
                "Function : Token identifier, List<Expr> arguments"
        ));
        defineAst(outputDir, "Stmt", Arrays.asList(
                "Block : List<Stmt> statements",
                "If : Expr condition, Stmt thenBranch, Stmt elseBranch",
                "While : Expr condition, Stmt body",
                "Break : Token breakToken",
                "Expression : Expr expression",
                "Print : Expr expression",
                "Return : Token name, Expr initializer",
                "Var : Token identifier, Expr initializer",
                "Function : Token identifier, List<Token> params, Stmt body"
        ));
    }

    private static void defineAst(String outputDir, String baseName, List<String> types) throws IOException {
        String path = outputDir + "/" + baseName + ".java";
        PrintWriter writer = new PrintWriter(path, StandardCharsets.UTF_8);

        writer.println("package com.cristian.app.lox;");
        writer.println();
        writer.println("import java.util.List;");
        writer.println();
        writer.println("public abstract class " + baseName + " {");
        writer.println();
        defineVisitor(writer, baseName, types);
        writer.println();
        types.forEach(type -> {
            int colonIdx = type.indexOf(":");
            String typeName = type;
            String[] constructorParams = new String[0];
            if (colonIdx != -1) {
                typeName = type.substring(0, colonIdx - 1).trim();
                constructorParams = type.substring(colonIdx + 1, type.length()).split(",");
            }
            writer.println("    public static class " + typeName + " extends " + baseName + " {");
            writer.println("        " + typeName + "(" + String.join(", ", constructorParams) + ") {");
            for (String constructorParam : constructorParams) {
                String ident = constructorParam.split(" ")[2];
                writer.println("            this." + ident + " = " + ident + ";");
            }
            writer.println("        }");
            writer.println();
            for (String constructorParam : constructorParams) {
                writer.println("        final " + constructorParam + ";");
            }
            writer.println();
            writer.println("        @Override");
            writer.println("        <R> R accept(Visitor<R> visitor) {");
            writer.println("            return visitor.visit" + typeName + baseName + "(this);");
            writer.println("        }");
            writer.println("    }");
            writer.println();
        });
        writer.println();
        writer.println("    abstract <R> R accept(Visitor<R> visitor);");
        writer.println("}");
        writer.close();
    }

    private static void defineVisitor(PrintWriter writer, String baseName, List<String> types) {
        writer.println("    interface Visitor<R> {");
        for (String type : types) {
            writer.println();
            String typeName = type.split(":")[0].trim();
            writer.println("        R visit" + typeName + baseName + "(" + typeName + " " + baseName.toLowerCase() + ");");
        }
        writer.println("    }");
    }
}
