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
package de.bmarwell.jdyninwx.xml;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

class NodeUtilityTest {

    @Test
    void make_node_list_iterable() {
        // given
        NodeList nodeList = new NodeList() {
            private final List<Node> nodes = List.of();

            @Override
            public Node item(int index) {
                return nodes.get(index);
            }

            @Override
            public int getLength() {
                return nodes.size();
            }
        };

        // when
        final Iterable<Node> iterable = NodeUtility.iterable(nodeList);

        // then
        assertThat(iterable).isEmpty();
    }
}
