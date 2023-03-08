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

import de.bmarwell.jdyninwx.lib.services.ApacheHttpClientIpAddressService;
import de.bmarwell.jdyninwx.lib.services.IpAddressService;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;

/**
 * Updates the inwx resource records according to the specified application.properties.
 */
@Command(
        name = "update",
        description = "Updates the inwx resource records according to the specified application.properties.")
public class InwxUpdateCommand implements Callable<Integer> {

    private IpAddressService ipAddressService;

    public InwxUpdateCommand() {
        // init
        ipAddressService = new ApacheHttpClientIpAddressService();
    }

    @Override
    public Integer call() throws Exception {
        throw new UnsupportedOperationException("not implemented");
    }
}
