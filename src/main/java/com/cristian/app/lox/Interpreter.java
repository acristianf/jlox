package com.cristian.app.lox;


import com.cristian.app.Lox;

import java.util.List;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {
    private Environment enviroment = new Environment();

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
        enviroment.assign(expr.identifier, value);
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
                    return (String) leftValue + (String) rightValue;
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
        return enviroment.get(expr.identifier);
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

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        Environment previous = enviroment;
        try {
            this.enviroment = new Environment(previous);
            stmt.statements.forEach(s -> s.accept(this));
        } finally {
            this.enviroment = previous;
        }
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
    public Void visitVarStmt(Stmt.Var stmt) {
        Object value = null;
        if (stmt.initializer != null) {
            value = stmt.initializer.accept(this);
        }
        enviroment.define(stmt.identifier.lexeme, value);
        return null;
    }
}
