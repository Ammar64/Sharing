package com.ammar.sharing.custom.lambda;

@FunctionalInterface
public interface LambdaNoReturn<T> {
    void apply(T p1);
}