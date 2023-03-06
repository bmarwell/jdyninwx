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

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.BasicHttpClientResponseHandler;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;

public class ApacheHttpClientStaticInwxUpdateService extends AbstractInwxUpdateService {

    private static final URI API_ENDPOINT = URI.create("https://api.domrobot.com/xmlrpc");

    private static final String XML_POST_TEMPALTE =
            """
            <?xml version="1.0" encoding="UTF-8"?>
            <methodCall>
               <methodName>nameserver.updateRecord</methodName>
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
                           <member>
                              <name>id</name>
                              <value>
                                 <int>%DNSID%</int>
                              </value>
                           </member>
                           <member>
                              <name>content</name>
                              <value>
                                 <string>%NEWIP%</string>
                              </value>
                           </member>
                           <member>
                              <name>ttl</name>
                              <value>
                                 <int>300</int>
                              </value>
                           </member>
                        </struct>
                     </value>
                  </param>
               </params>
            </methodCall>
            """;

    @Override
    public Result<?> updateRecord(int dnsRecordId, InetAddress newIp, int ttlSeconds) {
        try (CloseableHttpClient client = createApacheHttpClient()) {
            StringEntity entity = new StringEntity(XML_POST_TEMPALTE, ContentType.TEXT_XML);
            HttpPost httpPost = new HttpPost(API_ENDPOINT);
            httpPost.setEntity(entity);
            String execute = client.execute(httpPost, new BasicHttpClientResponseHandler());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    CloseableHttpClient createApacheHttpClient() {
        return HttpClientBuilder.create().useSystemProperties().build();
    }
}
