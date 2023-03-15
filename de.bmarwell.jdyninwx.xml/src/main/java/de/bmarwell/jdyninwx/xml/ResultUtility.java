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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.stream.Stream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class ResultUtility {

    static final String XP_RETURN_CODE = "/methodResponse/params/param/value/struct/member[name='code']/value/int";
    static final String XP_MESSAGE = "/methodResponse/params/param/value/struct/member[name='msg']/value/string";
    static final String XP_RUNTIME = "/methodResponse/params/param/value/struct/member[name='runtime']/value/double";

    public XmlRpcResult parseUpdateResponse(String xmlResponse) {
        try (var is = new ByteArrayInputStream(xmlResponse.getBytes(StandardCharsets.UTF_8))) {
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            builderFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            Document xmlDocument = builder.parse(is);
            XPath xPath = XPathFactory.newInstance().newXPath();

            var returnCode = (Number) xPath.compile(XP_RETURN_CODE).evaluate(xmlDocument, XPathConstants.NUMBER);
            var message = (String) xPath.compile(XP_MESSAGE).evaluate(xmlDocument, XPathConstants.STRING);
            var runtime = (Number) xPath.compile(XP_RUNTIME).evaluate(xmlDocument, XPathConstants.NUMBER);

            return XmlRpcResult.ok(new XmlRpcResponse(returnCode, message, runtime));
        } catch (IOException
                | SAXException
                | ParserConfigurationException
                | XPathExpressionException javaIoIOException) {
            return XmlRpcResult.fail(javaIoIOException);
        }
    }

    public record XmlRpcResponse(Number code, String message, Number runtime) {}

    public record XmlRpcResult(XmlRpcResponse response, Throwable error) {
        private XmlRpcResult(XmlRpcResponse response) {
            this(response, null);
        }

        public XmlRpcResult {
            if (response != null && error != null) {
                throw new IllegalArgumentException("Cannot set both response and error!");
            }
            if (response == null && error == null) {
                throw new IllegalArgumentException("Cannot set none of response and error!");
            }
        }

        private XmlRpcResult(Throwable error) {
            this(null, error);
        }

        static XmlRpcResult ok(XmlRpcResponse success) {
            return new XmlRpcResult(Objects.requireNonNull(success));
        }

        static XmlRpcResult fail(Throwable error) {
            return new XmlRpcResult(Objects.requireNonNull(error));
        }

        public boolean isSuccess() {
            return this.response != null && this.error == null;
        }

        public boolean isError() {
            return this.error != null && this.response == null;
        }

        public Stream<XmlRpcResponse> stream() {
            return Stream.of(response());
        }
    }
}
