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

import de.bmarwell.jdyninwx.app.InwxUpdater;
import de.bmarwell.jdyninwx.lib.services.ApacheHttpClientIpAddressService;
import de.bmarwell.jdyninwx.lib.services.InwxQueryService;
import de.bmarwell.jdyninwx.lib.services.Result;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.URI;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

/**
 * The {@code ip} command will just resolve and show IP addreses..
 */
@CommandLine.Command(
        name = "ip",
        description = "Updates the inwx resource records according to the specified application.properties.")
public class Ip implements Callable<Integer> {

    private static final Logger LOG = LoggerFactory.getLogger(Ip.class);

    @CommandLine.Option(
            names = {"-a", "--all"},
            description = "Show results from all resolvers.")
    boolean showAll;

    @CommandLine.Option(
            names = {"--no-ipv4"},
            description = "Never show IPv4 results")
    boolean noIpv4;

    @CommandLine.Option(
            names = {"--no-ipv6"},
            description = "Never show IPv6 results")
    boolean noIpv6;

    @CommandLine.ParentCommand
    private InwxUpdater parent;

    private InwxQueryService inwxQueryService;

    public Ip() {}

    @Override
    public Integer call() {
        inwxQueryService = new ApacheHttpClientIpAddressService()
                .withConnectTimeout(parent.getSettings().identConnectTimeout())
                .withRequestTimeout(parent.getSettings().identRequestTimeout());

        if (!noIpv4) {
            showIpv4Results();
        }

        if (!noIpv6) {
            showIpv6Results();
        }

        return 0;
    }

    private void showIpv4Results() {
        if (showAll) {
            showAllIpv4Results();
            return;
        }

        showFirstIpv4Results();
    }

    private void showIpv6Results() {
        if (showAll) {
            showAllIpv6Results();
            return;
        }

        showFirstIpv6Results();
    }

    private void showAllIpv4Results() {
        for (URI uri : parent.getSettings().identPoolIpv4()) {
            Result<Inet4Address> inet4Address = inwxQueryService.getInet4Address(uri);
            if (inet4Address.isError()) {
                String message = String.format(
                        Locale.ROOT,
                        "[%-40s] => %s",
                        uri,
                        "fail: " + inet4Address.error().getMessage().replaceAll("\\n", " "));
                LOG.error(message);
            } else {
                String message = String.format(
                        Locale.ROOT,
                        "[%-40s] => [%45s]",
                        uri,
                        inet4Address.success().getHostAddress());
                LOG.info(message);
            }
        }
    }

    private void showFirstIpv4Results() {
        Optional<Inet4Address> address = inwxQueryService.getFirstResolvedInet4Address(
                parent.getSettings().identPoolIpv4());

        String message = String.format(
                Locale.ROOT,
                "[%-45s]",
                address.map(Inet4Address::getHostAddress).orElse("fail"));
        if (address.isEmpty()) {
            LOG.error(message);
        } else {
            LOG.info(message);
        }
    }

    private void showAllIpv6Results() {
        // length 45, because of https://stackoverflow.com/a/166157.
        for (URI uri : parent.getSettings().identPoolIpv6()) {
            Result<Inet6Address> inet6Address = inwxQueryService.getInet6Address(uri);
            if (inet6Address.isError()) {
                String message = String.format(Locale.ROOT, "[%-40s] => [%45s]", uri, "fail");
                LOG.error(message);
            } else {
                String message = String.format(
                        Locale.ROOT,
                        "[%-40s] => [%45s]",
                        uri,
                        inet6Address.success().getHostAddress());
                LOG.info(message);
            }
        }
    }

    private void showFirstIpv6Results() {
        Optional<Inet6Address> address = inwxQueryService.getFirstResolvedInet6Address(
                parent.getSettings().identPoolIpv6());

        String message = String.format(
                Locale.ROOT,
                "[%-45s]",
                address.map(Inet6Address::getHostAddress).orElse("fail"));
        if (address.isEmpty()) {
            LOG.error(message);
        } else {
            LOG.info(message);
        }
    }
}
