package com.eldritch.invoken.util;

public class Pair<T, S> {
    public final T first;
    public final S second;
    
    public Pair(T first, S second) {
        this.first = first;
        this.second = second;
    }
    
    public T getFirst() {
        return first;
    }
    
    public S getSecond() {
        return second;
    }
    
    public static <T, S> Pair<T, S> of(T first, S second) {
        return new Pair<>(first, second);
    }
}
