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
package de.bmarwell.jdyninwx.app;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

class InwxUpdaterTest {

    @Test
    void can_be_executed() throws IOException {
        // given
        var app = new InwxUpdater();

        // when
        new CommandLine(app).parseArgs("-s", "target/test-classes/de/bmarwell/jdyninwx/app/settings/full.properties");

        // then
        assertThat(app.getSettings())
                .matches(inwxSettings -> inwxSettings.identPoolIpv4().size() == 1);
    }
}
