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
package de.bmarwell.jdyninwx.app.settings;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

public record InwxSettings(
        String inwxUserName,
        char[] inwxPassword,
        Optional<URI> inwxApiEndpoint,
        List<RecordConfiguration> ipv4UpdateRecords,
        List<RecordConfiguration> ipv6UpdateRecords,
        List<URI> identPoolIpv4,
        List<URI> identPoolIpv6,
        Optional<Duration> identConnectTimeout,
        Optional<Duration> identRequestTimeout) {

    public record RecordConfiguration(int recordId, Duration ttl) {}
}
