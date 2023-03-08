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
package de.bmarwell.jdyninwx.app;

import de.bmarwell.jdyninwx.app.commands.InwxStatusCommand;
import de.bmarwell.jdyninwx.app.commands.InwxUpdateCommand;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;

/**
 * Main InwxUpdater application entrypoint.
 */
@Command(
        name = "jdynsinwx",
        subcommands = {InwxUpdateCommand.class, InwxStatusCommand.class, CommandLine.HelpCommand.class},
        description = "Updates inwx resource records or displays their current state.")
public class InwxUpdater {

    @Spec
    CommandSpec spec;

    /**
     * Main entrypoint.
     * @param args the java command line args.
     */
    public static void main(String[] args) {
        int exitCode = new CommandLine(new InwxUpdater()).execute(args);
        System.exit(exitCode);
    }
}
