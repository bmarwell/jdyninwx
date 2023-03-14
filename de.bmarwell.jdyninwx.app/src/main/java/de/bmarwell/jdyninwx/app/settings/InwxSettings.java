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

import de.bmarwell.jdyninwx.lib.services.InwxUpdateService;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

public record InwxSettings(
        String inwxUserName,
        char[] inwxPassword,
        Optional<URI> inwxApiEndpoint,
        List<RecordConfiguration> ipv4UpdateRecords,
        List<RecordConfiguration> ipv6UpdateRecords,
        List<URI> identPoolIpv4,
        List<URI> identPoolIpv6,
        Duration identConnectTimeout,
        Duration identRequestTimeout) {

    public InwxUpdateService.InwxCredentials getCredentials() {
        if (inwxUserName() == null || inwxUserName().isBlank()) {
            throw new IllegalStateException("No username configured!");
        }
        if (inwxPassword() == null || inwxPassword().length == 0) {
            throw new IllegalStateException("No password configured!");
        }
        return new InwxUpdateService.InwxCredentials(inwxUserName(), inwxPassword());
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", InwxSettings.class.getSimpleName() + "[", "]")
                .add("inwxUserName='" + inwxUserName + "'")
                .add("inwxPassword=" + "*".repeat(inwxPassword != null ? inwxPassword.length : 0))
                .add("inwxApiEndpoint=" + inwxApiEndpoint)
                .add("ipv4UpdateRecords=" + ipv4UpdateRecords)
                .add("ipv6UpdateRecords=" + ipv6UpdateRecords)
                .add("identPoolIpv4=" + identPoolIpv4)
                .add("identPoolIpv6=" + identPoolIpv6)
                .add("identConnectTimeout=" + identConnectTimeout)
                .add("identRequestTimeout=" + identRequestTimeout)
                .toString();
    }

    public record RecordConfiguration(int recordId, Duration ttl) {}
}
