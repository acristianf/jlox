package com.cristian.app.lox;

import com.cristian.app.Lox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class Resolver implements Expr.Visitor<Void>, Stmt.Visitor<Void> {
    private final Interpreter interpreter;
    private final Stack<Map<String, Boolean>> scopes = new Stack<>();
    private FunctionType currentFuntion = FunctionType.NONE;

    public Resolver(Interpreter interpreter) {
        this.interpreter = interpreter;
    }

    public void resolve(List<Stmt> statements) {
        statements.forEach(this::resolve);
    }

    private void resolve(Stmt s) {
        s.accept(this);
    }

    private void resolve(Expr e) {
        e.accept(this);
    }

    @Override
    public Void visitAssignExpr(Expr.Assign expr) {
        resolve(expr.value);
        resolveLocal(expr, expr.identifier);
        return null;
    }

    @Override
    public Void visitLogicalExpr(Expr.Logical expr) {
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitBinaryExpr(Expr.Binary expr) {
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitGroupingExpr(Expr.Grouping expr) {
        resolve(expr.expression);
        return null;
    }

    @Override
    public Void visitLiteralExpr(Expr.Literal expr) {
        return null;
    }

    @Override
    public Void visitUnaryExpr(Expr.Unary expr) {
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitVariableExpr(Expr.Variable expr) {
        if (!scopes.empty() && scopes.peek().get(expr.identifier.lexeme) == Boolean.FALSE) {
            Lox.error(expr.identifier, "Can't read local variable in its own initializer.");
        }
        resolveLocal(expr, expr.identifier);
        return null;
    }

    private void resolveLocal(Expr expr, Token identifier) {
        for (int i = scopes.size() - 1; i >= 0; i--) {
            if (scopes.get(i).containsKey(identifier.lexeme)) {
                interpreter.resolve(expr, scopes.size() - 1 - i);
                return;
            }
        }
    }

    @Override
    public Void visitFunctionExpr(Expr.Function expr) {
        expr.arguments.forEach(this::resolve);
        return null;
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        beginScope();
        resolve(stmt.statements);
        endScope();
        return null;
    }

    private void endScope() {
        scopes.pop();
    }

    private void beginScope() {
        scopes.push(new HashMap<>());
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        resolve(stmt.condition);
        resolve(stmt.thenBranch);
        if (stmt.elseBranch != null) resolve(stmt.elseBranch);
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        resolve(stmt.condition);
        resolve(stmt.body);
        return null;
    }

    @Override
    public Void visitBreakStmt(Stmt.Break stmt) {
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        resolve(stmt.expression);
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        resolve(stmt.expression);
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        if (currentFuntion == FunctionType.NONE) {
            Lox.error(stmt.name, "Can't return from top-level code.");
        }
        if (stmt.initializer != null) {
            resolve(stmt.initializer);
        }
        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        declare(stmt.identifier);
        if (stmt.initializer != null) {
            resolve(stmt.initializer);
        }
        define(stmt.identifier);
        return null;
    }

    private void define(Token identifier) {
        if (scopes.empty()) return;
        scopes.peek().put(identifier.lexeme, true);
    }

    private void declare(Token identifier) {
        if (scopes.empty()) return;
        Map<String, Boolean> scope = scopes.peek();
        if (scope.containsKey(identifier.lexeme)) {
            Lox.error(identifier, "Already a variable with this name in this scope.");
        }
        scope.put(identifier.lexeme, false);
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt) {
        declare(stmt.identifier);
        define(stmt.identifier);
        resolveFunction(stmt, FunctionType.FUNCTION);
        return null;
    }

    private void resolveFunction(Stmt.Function function, FunctionType functionType) {
        FunctionType enclosingFunction = currentFuntion;
        currentFuntion = functionType;
        beginScope();
        for (Token param : function.params) {
            declare(param);
            define(param);
        }
        resolve(function.body);
        endScope();
        currentFuntion = enclosingFunction;
    }
}
