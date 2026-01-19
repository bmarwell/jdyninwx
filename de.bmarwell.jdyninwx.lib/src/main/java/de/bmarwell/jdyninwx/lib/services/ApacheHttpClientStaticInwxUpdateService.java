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

import de.bmarwell.jdyninwx.common.value.InwxRecordId;
import java.io.IOException;
import java.io.Serial;
import java.net.InetAddress;
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

    @Override
    public Result<String> updateRecord(InwxRecordId dnsRecordId, InetAddress newIp, int ttlSeconds) {
        try (CloseableHttpClient client = createApacheHttpClient()) {
            String xmlPost = createPostRequest(dnsRecordId, newIp, ttlSeconds);
            StringEntity entity = new StringEntity(xmlPost, ContentType.APPLICATION_XML);
            HttpPost httpPost = new HttpPost(getApiEndpoint());
            httpPost.setEntity(entity);
            httpPost.setHeader("Content-Type", ContentType.APPLICATION_XML);
            httpPost.setHeader("Accept", ContentType.APPLICATION_XML);
            String execute = client.execute(httpPost, new BasicHttpClientResponseHandler());

            return Result.ok(execute);
        } catch (IOException e) {
            return Result.fail(e);
        }
    }

    CloseableHttpClient createApacheHttpClient() {
        return HttpClientBuilder.create().useSystemProperties().build();
    }

    protected String createPostRequest(InwxRecordId dnsRecordId, InetAddress newIp, int ttlSeconds) {
        return Template.templateBuilder()
                .withCredentials(getCredentials().orElseThrow())
                .withMethod(Template.MethodName.nameserver_updateRecord)
                .withParameter("id", "long", dnsRecordId.value())
                .withParameter("content", "string", newIp.getHostAddress())
                .withParameter("ttl", "int", Integer.toString(ttlSeconds, 10))
                .build();
    }
}
