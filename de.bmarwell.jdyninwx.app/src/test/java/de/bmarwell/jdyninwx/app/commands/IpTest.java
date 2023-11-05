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
package de.bmarwell.jdyninwx.app.commands;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import de.bmarwell.jdyninwx.app.InwxUpdater;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

class IpTest {

    @RegisterExtension
    static WireMockExtension SERVER =
            WireMockExtension.newInstance().options(options().dynamicPort()).build();

    @TempDir
    static Path tempDir;

    static Path testProperties;

    private static boolean SUPPORTS_IPV4 = false;

    private static boolean SUPPORTS_IPV6 = false;

    private ListAppender<ILoggingEvent> logWatcher;

    @BeforeAll
    static void setUpConfig() throws IOException {
        testProperties = tempDir.resolve("IpTest.properties");
        Files.writeString(
                testProperties,
                """
                    ## =====================
                    ## === Pool settings ===
                    ## =====================
                    ## IPv4 pools. All domains will be queried via HTTP GET.
                    jdynsinwx.ident.pool.ipv4[0]=%1$s
                    ## IPv6 pools. All domains will be queried via HTTP GET.
                    jdynsinwx.ident.pool.ipv6[0]=%1$s
                    """
                        .formatted(SERVER.baseUrl()),
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE_NEW);

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
    void setUp() {
        logWatcher = new ListAppender<>();
        logWatcher.start();
        ((Logger) LoggerFactory.getLogger(IpTest.class)).addAppender(logWatcher);
        ((Logger) LoggerFactory.getLogger(Ip.class)).addAppender(logWatcher);

        SERVER.stubFor(get("/").withHeader("accept", matching("^text/plain.*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("content-type", "text/plain")
                        .withTransformers(ResponseTemplateTransformer.NAME)
                        .withBody("{{request.clientIp}}")));
    }

    @Test
    @EnabledIf("supportsIpv4")
    void show_ip_shows_ipv4() {
        // given
        var app = new InwxUpdater();

        // when
        final int result = new CommandLine(app).execute("-s", testProperties.toString(), "ip", "--no-ipv6");

        assertThat(result).isEqualTo(0);
        assertThat(logWatcher.list.toString()).contains("127.0.0.1");
    }

    @Test
    @EnabledIf("supportsIpv6")
    void show_ip_shows_ipv6() {
        // given
        var app = new InwxUpdater();

        // when
        final int result = new CommandLine(app).execute("-s", testProperties.toString(), "ip", "--no-ipv4");

        assertThat(result).isEqualTo(0);
        assertThat(logWatcher.list.toString()).containsAnyOf("[0:0:0:0:0:0:0:1", "::1");
    }

    boolean supportsIpv4() {
        return SUPPORTS_IPV4;
    }

    boolean supportsIpv6() {
        return SUPPORTS_IPV6;
    }
}
