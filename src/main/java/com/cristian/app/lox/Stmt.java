package com.cristian.app.lox;

import java.util.List;

public abstract class Stmt {

    interface Visitor<R> {

        R visitBlockStmt(Block stmt);

        R visitIfStmt(If stmt);

        R visitWhileStmt(While stmt);

        R visitBreakStmt(Break stmt);

        R visitExpressionStmt(Expression stmt);

        R visitPrintStmt(Print stmt);

        R visitReturnStmt(Return stmt);

        R visitVarStmt(Var stmt);

        R visitFunctionStmt(Function stmt);

        R visitClassStmt(Class stmt);
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

    public static class If extends Stmt {
        If(Expr condition, Stmt thenBranch, Stmt elseBranch) {
            this.condition = condition;
            this.thenBranch = thenBranch;
            this.elseBranch = elseBranch;
        }

        final Expr condition;
        final Stmt thenBranch;
        final Stmt elseBranch;

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitIfStmt(this);
        }
    }

    public static class While extends Stmt {
        While(Expr condition, Stmt body) {
            this.condition = condition;
            this.body = body;
        }

        final Expr condition;
        final Stmt body;

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitWhileStmt(this);
        }
    }

    public static class Break extends Stmt {
        Break(Token breakToken) {
            this.breakToken = breakToken;
        }

        final Token breakToken;

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitBreakStmt(this);
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

    public static class Return extends Stmt {
        Return(Token name, Expr initializer) {
            this.name = name;
            this.initializer = initializer;
        }

        final Token name;
        final Expr initializer;

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitReturnStmt(this);
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

    public static class Function extends Stmt {
        Function(Token identifier, List<Token> params, Stmt body) {
            this.identifier = identifier;
            this.params = params;
            this.body = body;
        }

        final Token identifier;
        final List<Token> params;
        final Stmt body;

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitFunctionStmt(this);
        }
    }

    public static class Class extends Stmt {
        Class(Token identifier, List<Stmt.Function> methods) {
            this.identifier = identifier;
            this.methods = methods;
        }

        final Token identifier;
        final List<Stmt.Function> methods;

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitClassStmt(this);
        }
    }


    abstract <R> R accept(Visitor<R> visitor);
}
