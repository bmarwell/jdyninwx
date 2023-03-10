/*
 * Copyright (C) 2023.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.bmarwell.jdyninwx.lib.services;

import java.util.Objects;
import java.util.stream.Stream;

public record Result<T>(T success, Throwable error) {

    public Result {
        if (success != null && error != null) {
            throw new IllegalArgumentException("Cannot set both success and error!");
        }
        if (success == null && error == null) {
            throw new IllegalArgumentException("Cannot set none of success and error!");
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
        return new Result<>(Objects.requireNonNull(success));
    }

    static <T> Result<T> fail(Throwable error) {
        return new Result<>(Objects.requireNonNull(error));
    }

    public Stream<T> stream() {
        return Stream.of(success());
    }
}
