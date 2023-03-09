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

import de.bmarwell.jdyninwx.app.InwxUpdater.GlobalDefaultValueProvider;
import de.bmarwell.jdyninwx.app.commands.InwxStatusCommand;
import de.bmarwell.jdyninwx.app.commands.InwxUpdateCommand;
import de.bmarwell.jdyninwx.app.settings.InwxSettings;
import de.bmarwell.jdyninwx.app.settings.PropertyParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.IDefaultValueProvider;
import picocli.CommandLine.Model.ArgSpec;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Model.OptionSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ScopeType;
import picocli.CommandLine.Spec;

/**
 * Main InwxUpdater application entrypoint.
 */
@Command(
        name = "jdynsinwx",
        subcommands = {InwxUpdateCommand.class, InwxStatusCommand.class, CommandLine.HelpCommand.class},
        description = "Updates inwx resource records or displays their current state.",
        defaultValueProvider = GlobalDefaultValueProvider.class)
public class InwxUpdater {

    private static final Logger LOG = LoggerFactory.getLogger(InwxUpdater.class);

    @Spec
    CommandSpec spec;

    private InwxSettings settings;

    @Option(
            names = {"-s", "--settings"},
            scope = ScopeType.INHERIT)
    public void setSettingsFile(Path settingsFile) {
        if (!Files.exists(settingsFile)) {
            throw new IllegalArgumentException("File [" + settingsFile + "] does not exist");
        } else {
            LOG.info("Using settings file [" + settingsFile + "].");
            this.settings = new PropertyParser(settingsFile).getInwxSettings();
        }
    }

    /**
     * Main entrypoint.
     * @param args the java command line args.
     */
    public static void main(String[] args) {
        CommandLine commandLine = new CommandLine(new InwxUpdater());
        int exitCode = commandLine.execute(args);
        System.exit(exitCode);
    }

    public InwxSettings getSettings() {
        return this.settings;
    }

    static class GlobalDefaultValueProvider implements IDefaultValueProvider {

        @SuppressWarnings("ReturnOfNull")
        @Override
        public String defaultValue(ArgSpec argSpec) throws Exception {
            if (argSpec.isOption() && argSpec instanceof OptionSpec option) {
                return defaultOptionValue(option);
            }

            return null;
        }

        private String defaultOptionValue(OptionSpec option) throws IOException {
            switch (option.shortestName()) {
                case "-s": {
                    Path defaultConfigFile = getBasePathConfigHome();
                    Path configFileParent = defaultConfigFile.getParent();
                    Files.createDirectories(configFileParent);

                    if (!Files.exists(defaultConfigFile)) {
                        try (var defaultConf =
                                this.getClass().getResourceAsStream("settings/default.application.properties")) {
                            if (defaultConf == null) {
                                LOG.error("Application shipped without default.application.properties!");
                                throw new IllegalStateException(
                                        "Application shipped without default.application.properties!");
                            }
                            Files.copy(defaultConf, defaultConfigFile);
                        }
                    }

                    return defaultConfigFile.toString();
                }
                default:
                    return null;
            }
        }

        private Path getBasePathConfigHome() {
            String xdgConfigHome = System.getenv("XDG_CONFIG_HOME");
            if (xdgConfigHome != null) {
                return Path.of(xdgConfigHome, "jdyninwx", "application.properties");
            }

            String userHome = System.getenv("HOME");
            if (userHome != null) {
                return Path.of(userHome, ".config", "jdyninwx", "application.properties");
            }

            throw new UnsupportedOperationException("Cannot find config dir on your system.");
        }
    }
}
