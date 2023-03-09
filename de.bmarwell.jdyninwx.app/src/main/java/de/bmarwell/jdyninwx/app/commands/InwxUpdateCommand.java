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
import de.bmarwell.jdyninwx.lib.services.IpAddressService;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;

/**
 * Updates the inwx resource records according to the specified application.properties.
 */
@Command(
        name = "update",
        description = "Updates the inwx resource records according to the specified application.properties.")
public class InwxUpdateCommand implements Callable<Integer> {

    private static final Logger LOG = LoggerFactory.getLogger(InwxUpdateCommand.class);

    @ParentCommand
    private InwxUpdater parent;

    private IpAddressService ipAddressService;

    public InwxUpdateCommand() {
        // init
        ipAddressService = new ApacheHttpClientIpAddressService();
    }

    @Override
    public Integer call() throws Exception {
        LOG.info("called with: " + parent.getSettings());

        updateIpv4Records();

        updateIpv6Records();

        throw new UnsupportedOperationException("not implemented");
    }

    private void updateIpv6Records() {
        if (parent.getSettings().ipv6UpdateRecords().isEmpty()) {
            LOG.info("Skipping IPv6 records update, no IPv6 records defined.");
            // return;
        }

        doUpdateIpv6Records();
    }

    private void updateIpv4Records() {
        if (parent.getSettings().ipv4UpdateRecords().isEmpty()) {
            LOG.info("Skipping IPv4 records update, no IPv4 records defined.");
            // return;
        }

        doUpdateIpv4Records();
    }

    private void doUpdateIpv4Records() {
        List<URI> ipv4resolvers = parent.getSettings().identPoolIpv4();
        if (ipv4resolvers.isEmpty()) {
            throw new IllegalStateException("Cannot update ipv4 records, no IPv4 resolvers defined!");
        }
        Optional<Inet4Address> inet4Address = ipAddressService.getFirstResolvedInet4Address(ipv4resolvers);
        if (inet4Address.isEmpty()) {
            throw new IllegalStateException("Cannot update ipv4 records, no public IPv4 address!");
        }

        Inet4Address publicInet4Address = inet4Address.orElseThrow();
        LOG.info("Public IPv4 address: [" + publicInet4Address + "].");
    }

    private void doUpdateIpv6Records() {
        List<URI> ipv6resolvers = parent.getSettings().identPoolIpv4();
        if (ipv6resolvers.isEmpty()) {
            throw new IllegalStateException("Cannot update IPv6 records, no IPv6 resolvers defined!");
        }
        Optional<Inet6Address> inet6Address = ipAddressService.getFirstResolvedInet6Address(ipv6resolvers);
        if (inet6Address.isEmpty()) {
            throw new IllegalStateException("Cannot update ipv6 records, no public IPv6 address!");
        }

        Inet6Address publicInet6Address = inet6Address.orElseThrow();
        LOG.info("Public IPv4 address: [" + publicInet6Address + "].");
    }
}
