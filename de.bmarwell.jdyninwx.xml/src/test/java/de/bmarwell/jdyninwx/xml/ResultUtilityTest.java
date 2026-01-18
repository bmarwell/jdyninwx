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

import org.junit.jupiter.api.Test;

class ResultUtilityTest {

    @Test
    void canParseSuccessfulResponse() {
        // given
        final ResultUtility resultUtility = new ResultUtility();
        var xmlResult = """
                <?xml version="1.0" encoding="UTF-8"?>
                <methodResponse>
                   <params>
                      <param>
                         <value>
                            <struct>
                               <member>
                                  <name>code</name>
                                  <value>
                                     <int>1000</int>
                                  </value>
                               </member>
                               <member>
                                  <name>msg</name>
                                  <value>
                                     <string>Command completed successfully</string>
                                  </value>
                               </member>
                               <member>
                                  <name>svTRID</name>
                                  <value>
                                     <string>20230315-1022145658</string>
                                  </value>
                               </member>
                               <member>
                                  <name>runtime</name>
                                  <value>
                                     <double>1.224550</double>
                                  </value>
                               </member>
                            </struct>
                         </value>
                      </param>
                   </params>
                </methodResponse>
                """;

        // when
        final ResultUtility.XmlRpcResult xmlRpcResult = resultUtility.parseUpdateResponse(xmlResult);

        // then
        assertThat(xmlRpcResult)
                .matches(ResultUtility.XmlRpcResult::isSuccess)
                .extracting(ResultUtility.XmlRpcResult::response)
                .extracting(
                        ResultUtility.XmlRpcResponse::code,
                        ResultUtility.XmlRpcResponse::message,
                        ResultUtility.XmlRpcResponse::runtime)
                .contains(1_000.0, "Command completed successfully", 1.224_550);
    }
}
