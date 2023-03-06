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

import static org.assertj.core.api.Assertions.assertThat;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.time.Duration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

class ApacheHttpClientIpAddressServiceTest {

    private static boolean SUPPORTS_IPV4 = false;

    private static boolean SUPPORTS_IPV6 = false;

    private static final URI IDENT_ME = URI.create("https://ident.me");

    private final ApacheHttpClientIpAddressService service =
            (ApacheHttpClientIpAddressService) new ApacheHttpClientIpAddressService()
                    .withConnectTimeout(Duration.ofMillis(1500L))
                    .withRequestTimeout(Duration.ofMillis(1500L));

    @BeforeAll
    static void setUpIpSupport() throws UnknownHostException {
        InetAddress localhost = InetAddress.getLocalHost();
        InetAddress[] allMyIps = InetAddress.getAllByName(localhost.getCanonicalHostName());

        if (allMyIps == null || allMyIps.length == 0) {
            return;
        }

        for (InetAddress allMyIp : allMyIps) {
            if (allMyIp instanceof Inet4Address) {
                SUPPORTS_IPV4 = true;
            } else if (allMyIp instanceof Inet6Address) {
                SUPPORTS_IPV6 = true;
            }
        }
    }

    @Test
    void getInet4HostIp() throws UnknownHostException {
        ApacheHttpClientIpAddressService.DnsResolver service =
                new ApacheHttpClientIpAddressService.DnsResolver(ApacheHttpClientIpAddressService.IpFamily.IPV4);

        // when
        InetAddress[] inet4Addresses = service.resolve(IDENT_ME.getHost());

        // then
        assertThat(inet4Addresses).isNotNull().allMatch(Inet4Address.class::isInstance, "must be IPv4 address");
    }

    @Test
    @EnabledIf("supportsIpv4")
    void resolveInet4Address() {
        // when
        Result<Inet4Address> inet4Address = service.getInet4Address(IDENT_ME);

        // then
        assertThat(inet4Address).isNotNull().extracting(Result::success).isInstanceOf(Inet4Address.class);
    }

    @Test
    void getInet6HostIp() throws UnknownHostException {
        ApacheHttpClientIpAddressService.DnsResolver service =
                new ApacheHttpClientIpAddressService.DnsResolver(ApacheHttpClientIpAddressService.IpFamily.IPV6);

        // when
        InetAddress[] inet4Addresses = service.resolve(IDENT_ME.getHost());

        // then
        assertThat(inet4Addresses).isNotNull().allMatch(Inet6Address.class::isInstance, "must be IPv6 address");
    }

    @Test
    @EnabledIf("supportsIpv6")
    void resolveInet6Address() {
        // when
        Result<Inet6Address> inet6Address = service.getInet6Address(IDENT_ME);

        // then
        assertThat(inet6Address).isNotNull().extracting(Result::success).isInstanceOf(Inet6Address.class);
    }

    boolean supportsIpv4() {
        return SUPPORTS_IPV4;
    }

    boolean supportsIpv6() {
        return SUPPORTS_IPV6;
    }
}
