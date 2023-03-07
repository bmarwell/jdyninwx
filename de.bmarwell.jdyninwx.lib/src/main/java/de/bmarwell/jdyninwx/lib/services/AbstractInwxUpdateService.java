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

import java.io.Serial;
import java.net.URI;
import java.time.Duration;
import java.util.Optional;

/**
 * Default implementation for {@code with*()}-methods.
 */
abstract class AbstractInwxUpdateService implements InwxUpdateService {

    @Serial
    private static final long serialVersionUID = -3181748065886503025L;

    private int defaultTtlSeconds = 300;

    private Optional<InwxCredentials> credentials = Optional.empty();

    private URI apiEndpoint = URI.create("https://api.domrobot.com/xmlrpc");

    @Override
    public int getDefaultTtlSeconds() {
        return defaultTtlSeconds;
    }

    protected Optional<InwxCredentials> getCredentials() {
        return credentials;
    }

    protected URI getApiEndpoint() {
        return apiEndpoint;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends InwxUpdateService> T withDefaultTtl(Duration defaultTtl) {
        InwxUpdateService.super.withDefaultTtl(defaultTtl);
        this.defaultTtlSeconds = Math.toIntExact(defaultTtl.toSeconds());

        return (T) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends InwxUpdateService> T withCredentials(InwxCredentials credentials) {
        this.credentials = Optional.ofNullable(credentials);

        return (T) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends InwxUpdateService> T withApiEndpoint(URI apiEndpoint) {
        this.apiEndpoint = apiEndpoint;
        return (T) this;
    }
}
