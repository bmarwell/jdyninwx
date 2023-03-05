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
import java.net.Inet4Address;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

/**
 * Provides methods to return this device‘s current IP addresses.
 */
public interface IpAddressService extends Serializable {

    default Optional<Inet4Address> getFirstResolvedInet4Address(List<URI> ipv4resolvers) {
        return ipv4resolvers.stream()
                .map(this::getInet4Address)
                .filter(Result::isSuccess)
                .flatMap(Result::stream)
                .findFirst();
    }

    Result<Inet4Address> getInet4Address(URI ipv4resolver);

    IpAddressService withRequestTimeout(Duration timeout);

    IpAddressService withConnectTimeout(Duration timeout);
}
