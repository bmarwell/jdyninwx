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

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.extension.RegisterExtension;

class ApacheHttpClientInwxQueryServiceTest {

    @RegisterExtension
    public static final WireMockExtension wiremock = WireMockExtension.newInstance()
            .options(options().dynamicPort().extensions(new ResponseTemplateTransformer(false)))
            .build();

    private static boolean SUPPORTS_IPV4 = false;
    private static boolean SUPPORTS_IPV6 = false;
    private final InwxQueryService service = new ApacheHttpClientIpAddressService()
            .withConnectTimeout(Duration.ofMillis(1500L))
            .withRequestTimeout(Duration.ofMillis(1500L));

    @BeforeAll
    static void setUpIpSupport() throws UnknownHostException {
        InetAddress localhost = InetAddress.getLocalHost();
        final InetAddress[] allMyIps = Stream.concat(
                        Arrays.stream(InetAddress.getAllByName(localhost.getCanonicalHostName())),
                        Arrays.stream(InetAddress.getAllByName("localhost")))
                .toArray(InetAddress[]::new);

        for (InetAddress allMyIp : allMyIps) {
            if (allMyIp instanceof Inet4Address) {
                SUPPORTS_IPV4 = true;
            } else if (allMyIp instanceof Inet6Address) {
                SUPPORTS_IPV6 = true;
            }
        }
    }

    @BeforeEach
    void setUpWireMock() {
        wiremock.stubFor(get("/").willReturn(aResponse()
                .withTransformers(ResponseTemplateTransformer.NAME)
                .withBody("{{request.clientIp}}")));
    }

    @AfterEach
    void printFailedRequests() {
        wiremock.findUnmatchedRequests().getRequests().forEach(System.out::println);
    }

    @Test
    void getInet4HostIp() throws UnknownHostException {
        ApacheHttpClientIpAddressService.DnsResolver service =
                new ApacheHttpClientIpAddressService.DnsResolver(ApacheHttpClientIpAddressService.IpFamily.IPV4);

        // when
        InetAddress[] inet4Addresses =
                service.resolve(URI.create(wiremock.baseUrl()).getHost());

        // then
        assertThat(inet4Addresses).isNotNull().allMatch(Inet4Address.class::isInstance, "must be IPv4 address");
    }

    @Test
    @EnabledIf("supportsIpv4")
    void resolveInet4Address() {

        // when
        Result<Inet4Address> inet4Address = service.getInet4Address(URI.create(wiremock.baseUrl()));

        // then
        assertThat(inet4Address).isNotNull().extracting(Result::success).isInstanceOf(Inet4Address.class);
    }

    @Test
    void getInet6HostIp() throws UnknownHostException {
        ApacheHttpClientIpAddressService.DnsResolver service =
                new ApacheHttpClientIpAddressService.DnsResolver(ApacheHttpClientIpAddressService.IpFamily.IPV6);

        // when
        InetAddress[] inet4Addresses =
                service.resolve(URI.create(wiremock.baseUrl()).getHost());

        // then
        assertThat(inet4Addresses).isNotNull().allMatch(Inet6Address.class::isInstance, "must be IPv6 address");
    }

    @Test
    @EnabledIf("supportsIpv6")
    void resolveInet6Address() {
        // when
        Result<Inet6Address> inet6Address = service.getInet6Address(URI.create(wiremock.baseUrl()));

        // then
        assertThat(inet6Address).isNotNull().extracting(Result::success).isInstanceOf(Inet6Address.class);
    }

    @Test
    @EnabledIf("supportsIpv4")
    void resolveFirst() {
        // when
        Optional<Inet4Address> resolvedInet4Address =
                service.getFirstResolvedInet4Address(singletonList(URI.create(wiremock.baseUrl())));

        // then
        assertThat(resolvedInet4Address).isPresent().get().isInstanceOf(Inet4Address.class);
    }

    @Test
    void resolveFirstIPv4_empty() {
        // when
        var address = service.getFirstResolvedInet4Address(List.of());

        // then
        assertThat(address).isEmpty();
    }

    @Test
    @EnabledIf("supportsIpv6")
    void resolveFirstIPv6() {
        // when
        Optional<Inet6Address> resolvedInet6Address =
                service.getFirstResolvedInet6Address(singletonList(URI.create(wiremock.baseUrl())));

        // then
        assertThat(resolvedInet6Address).isPresent().get().isInstanceOf(Inet6Address.class);
    }

    @Test
    void resolveFirstIPv6_empty() {
        // when
        var address = service.getFirstResolvedInet6Address(List.of());

        // then
        assertThat(address).isEmpty();
    }

    boolean supportsIpv4() {
        return SUPPORTS_IPV4;
    }

    boolean supportsIpv6() {
        return SUPPORTS_IPV6;
    }
}
