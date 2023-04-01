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
import de.bmarwell.jdyninwx.app.settings.InwxSettings;
import de.bmarwell.jdyninwx.lib.services.ApacheHttpClientIpAddressService;
import de.bmarwell.jdyninwx.lib.services.ApacheHttpClientStaticInwxUpdateService;
import de.bmarwell.jdyninwx.lib.services.InwxQueryService;
import de.bmarwell.jdyninwx.lib.services.InwxUpdateService;
import de.bmarwell.jdyninwx.lib.services.Result;
import de.bmarwell.jdyninwx.xml.ResultUtility;
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
public class Update implements Callable<Integer> {

    private static final Logger LOG = LoggerFactory.getLogger(Update.class);
    private static final int RC_NO_IPV4_ADDRESS = 1;
    private static final int RC_NO_IPV6_ADDRESS = 2;
    private static final int RC_UPDATE_IPV4_FAILED = 4;
    private static final int RC_UPDATE_IPV6_FAILED = 8;

    private InwxQueryService inwxQueryService;
    private InwxUpdateService inwxUpdateService;

    @ParentCommand
    private InwxUpdater parent;

    public Update() {
        // init
    }

    @Override
    public Integer call() {
        inwxQueryService = new ApacheHttpClientIpAddressService();
        final InwxUpdateService.InwxCredentials inwxCredentials =
                parent.getSettings().getCredentials();
        inwxUpdateService = new ApacheHttpClientStaticInwxUpdateService();
        URI defaultApiEndpoint = inwxUpdateService.getApiEndpoint();
        inwxUpdateService = inwxUpdateService
                .withCredentials(inwxCredentials)
                .withApiEndpoint(parent.getSettings().inwxApiEndpoint().orElse(defaultApiEndpoint));
        // TODO: default TTL

        LOG.info("called with: " + parent.getSettings());

        final int rcIpv4 = updateIpv4Records();

        final int rcIpv6 = updateIpv6Records();

        return rcIpv4 | rcIpv6;
    }

    private int updateIpv4Records() {
        if (parent.getSettings().ipv4UpdateRecords().isEmpty()) {
            LOG.info("Skipping IPv4 records update, no IPv4 records defined.");
            return 0;
        }

        return doUpdateIpv4Records();
    }

    private int updateIpv6Records() {
        if (parent.getSettings().ipv6UpdateRecords().isEmpty()) {
            LOG.info("Skipping IPv6 records update, no IPv6 records defined.");
            return 0;
        }

        return doUpdateIpv6Records();
    }

    private int doUpdateIpv4Records() {
        List<URI> ipv4resolvers = parent.getSettings().identPoolIpv4();
        if (ipv4resolvers.isEmpty()) {
            throw new IllegalStateException("Cannot update ipv4 records, no IPv4 resolvers defined!");
        }
        Optional<Inet4Address> inet4Address = inwxQueryService.getFirstResolvedInet4Address(ipv4resolvers);
        if (inet4Address.isEmpty()) {
            return RC_NO_IPV4_ADDRESS;
        }

        Inet4Address publicInet4Address = inet4Address.orElseThrow();
        LOG.info("Public IPv4 address: [" + publicInet4Address.getHostAddress() + "].");

        for (InwxSettings.RecordConfiguration ipv6UpdateRecord :
                parent.getSettings().ipv4UpdateRecords()) {
            final Result<String> result = inwxUpdateService.updateRecord(
                    ipv6UpdateRecord.recordId(),
                    publicInet4Address,
                    ipv6UpdateRecord.ttl().getSeconds());

            if (result.isError()) {
                LOG.error("Exception updating IPv4 record with ID %s: %s."
                        .formatted(ipv6UpdateRecord.recordId(), result.error().getMessage()));
                return RC_UPDATE_IPV4_FAILED;
            }

            final String xmlRpcResponse = result.success();
            final ResultUtility.XmlRpcResult xmlRpcResult = new ResultUtility().parseUpdateResponse(xmlRpcResponse);

            if (xmlRpcResult.isSuccess()) {
                LOG.info("Updated IPv4 record %s successfully. Response: %s."
                        .formatted(ipv6UpdateRecord.recordId(), xmlRpcResult));
                LOG.debug("INWX response: %s".formatted(xmlRpcResponse));
            } else {
                LOG.error(
                        "Update IPv4 record %s not successfull. Response: %s"
                                .formatted(ipv6UpdateRecord.recordId(), xmlRpcResponse),
                        xmlRpcResult.error());
            }
        }

        return 0;
    }

    private int doUpdateIpv6Records() {
        List<URI> ipv6resolvers = parent.getSettings().identPoolIpv4();
        if (ipv6resolvers.isEmpty()) {
            throw new IllegalStateException("Cannot update IPv6 records, no IPv6 resolvers defined!");
        }
        Optional<Inet6Address> inet6Address = inwxQueryService.getFirstResolvedInet6Address(ipv6resolvers);
        if (inet6Address.isEmpty()) {
            return RC_NO_IPV6_ADDRESS;
        }

        Inet6Address publicInet6Address = inet6Address.orElseThrow();
        LOG.info("Public IPv6 address: [" + publicInet6Address.getHostAddress() + "].");

        for (InwxSettings.RecordConfiguration ipv6UpdateRecord :
                parent.getSettings().ipv6UpdateRecords()) {
            final Result<String> result = inwxUpdateService.updateRecord(
                    ipv6UpdateRecord.recordId(),
                    publicInet6Address,
                    ipv6UpdateRecord.ttl().getSeconds());

            if (result.isError()) {
                LOG.error("Exception updating IPv6 record with ID %s: %s."
                        .formatted(ipv6UpdateRecord.recordId(), result.error().getMessage()));
                return RC_UPDATE_IPV6_FAILED;
            } else if (!result.success().contains("Command completed successfully")) {
                LOG.error("Unknown status updating IPv6 record with ID %s: %s."
                        .formatted(ipv6UpdateRecord.recordId(), result.success()));
                return RC_UPDATE_IPV6_FAILED;
            }

            final String xmlRpcResponse = result.success();
            final ResultUtility.XmlRpcResult<Void> xmlRpcResult =
                    new ResultUtility().parseUpdateResponse(xmlRpcResponse);

            if (xmlRpcResult.isSuccess()) {
                LOG.info("Updated IPv6 record %s successfully. Response: %s."
                        .formatted(ipv6UpdateRecord.recordId(), xmlRpcResult));
                LOG.debug("INWX response: %s".formatted(xmlRpcResponse));
            } else {
                LOG.error(
                        "Update IPv6 record %s not successfull. Response: %s"
                                .formatted(ipv6UpdateRecord.recordId(), xmlRpcResponse),
                        xmlRpcResult.error());
            }
        }

        return 0;
    }
}
