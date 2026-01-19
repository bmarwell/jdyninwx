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
package de.bmarwell.jdyninwx.app.settings;

import static java.util.Collections.unmodifiableMap;

import de.bmarwell.jdyninwx.app.settings.InwxSettings.RecordConfiguration;
import de.bmarwell.jdyninwx.common.value.InwxRecordId;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertyParser {

    private static final Logger LOG = LoggerFactory.getLogger(PropertyParser.class);

    private static final Pattern LIST_ELEMENT = Pattern.compile("^(?<actualKey>([a-zA-Z\\d.-])*)\\[\\d+\\]$");
    private static final Pattern LIST_MAP_ELEMENT =
            Pattern.compile("^(?<actualKey>([a-zA-Z\\d.-])*)\\[(?<theIndex>\\d+)\\]\\.(?<mapKey>[a-zA-Z\\d.-]+\\.?)+");

    private final Path settingsFile;

    private final Properties originalProperties = new Properties();

    private final Map<String, Object> parsedProperties = new LinkedHashMap<>();

    public PropertyParser(Path pathToSettingsFile) {
        this(readOriginalProperties(pathToSettingsFile));
    }

    public PropertyParser(Properties originalProperties) {
        this.settingsFile = null;
        this.originalProperties.putAll(originalProperties);
        initNormalizeAndSaveProperties();
    }

    private static Properties readOriginalProperties(Path settingsFile) {
        try (BufferedReader bufferedReader = Files.newBufferedReader(settingsFile, StandardCharsets.UTF_8)) {
            Properties properties = new Properties();
            properties.load(bufferedReader);
            return properties;
        } catch (IOException ioException) {
            throw new IllegalArgumentException("Unable to read properties file: [" + settingsFile + "].", ioException);
        }
    }

    /**
     * Read in the original properties, and normalize lists.
     */
    private void initNormalizeAndSaveProperties() {
        for (Entry<Object, Object> entry : originalProperties.entrySet()) {
            Object key = entry.getKey();
            if (!(key instanceof String stringKey)) {
                continue;
            }

            Object value = entry.getValue();
            if (!(value instanceof String stringValue)) {
                continue;
            }

            Matcher listElement = LIST_ELEMENT.matcher(stringKey);
            Matcher listMapElement = LIST_MAP_ELEMENT.matcher(stringKey);

            if (listElement.matches()) {
                String actualKey = listElement.group("actualKey");
                addListProperty(actualKey, stringValue);
            } else if (listMapElement.matches()) {
                String actualKey = listMapElement.group("actualKey");
                String mapKey = listMapElement.group("mapKey");
                int index = Integer.parseInt(listMapElement.group("theIndex"), 10);
                addMapPropertyEntry(actualKey, index, mapKey, stringValue);
            } else {
                addStringKey(stringKey, value);
            }
        }
    }

    private void addListProperty(String stringKey, String stringValue) {
        Object existingValue = parsedProperties.get(stringKey);

        if (existingValue == null) {
            LinkedList<Object> newList = new LinkedList<>();
            parsedProperties.put(stringKey, newList);
            newList.add(stringValue);
        } else if (existingValue instanceof List existingList) {
            existingList.add(stringValue);
        } else {
            throw new IllegalArgumentException(
                    "Cannot add key [" + stringKey + "] because another value type already exists for this key.");
        }
    }

    private void addMapPropertyEntry(String actualKey, int index, String mapKey, String stringValue) {
        Object existingValue = parsedProperties.get(actualKey);
        LOG.trace("Adding entry " + Map.of(mapKey, stringValue) + " to key " + actualKey);

        if (existingValue == null) {
            Map<Integer, Map<String, String>> entries = new ConcurrentHashMap<>();
            parsedProperties.put(actualKey, entries);

            Map<String, String> entryMap = new LinkedHashMap<>();
            entryMap.put(mapKey, stringValue);

            entries.put(index, entryMap);

        } else if (existingValue instanceof Map existingMap) {
            Object innserValue = existingMap.get(index);
            if (innserValue != null && !(innserValue instanceof Map)) {
                throw new IllegalStateException("This must be an error beforehand.");
            }

            if (innserValue instanceof Map innerMap) {
                innerMap.put(mapKey, stringValue);
            } else {
                Map<String, String> innerMap = new LinkedHashMap<>();
                innerMap.put(mapKey, stringValue);
                existingMap.put(index, innerMap);
            }
        } else {
            // other type
            throw new IllegalArgumentException("Cannot add key [" + actualKey + "] with map entry because value type "
                    + existingValue.getClass().getSimpleName() + "already exists for this key.");
        }
    }

    private void addStringKey(String stringKey, Object value) {
        Object existingValue = parsedProperties.get(stringKey);

        if (existingValue == null || existingValue instanceof String) {
            parsedProperties.put(stringKey, value);
        } else {
            throw new IllegalArgumentException(
                    "Cannot add key [" + stringKey + "] with single value because value type "
                            + existingValue.getClass().getSimpleName() + " already exists for this key.");
        }
    }

    public InwxSettings getInwxSettings() {
        PropertyFileConstants constants = new PropertyFileConstants(this.getParsedProperties());
        return new InwxSettings(
                constants.getInwxUserName(),
                constants.getInwxPassword(),
                constants.getInwxApiEndpoint(),
                constants.getInwxIpv4RecordConfigurations(),
                constants.getInwxIpv6RecordConfigurations(),
                constants.getIdentPoolIpv4(),
                constants.getIdentPoolIpv6(),
                PropertyFileConstants.DEFAULT_IDENT_CONNECT_TIMEOUT,
                constants.getIdentConnectTimeout());
    }

    protected Map<String, Object> getParsedProperties() {
        return unmodifiableMap(parsedProperties);
    }

    static final class PropertyFileConstants {

        static final Duration DEFAULT_IDENT_CONNECT_TIMEOUT = Duration.ofMillis(500L);
        static final Duration DEFAULT_IDENT_REQUEST_TIMEOUT = Duration.ofMillis(1500L);

        static final String INWX_USER_NAME = "jdynsinwx.inwx.username";
        static final String INWX_PASSWORD = "jdynsinwx.inwx.password";

        static final String INWX_API_ENDPOINT = "jdynsinwx.inwx.api.endpoint";

        static final String INWX_RECORDS_IPV4 = "jdynsinwx.inwx.record.ipv4";
        static final String INWX_RECORDS_IPV6 = "jdynsinwx.inwx.record.ipv6";
        static final String IDENT_POOL_IPV4 = "jdynsinwx.ident.pool.ipv4";
        static final String IDENT_POOL_IPV6 = "jdynsinwx.ident.pool.ipv6";
        static final String IDENT_REQUEST_TIMEOUT = "jdynsinwx.ident.connection.request.timeout";

        private final Map<String, Object> settings;

        public PropertyFileConstants(Map<String, Object> settings) {
            this.settings = settings;
        }

        String getInwxUserName() {
            return (String) settings.get(INWX_USER_NAME);
        }

        char[] getInwxPassword() {
            String password = (String) settings.get(INWX_PASSWORD);
            if (password == null) {
                return new char[] {};
            }
            return password.toCharArray();
        }

        Optional<URI> getInwxApiEndpoint() {
            return Optional.ofNullable((String) settings.get(INWX_API_ENDPOINT)).map(URI::create);
        }

        List<RecordConfiguration> getInwxIpv4RecordConfigurations() {
            return getInwxRecordConfigurationList(INWX_RECORDS_IPV4);
        }

        private List<RecordConfiguration> getInwxRecordConfigurationList(String settingConstant) {
            Object recordIds = settings.get(settingConstant);
            if (recordIds == null) {
                return List.of();
            }
            if (recordIds instanceof Map<?, ?> recordIdList) {
                return recordIdList.values().stream()
                        .map(v -> (Map<?, ?>) v)
                        .map(this::mapToRecordConfiguration)
                        .collect(Collectors.toCollection(LinkedList<RecordConfiguration>::new));
            }

            throw new UnsupportedOperationException("Cannot parse recordIds: " + recordIds);
        }

        private RecordConfiguration mapToRecordConfiguration(Map<?, ?> map) {
            String id = (String) map.get("id");
            long idNum;

            try {
                idNum = Long.parseLong(id, 10);
            } catch (NumberFormatException nfe) {
                throw new IllegalArgumentException("record IDs must be numeric, but found entry: " + map);
            }

            Object mapTtl = map.get("ttl");
            Duration duration = null;

            if (mapTtl instanceof String mapTtlString) {
                duration = Duration.ofSeconds(Integer.parseInt(mapTtlString));
            }

            return new RecordConfiguration(
                    new InwxRecordId(idNum), Optional.ofNullable(duration).orElseGet(() -> Duration.ofSeconds(300L)));
        }

        List<RecordConfiguration> getInwxIpv6RecordConfigurations() {
            return getInwxRecordConfigurationList(INWX_RECORDS_IPV6);
        }

        List<URI> getIdentPoolIpv4() {
            return getUriList(IDENT_POOL_IPV4);
        }

        private List<URI> getUriList(String settingConstant) {
            Object identPoolEntries = settings.get(settingConstant);
            if (identPoolEntries == null) {
                return List.of();
            }
            if (identPoolEntries instanceof List<?> identPoolList) {
                return identPoolList.stream()
                        .map(Object::toString)
                        .map(URI::create)
                        .collect(Collectors.toCollection(LinkedList<URI>::new));
            }

            throw new UnsupportedOperationException("Cannot parse recordIds: " + identPoolEntries);
        }

        List<URI> getIdentPoolIpv6() {
            return getUriList(IDENT_POOL_IPV6);
        }

        public Duration getIdentConnectTimeout() {
            Object reqTimeout = settings.get(IDENT_REQUEST_TIMEOUT);
            if (!(reqTimeout instanceof String)) {
                return DEFAULT_IDENT_REQUEST_TIMEOUT;
            }

            try {
                int durationMs = Integer.parseInt((String) reqTimeout, 10);
                return Duration.ofMillis(durationMs);
            } catch (NumberFormatException nfe) {
                String message = "Invalid setting in application.properties for key [" + IDENT_REQUEST_TIMEOUT + "]: ["
                        + reqTimeout + "].";
                LOG.error(message);
                throw new IllegalArgumentException(message, nfe);
            }
        }
    }
}
