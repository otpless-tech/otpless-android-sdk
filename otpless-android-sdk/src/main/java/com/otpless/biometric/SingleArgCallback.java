package com.otpless.biometric;

@FunctionalInterface
public interface SingleArgCallback<T> {
    void invoke(T arg);
}
