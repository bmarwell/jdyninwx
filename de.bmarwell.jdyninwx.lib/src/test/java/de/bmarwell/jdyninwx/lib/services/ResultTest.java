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

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class ResultTest {

    @Test
    void cannot_set_neither() {
        assertThatThrownBy(() -> new Result<>(null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("none");
    }

    @Test
    void cannot_set_both() {
        assertThatThrownBy(() -> new Result<>(new Object(), new Throwable()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("both");
    }

    @Test
    void cannot_set_null_success() {
        assertThatThrownBy(() -> Result.ok(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    void cannot_set_null_fail() {
        assertThatThrownBy(() -> Result.fail((Throwable) null)).isInstanceOf(NullPointerException.class);
    }
}
