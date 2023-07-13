package com.cristian.app.lox;

import java.util.List;

public abstract class Stmt {

    interface Visitor<R> {

        R visitBlockStmt(Block stmt);

        R visitExpressionStmt(Expression stmt);

        R visitPrintStmt(Print stmt);

        R visitVarStmt(Var stmt);
    }

    public static class Block extends Stmt {
        Block(List<Stmt> statements) {
            this.statements = statements;
        }

        final List<Stmt> statements;

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitBlockStmt(this);
        }
    }

    public static class Expression extends Stmt {
        Expression(Expr expression) {
            this.expression = expression;
        }

        final Expr expression;

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitExpressionStmt(this);
        }
    }

    public static class Print extends Stmt {
        Print(Expr expression) {
            this.expression = expression;
        }

        final Expr expression;

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitPrintStmt(this);
        }
    }

    public static class Var extends Stmt {
        Var(Token identifier, Expr initializer) {
            this.identifier = identifier;
            this.initializer = initializer;
        }

        final Token identifier;
        final Expr initializer;

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitVarStmt(this);
        }
    }


    abstract <R> R accept(Visitor<R> visitor);
}
