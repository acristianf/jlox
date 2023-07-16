package com.cristian.app.lox;


import com.cristian.app.Lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {
    private Environment environment = new Environment();
    final Environment globals = environment;
    private final Map<Expr, Integer> locals = new HashMap<>();

    public void resolve(Expr expr, int depth) {
        locals.put(expr, depth);
    }

    private static class BreakException extends RuntimeException {
    }

    public void interpret(List<Stmt> statements) {
        try {
            statements.forEach(stmt -> stmt.accept(this));
        } catch (RuntimeError error) {
            Lox.runtimeError(error);
        }
    }

    private String stringify(Object value) {
        if (value == null) return "nil";
        if (value instanceof Double) {
            String text = value.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }
        return value.toString();
    }

    @Override
    public Object visitAssignExpr(Expr.Assign expr) {
        Object value = null;
        if (expr.value != null) {
            value = expr.value.accept(this);
        }
        Integer distance = locals.get(expr);
        if (distance != null) {
            environment.assignAt(distance, expr.identifier, value);
        } else {
            globals.assign(expr.identifier, value);
        }
        return value;
    }

    @Override
    public Object visitLogicalExpr(Expr.Logical expr) {
        Object left = expr.left.accept(this);
        if (expr.operator.type == TokenType.OR) {
            if (isTruthy(left)) return left;
        } else {
            if (!isTruthy(left)) return left;
        }
        return expr.right.accept(this);
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        Object leftValue = expr.left.accept(this);
        Object rightValue = expr.right.accept(this);
        Token operator = expr.operator;
        switch (operator.type) {
            case PLUS -> {
                if (leftValue instanceof Double && rightValue instanceof Double) {
                    return (Double) leftValue + (Double) rightValue;
                } else if (leftValue instanceof String && rightValue instanceof String) {
                    return leftValue + (String) rightValue;
                }
                throw new RuntimeError(operator, "Operands must be String or Numbers");
            }
            case MINUS -> {
                checkNumberOperands(operator, leftValue, rightValue);
                return (Double) leftValue - (Double) rightValue;
            }
            case STAR -> {
                checkNumberOperands(operator, leftValue, rightValue);
                return (Double) leftValue * (Double) rightValue;
            }
            case SLASH -> {
                checkNumberOperands(operator, leftValue, rightValue);
                if ((Double) rightValue == 0) throw new RuntimeError(operator, "Can't divide by zero.");
                return (Double) leftValue / (Double) rightValue;
            }
            case LESS -> {
                checkNumberOperands(operator, leftValue, rightValue);
                return (Double) leftValue < (Double) rightValue;
            }
            case LESS_EQUAL -> {
                checkNumberOperands(operator, leftValue, rightValue);
                return (Double) leftValue <= (Double) rightValue;
            }
            case GREATER -> {
                checkNumberOperands(operator, leftValue, rightValue);
                return (Double) leftValue > (Double) rightValue;
            }
            case GREATER_EQUAL -> {
                checkNumberOperands(operator, leftValue, rightValue);
                return (Double) leftValue >= (Double) rightValue;
            }
        }
        if (operator.type == TokenType.EQUAL_EQUAL) {
            return isEqual(leftValue, rightValue);
        } else if (operator.type == TokenType.BANG_EQUAL) {
            return !isEqual(leftValue, rightValue);
        }
        return null;
    }

    @Override
    public Object visitCallExpr(Expr.Call expr) {
        Object callee = expr.callee.accept(this);
        List<Object> arguments = new ArrayList<>();
        expr.arguments.forEach(a -> arguments.add(a.accept(this)));
        LoxCallable function = (LoxCallable) callee;
        if (arguments.size() != function.arity()) {
            throw new RuntimeError(expr.paren, "Expected " + function.arity() + " arguments but got "
                    + arguments.size() + ".");
        }
        return function.call(this, arguments);
    }

    private void checkNumberOperands(Token operator, Object leftValue, Object rightValue) {
        if (leftValue instanceof Double && rightValue instanceof Double) return;
        throw new RuntimeError(operator, "Operands must be both Numbers");
    }

    private boolean isEqual(Object leftValue, Object rightValue) {
        if (leftValue == null && rightValue == null) return true;
        if (leftValue == null) return false;
        return leftValue.equals(rightValue);
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return expr.expression.accept(this);
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        TokenType operator = expr.operator.type;
        Object obj = expr.right.accept(this);
        if (operator == TokenType.BANG) {
            return !isTruthy(obj);
        } else if (operator == TokenType.MINUS) {
            checkNumberOperand(expr.operator, obj);
            return -(Double) obj;
        }
        return null;
    }

    @Override
    public Object visitVariableExpr(Expr.Variable expr) {
        return lookUpVariable(expr.identifier, expr);
    }

    private Object lookUpVariable(Token identifier, Expr expr) {
        Integer distance = locals.get(expr);
        if (distance != null) {
            return environment.getAt(distance, identifier.lexeme);
        } else {
            return globals.get(identifier);
        }
    }

    @Override
    public Object visitClassExpr(Expr.Class expr) {
        LoxClass klass = (LoxClass) environment.get(expr.identifier);
        LoxInstance instance = new LoxInstance(klass);
        Object constructor = instance.klass.findMethod(expr.identifier.lexeme);
        if (constructor instanceof Func c) {
            List<Object> args = new ArrayList<>();
            expr.arguments.forEach(a -> args.add(a.accept(this)));
            if (args.size() != c.arity()) {
                throw new RuntimeError(expr.identifier, "Class constructor expected " + c.arity() + " arguments but got " +
                        args.size() + " instead.");
            }
            c.call(this, args);
        }
        return instance;
    }

    @Override
    public Object visitGetExpr(Expr.Get expr) {
        Object obj = expr.object.accept(this);
        if (obj instanceof LoxInstance) {
            return ((LoxInstance) obj).get(expr.identifier);
        }
        throw new RuntimeError(expr.identifier, "Only instances have properties.");
    }

    @Override
    public Object visitSetExpr(Expr.Set expr) {
        Object obj = expr.object.accept(this);
        if (!(obj instanceof LoxInstance)) {
            throw new RuntimeError(expr.identifier, "Only instances have fields");
        }
        Object value = expr.value.accept(this);
        ((LoxInstance) obj).set(expr.identifier, value);
        return value;
    }

    @Override
    public Object visitThisExpr(Expr.This expr) {
        return lookUpVariable(expr.keyword, expr);
    }

    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double) return;
        throw new RuntimeError(operator, "Operand must be a number");
    }

    private boolean isTruthy(Object obj) {
        if (obj == null) return false;
        if (obj instanceof Boolean b) return b;
        return true;
    }

    public void executeBlock(List<Stmt> block, Environment environment) {
        Environment previous = this.environment;
        try {
            this.environment = environment;
            block.forEach(s -> s.accept(this));
        } finally {
            this.environment = previous;
        }
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        stmt.statements.forEach(s -> s.accept(this));
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        Object value = stmt.condition.accept(this);
        if (isTruthy(value)) {
            stmt.thenBranch.accept(this);
        } else if (stmt.elseBranch != null) {
            stmt.elseBranch.accept(this);
        }
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        try {
            while (isTruthy(stmt.condition.accept(this))) {
                stmt.body.accept(this);
            }
        } catch (BreakException ignored) {

        }
        return null;
    }

    @Override
    public Void visitBreakStmt(Stmt.Break stmt) {
        throw new BreakException();
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        stmt.expression.accept(this);
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        Object value = stmt.expression.accept(this);
        System.out.println(stringify(value));
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        Object value = null;
        if (stmt.initializer != null) value = stmt.initializer.accept(this);
        throw new Return(value);
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        Object value = null;
        if (stmt.initializer != null) {
            value = stmt.initializer.accept(this);
        }
        environment.define(stmt.identifier.lexeme, value);
        return null;
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt) {
        environment.define(stmt.identifier.lexeme, new Func(stmt.identifier.lexeme, stmt.params, stmt.body, this.environment));
        return null;
    }

    @Override
    public Void visitClassStmt(Stmt.Class stmt) {
        environment.define(stmt.identifier.lexeme, null);
        Map<String, Func> methods = new HashMap<>();
        stmt.methods.forEach(method -> {
            Func function = new Func(method.identifier.lexeme, method.params, method.body, environment);
            methods.put(method.identifier.lexeme, function);
        });
        LoxClass klass = new LoxClass(stmt.identifier.lexeme, methods);
        environment.assign(stmt.identifier, klass);
        return null;
    }
}
