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

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import de.bmarwell.jdyninwx.lib.services.InwxUpdateService.InwxCredentials;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class ApacheHttpClientStaticInwxUpdateServiceTest {

    @RegisterExtension
    public static final WireMockExtension WIREMOCK =
            WireMockExtension.newInstance().options(options().dynamicPort()).build();

    @Test
    void template_replaces_fields() throws UnknownHostException {
        // given
        final ApacheHttpClientStaticInwxUpdateService service = new ApacheHttpClientStaticInwxUpdateService()
                .withCredentials(new InwxCredentials("myUserName", "myFancyPassword".toCharArray()));

        // when
        String postRequestEntity = service.createPostRequest(42, InetAddress.getByName("8.8.8.8"), 300);

        // then
        assertThat(postRequestEntity).contains(">myUserName</", ">myFancyPassword</");
    }

    @Test
    void upload_returns_200() throws UnknownHostException {
        // given
        final ApacheHttpClientStaticInwxUpdateService service = new ApacheHttpClientStaticInwxUpdateService()
                .withCredentials(new InwxCredentials("myUserName", "myFancyPassword".toCharArray()))
                .withApiEndpoint(URI.create(WIREMOCK.baseUrl()));
        WIREMOCK.stubFor(post("/")
                .withHeader("content-type", equalTo("text/xml; charset=UTF-8"))
                .willReturn(ok()));

        // when
        Result<?> updateRecordResult = service.updateRecord(42, InetAddress.getByName("8.8.8.8"));

        // then
        assertThat(updateRecordResult).matches(Result::isSuccess);
    }
}
