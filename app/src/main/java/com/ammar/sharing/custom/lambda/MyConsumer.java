package com.ammar.sharing.custom.lambda;

import java.util.Objects;

@FunctionalInterface
public interface MyConsumer<T> {
    void accept(T p);
    default MyConsumer<T> andThen(MyConsumer<? super T> after) {
        Objects.requireNonNull(after);
        return (T t) -> { accept(t); after.accept(t); };
    }

}
