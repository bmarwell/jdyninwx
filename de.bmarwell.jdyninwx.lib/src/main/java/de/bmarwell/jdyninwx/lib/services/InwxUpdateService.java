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

import java.io.Serializable;
import java.net.InetAddress;
import java.net.URI;
import java.time.Duration;

public interface InwxUpdateService extends Serializable {

    record InwxCredentials(String username, char[] password) {}

    Result<?> updateRecord(int dnsRecordId, InetAddress newIp, int ttlSeconds);

    default Result<?> updateRecord(int dnsRecordId, InetAddress newIp) {
        return updateRecord(dnsRecordId, newIp, getDefaultTtlSeconds());
    }

    int getDefaultTtlSeconds();

    @SuppressWarnings("unchecked")
    default <T extends InwxUpdateService> T withDefaultTtl(Duration defaultTtl) {
        if (defaultTtl.toSeconds() > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Cannot exceed int values");
        }

        return (T) this;
    }

    <T extends InwxUpdateService> T withCredentials(InwxCredentials credentials);

    <T extends InwxUpdateService> T withApiEndpoint(URI apiEndpoint);
}
