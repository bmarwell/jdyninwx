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
import java.io.Serial;
import java.net.InetAddress;
import org.apache.hc.client5.http.HttpResponseException;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.BasicHttpClientResponseHandler;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;

/**
 * InwxUpdateService based on the Apache HttpCilent and a static template string.
 */
public class ApacheHttpClientStaticInwxUpdateService extends AbstractInwxUpdateService {

    @Serial
    private static final long serialVersionUID = -2062602651810812231L;

    /**
     * Default constructor for immutable class; can be modified using {@code with*()}-methods.
     */
    public ApacheHttpClientStaticInwxUpdateService() {
        // cdi
    }

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
                                 <int>%TTL%</int>
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
            String xmlPost = createPostRequest(dnsRecordId, newIp, ttlSeconds);
            StringEntity entity = new StringEntity(xmlPost, ContentType.TEXT_XML);
            HttpPost httpPost = new HttpPost(getApiEndpoint());
            httpPost.setEntity(entity);
            String execute = client.execute(httpPost, new BasicHttpClientResponseHandler());

            return Result.ok(execute);
        } catch (HttpResponseException e) {
            return Result.fail(e);
        } catch (IOException e) {
            return Result.fail(e);
        }
    }

    protected String createPostRequest(int dnsRecordId, InetAddress newIp, int ttlSeconds) {
        return XML_POST_TEMPALTE
                .replace(
                        "%USER%",
                        getCredentials().map(InwxCredentials::username).orElse(""))
                .replace(
                        "%PASSWD%",
                        getCredentials()
                                .map(InwxCredentials::password)
                                .map(String::new)
                                .orElse(""))
                .replace("%DNSID%", Integer.toString(dnsRecordId, 10))
                .replace("%NEWIP%", newIp.getHostAddress())
                .replace("%TTL%", Integer.toString(ttlSeconds, 10));
    }

    CloseableHttpClient createApacheHttpClient() {
        return HttpClientBuilder.create().useSystemProperties().build();
    }
}
