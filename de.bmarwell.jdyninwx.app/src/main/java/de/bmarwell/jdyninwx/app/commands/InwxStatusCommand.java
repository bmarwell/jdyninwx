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
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;

/**
 * Retrieves and print the status of the specified domain resource records.
 */
@Command(name = "status", description = "Retrieves and print the status of the specified domain resource records.")
public class InwxStatusCommand implements Callable<Integer> {

    private static final Logger LOG = LoggerFactory.getLogger(InwxStatusCommand.class);

    @ParentCommand
    private InwxUpdater parent;

    @Override
    public Integer call() throws Exception {
        LOG.info("called with: " + parent.getSettings());
        throw new UnsupportedOperationException("not implemented");
    }
}
