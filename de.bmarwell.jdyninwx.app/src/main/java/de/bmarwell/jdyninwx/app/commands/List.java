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
import de.bmarwell.jdyninwx.common.value.InwxNameServerRecord;
import de.bmarwell.jdyninwx.lib.services.ApacheHttpClientIpAddressService;
import de.bmarwell.jdyninwx.lib.services.InwxQueryService;
import de.bmarwell.jdyninwx.lib.services.InwxUpdateService;
import de.bmarwell.jdyninwx.lib.services.Result;
import java.net.URI;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

/**
 * Lists all records from all domain servers.
 */
@CommandLine.Command(name = "list", description = "List all known nameserver records")
public class List implements Callable<Integer> {

    private static final Logger LOG = LoggerFactory.getLogger(Status.class);

    @CommandLine.Parameters(index = "0", description = "domain name", defaultValue = "*")
    String domainName;

    @CommandLine.ParentCommand
    private InwxUpdater parent;

    private InwxQueryService inwxQueryService;

    @Override
    public Integer call() throws Exception {
        final InwxUpdateService.InwxCredentials inwxCredentials =
                parent.getSettings().getCredentials();
        URI defaultApiEndpoint = parent.getSettings()
                .inwxApiEndpoint()
                .orElseGet(() -> new ApacheHttpClientIpAddressService().getApiEndpoint());

        inwxQueryService = new ApacheHttpClientIpAddressService()
                .withConnectTimeout(parent.getSettings().identConnectTimeout())
                .withRequestTimeout(parent.getSettings().identRequestTimeout())
                .withCredentials(inwxCredentials)
                .withApiEndpoint(parent.getSettings().inwxApiEndpoint().orElse(defaultApiEndpoint));

        Result<java.util.List<InwxNameServerRecord>> records = inwxQueryService.listAllNameServerRecords(domainName);

        for (InwxNameServerRecord record : records.success()) {
            LOG.info(
                    "{} :: {} :: {} :: {} :: {} :: {}",
                    record.recordId().value(),
                    record.recordType(),
                    record.name(),
                    record.content(),
                    record.ttl(),
                    record.prio());
        }

        return 0;
    }
}
