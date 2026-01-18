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
package de.bmarwell.jdyninwx.lib.services;

import java.util.Locale;

public final class Template {

    static final String XML_POST_TEMPALTE = """
          <?xml version="1.0" encoding="UTF-8"?>
          <methodCall>
             <methodName>%METHOD%</methodName>
             <params>
                <param>
                   <value>
                      <struct>
                         <member>
                            <name>user</name>
                            <value>
                               <string>%USER%</string>
                            </value>
                         </member>
                         <member>
                            <name>lang</name>
                            <value>
                               <string>en</string>
                            </value>
                         </member>
                         <member>
                            <name>pass</name>
                            <value>
                               <string>%PASSWD%</string>
                            </value>
                         </member>
                         %PARAMETER%
                      </struct>
                   </value>
                </param>
             </params>
          </methodCall>
          """.trim();

    private Template() {}

    static TemplateBuilder templateBuilder() {
        return new TemplateBuilder(XML_POST_TEMPALTE);
    }

    public enum MethodName {
        nameserver_updateRecord("nameserver.updateRecord"),
        nameserver_export_records("nameserver.exportrecords"),
        nameserver_info("nameserver.info");

        private final String inwxName;

        MethodName(String inwxName) {
            this.inwxName = inwxName;
        }

        public CharSequence inwxName() {
            return this.inwxName;
        }
    }

    static class TemplateBuilder {

        private String currentTemplate;

        public TemplateBuilder(String xmlPostTempalte) {
            this.currentTemplate = xmlPostTempalte;
        }

        public TemplateBuilder withMethod(MethodName methodName) {
            this.currentTemplate = currentTemplate.replace("%METHOD%", methodName.inwxName());
            return this;
        }

        public TemplateBuilder withCredentials(InwxUpdateService.InwxCredentials credentials) {
            this.currentTemplate = this.currentTemplate
                    .replace("%USER%", credentials.username())
                    .replace("%PASSWD%", new String(credentials.password()));
            return this;
        }

        public TemplateBuilder withParameter(String name, String type, Object value) {
            this.currentTemplate = this.currentTemplate.replace(
                    "%PARAMETER%\n", String.format(Locale.ROOT, """
                  <member>
                     <name>%1$s</name>
                     <value>
                        <%2$s>%3$s</%2$s>
                     </value>
                  </member>
                  %%PARAMETER%%\n""", name, type, value.toString()));
            return this;
        }

        public String build() {
            return currentTemplate.replace("%PARAMETER%", "");
        }
    }
}
