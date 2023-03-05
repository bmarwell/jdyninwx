package de.bmarwell.jdyninwx.lib.services;

import java.util.stream.Stream;

record Result<T>(T success, Throwable error) {

    Result {
        if (success() != null && error() != null) {
            throw new IllegalStateException("Cannot set both success and error!");
        }
    }

    private Result(T success) {
        this(success, null);
    }

    private Result(Throwable error) {
        this(null, error);
    }

    public boolean isSuccess() {
        return this.success != null && this.error == null;
    }

    public boolean isError() {
        return this.error != null && this.success == null;
    }

    static <T> Result<T> ok(T success) {
        return new Result<>(success);
    }

    static <T> Result<T> fail(Throwable error) {
        return new Result<>(error);
    }

    public Stream<T> stream() {
        return Stream.of(success());
    }
}
