package com.cristian.app.lox;

public class AstPrinter implements Expr.Visitor<String> {
    public String print(Expr expr) {
        return expr.accept(this);
    }

    @Override
    public String visitBinaryExpr(Expr.Binary expr) {
        return "(" + expr.operator.lexeme + " " +
                expr.left.accept(this) + " " +
                expr.right.accept(this) + ")";
    }

    @Override
    public String visitGroupingExpr(Expr.Grouping expr) {
        return "(group " + expr.expression.accept(this) + ")";
    }

    @Override
    public String visitLiteralExpr(Expr.Literal expr) {
        if (expr.value == null) return "nil";
        return expr.value.toString();
    }

    @Override
    public String visitUnaryExpr(Expr.Unary expr) {
        return "(" + expr.operator.lexeme + " " + expr.right.accept(this) + ")";
    }
}
