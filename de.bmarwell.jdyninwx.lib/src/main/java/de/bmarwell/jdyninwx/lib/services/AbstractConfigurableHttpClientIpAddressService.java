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
import java.time.Duration;

/**
 * Default base implementation for {@code with*()}-methods.
 */
abstract class AbstractConfigurableHttpClientIpAddressService implements IpAddressService {

    @Serial
    private static final long serialVersionUID = -3790874923289555729L;

    private Duration requestTimeout = Duration.ofMillis(1500L);
    private Duration connectTimeout = Duration.ofMillis(500L);

    @SuppressWarnings("unchecked")
    @Override
    public <T extends IpAddressService> T withRequestTimeout(Duration timeout) {
        this.requestTimeout = timeout;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends IpAddressService> T withConnectTimeout(Duration timeout) {
        this.connectTimeout = timeout;
        return (T) this;
    }

    public Duration getRequestTimeout() {
        return requestTimeout;
    }

    public Duration getConnectTimeout() {
        return connectTimeout;
    }
}
