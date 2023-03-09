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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Path;
import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.junit.jupiter.api.Test;

class PropertyParserTest {

    @Test
    void read_two_properties() {
        // given
        Properties properties = new Properties();
        properties.put("key1", "value1");
        properties.put("key2", "value2");

        // when
        PropertyParser propertyParser = new PropertyParser(properties);
        Map<String, Object> parsedProperties = propertyParser.getParsedProperties();

        // then
        assertThat(parsedProperties).hasSize(2);
    }

    @Test
    void read_two_list_elements() {
        // given
        Properties properties = new Properties();
        properties.put("key[1]", "value1");
        properties.put("key[2]", "value2");

        // when
        PropertyParser propertyParser = new PropertyParser(properties);
        Map<String, Object> parsedProperties = propertyParser.getParsedProperties();

        // then
        assertThat(parsedProperties)
                .hasSize(1)
                .hasFieldOrProperty("key")
                .extracting("key")
                .isInstanceOf(List.class)
                .matches(theList -> ((List) theList).size() == 2);
    }

    @Test
    void read_list_then_single_key() {
        // given
        Properties properties = new Properties();
        properties.put("key[1]", "value1");
        properties.put("key", "value2");

        // expect
        assertThatThrownBy(() -> new PropertyParser(properties))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("key")
                .hasMessageContaining("value")
                .hasMessageContaining("value type LinkedList already exists");
    }

    @Test
    void read_single_key_then_list() {
        // given
        Properties properties = new Properties();
        properties.put("key", "value2");
        properties.put("key[1]", "value1");

        // expect
        assertThatThrownBy(() -> new PropertyParser(properties))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("key")
                .hasMessageContaining("value")
                // message 'a string value' cannot be hit b/c of Properties internal hashtable sorting.
                .hasMessageContaining("value type LinkedList already exists");
    }

    @Test
    void read_valid_properties_from_file() {
        // given
        String validProps = this.getClass().getResource("valid_test.properties").getPath();
        Path validPropsPath = Path.of(validProps);

        // when
        PropertyParser propertyParser = new PropertyParser(validPropsPath);
        Map<String, Object> parsedProperties = propertyParser.getParsedProperties();

        // then
        assertThat(parsedProperties)
                .hasSize(3)
                .contains(
                        new SimpleEntry<>("anotherstringkey", "anothervalue"),
                        new SimpleEntry<>("thelistkey", List.of("1", "3", "2")),
                        new SimpleEntry<>("thestringkey1", "value1"));
    }

    @Test
    void read_record_id_and_key() {
        // given
        Properties properties = new Properties();
        properties.put("jdynsinwx.inwx.record.ipv4[1].id", "41");
        properties.put("jdynsinwx.inwx.record.ipv4[1].ttl", "300");
        properties.put("jdynsinwx.inwx.record.ipv4[2].id", "42");
        properties.put("jdynsinwx.inwx.record.ipv4[2].ttl", "300");

        // when
        PropertyParser propertyParser = new PropertyParser(properties);
        Map<String, Object> parsedProperties = propertyParser.getParsedProperties();

        // then
        assertThat(parsedProperties)
                .hasSize(1)
                .extracting(map -> map.get("jdynsinwx.inwx.record.ipv4"))
                .isInstanceOf(Map.class)
                .matches(theList -> ((Map<?, ?>) theList).size() == 2);
    }

    @Test
    void test_complete_example_configuration() {
        // given
        String validProps = this.getClass().getResource("full.properties").getPath();
        Path validPropsPath = Path.of(validProps);

        // when
        PropertyParser propertyParser = new PropertyParser(validPropsPath);
        InwxSettings inwxSettings = propertyParser.getInwxSettings();

        // then
        assertThat(inwxSettings)
                .matches(is -> is.ipv4UpdateRecords().size() == 2)
                .matches(is -> is.ipv6UpdateRecords().size() == 1)
                .matches(is -> is.identPoolIpv6().size() == 1)
                .matches(is -> is.identPoolIpv4().size() == 1);
    }
}
