package org.example;

import java.util.List;

public class LoxFunction implements LoxCallable {

    private final Stmt.Function declaration;
    private final Environment closure;
    private final boolean isInitializer;

    public LoxFunction(Stmt.Function declaration, Environment closure, boolean isInitializer) {
        this.isInitializer = isInitializer;
        this.declaration = declaration;
        this.closure = closure;
    }

    public Stmt.Function getDeclaration() {
        return declaration;
    }

    @Override
    public int arity() {
        return declaration.params.size();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        // use closure as parent environment
        Environment environment = new Environment(closure);
        for (int i =0; i < declaration.params.size(); i++) {
            String name = declaration.params.get(i).lexeme;
            Object value = arguments.get(i);
            environment.define(name, value);
        }

        try {
            interpreter.executeBlock(declaration.body, environment);
        } catch (Return returnValue) {
            if (isInitializer) {
                return closure.getAt(0, "this");
            }
            return returnValue.value;
        }

        // init() methods always return `this
        if (isInitializer) {
            return closure.getAt(0, "this");
        }

        return null;

    }

    @Override
    public String toString() {
        return "<fn " + declaration.name.lexeme + ">";
    }

    public LoxFunction bind(LoxInstance loxInstance) {
        Environment environment = new Environment(closure);
        environment.define("this", loxInstance);
        return new LoxFunction(declaration, environment, isInitializer);
    }
}
