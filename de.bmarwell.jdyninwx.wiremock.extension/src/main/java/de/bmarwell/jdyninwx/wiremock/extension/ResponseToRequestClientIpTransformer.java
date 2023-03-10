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
package de.bmarwell.jdyninwx.wiremock.extension;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ResponseDefinitionTransformer;
import com.github.tomakehurst.wiremock.extension.StubLifecycleListener;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;

public class ResponseToRequestClientIpTransformer extends ResponseDefinitionTransformer
        implements StubLifecycleListener {

    public static final String NAME = "ResponseToRequestClientIpTransformer";
    private final boolean global;

    public ResponseToRequestClientIpTransformer(boolean applyGlobally) {
        this.global = applyGlobally;
    }

    @Override
    public boolean applyGlobally() {
        return global;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public ResponseDefinition transform(
            Request request, ResponseDefinition responseDefinition, FileSource files, Parameters parameters) {
        ResponseDefinitionBuilder newResponseDefBuilder = ResponseDefinitionBuilder.like(responseDefinition);

        newResponseDefBuilder.withBody(request.getClientIp());

        return newResponseDefBuilder.build();
    }

    @Override
    public void beforeStubCreated(StubMapping stub) {}

    @Override
    public void afterStubCreated(StubMapping stub) {}

    @Override
    public void beforeStubEdited(StubMapping oldStub, StubMapping newStub) {}

    @Override
    public void afterStubEdited(StubMapping oldStub, StubMapping newStub) {}

    @Override
    public void beforeStubRemoved(StubMapping stub) {}

    @Override
    public void afterStubRemoved(StubMapping stub) {}

    @Override
    public void beforeStubsReset() {}

    @Override
    public void afterStubsReset() {}
}
