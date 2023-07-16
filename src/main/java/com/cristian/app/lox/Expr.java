package com.cristian.app.lox;

import java.util.List;

public abstract class Expr {

    interface Visitor<R> {

        R visitAssignExpr(Assign expr);

        R visitLogicalExpr(Logical expr);

        R visitBinaryExpr(Binary expr);

        R visitCallExpr(Call expr);

        R visitGroupingExpr(Grouping expr);

        R visitLiteralExpr(Literal expr);

        R visitUnaryExpr(Unary expr);

        R visitVariableExpr(Variable expr);

        R visitClassExpr(Class expr);

        R visitGetExpr(Get expr);

        R visitSetExpr(Set expr);
    }

    public static class Assign extends Expr {
        Assign( Token identifier,  Expr value) {
            this.identifier = identifier;
            this.value = value;
        }

        final  Token identifier;
        final  Expr value;

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitAssignExpr(this);
        }
    }

    public static class Logical extends Expr {
        Logical( Expr left,  Token operator,  Expr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        final  Expr left;
        final  Token operator;
        final  Expr right;

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitLogicalExpr(this);
        }
    }

    public static class Binary extends Expr {
        Binary( Expr left,  Token operator,  Expr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        final  Expr left;
        final  Token operator;
        final  Expr right;

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitBinaryExpr(this);
        }
    }

    public static class Call extends Expr {
        Call( Expr callee,  Token paren,  List<Expr> arguments) {
            this.callee = callee;
            this.paren = paren;
            this.arguments = arguments;
        }

        final  Expr callee;
        final  Token paren;
        final  List<Expr> arguments;

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitCallExpr(this);
        }
    }

    public static class Grouping extends Expr {
        Grouping( Expr expression) {
            this.expression = expression;
        }

        final  Expr expression;

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitGroupingExpr(this);
        }
    }

    public static class Literal extends Expr {
        Literal( Object value) {
            this.value = value;
        }

        final  Object value;

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitLiteralExpr(this);
        }
    }

    public static class Unary extends Expr {
        Unary( Token operator,  Expr right) {
            this.operator = operator;
            this.right = right;
        }

        final  Token operator;
        final  Expr right;

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitUnaryExpr(this);
        }
    }

    public static class Variable extends Expr {
        Variable( Token identifier) {
            this.identifier = identifier;
        }

        final  Token identifier;

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitVariableExpr(this);
        }
    }

    public static class Class extends Expr {
        Class( Token identifier,  List<Expr> arguments) {
            this.identifier = identifier;
            this.arguments = arguments;
        }

        final  Token identifier;
        final  List<Expr> arguments;

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitClassExpr(this);
        }
    }

    public static class Get extends Expr {
        Get( Expr object,  Token identifier) {
            this.object = object;
            this.identifier = identifier;
        }

        final  Expr object;
        final  Token identifier;

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitGetExpr(this);
        }
    }

    public static class Set extends Expr {
        Set( Expr object,  Token identifier,  Expr value) {
            this.object = object;
            this.identifier = identifier;
            this.value = value;
        }

        final  Expr object;
        final  Token identifier;
        final  Expr value;

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitSetExpr(this);
        }
    }


    abstract <R> R accept(Visitor<R> visitor);
}
