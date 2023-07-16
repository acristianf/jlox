package com.cristian.app.lox;

public class AstPrinter implements Expr.Visitor<String> {
    public String print(Expr expr) {
        return expr.accept(this);
    }

    @Override
    public String visitAssignExpr(Expr.Assign expr) {
        return "(= " + expr.identifier.lexeme + " " + expr.value.accept(this) + ")";
    }

    @Override
    public String visitLogicalExpr(Expr.Logical expr) {
        return null;
    }

    @Override
    public String visitBinaryExpr(Expr.Binary expr) {
        return "(" + expr.operator.lexeme + " " +
                expr.left.accept(this) + " " +
                expr.right.accept(this) + ")";
    }

    @Override
    public String visitCallExpr(Expr.Call expr) {
        return null;
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

    @Override
    public String visitVariableExpr(Expr.Variable expr) {
        return "(var " + expr.identifier.lexeme + ")";
    }


    @Override
    public String visitClassExpr(Expr.Class expr) {
        return null;
    }

    @Override
    public String visitGetExpr(Expr.Get expr) {
        return null;
    }

    @Override
    public String visitSetExpr(Expr.Set expr) {
        return null;
    }
}
