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

import static de.bmarwell.jdyninwx.xml.NodeUtility.iterable;

import de.bmarwell.jdyninwx.common.value.DnsRecordType;
import de.bmarwell.jdyninwx.common.value.InwxNameServerRecord;
import de.bmarwell.jdyninwx.common.value.InwxRecordId;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ResultUtility {

    static final String XP_RETURN_CODE = "/methodResponse/params/param/value/struct/member[name='code']/value/int";
    static final String XP_MESSAGE = "/methodResponse/params/param/value/struct/member[name='msg']/value/string";
    static final String XP_RUNTIME = "/methodResponse/params/param/value/struct/member[name='runtime']/value/double";
    static final String XP_MEMBER_RECORD = "/methodResponse/params/param/value/struct/member[name='resData']/value"
            + "/struct/member[name='record']/value/array/data/*";
    static final String XP_RECORD_ID = "struct/member[name='id']/value/int";
    static final String XP_RECORD_NAME = "struct/member[name='name']/value/string";
    static final String XP_RECORD_CONTENT = "struct/member[name='content']/value/string";
    static final String XP_RECORD_TYPE = "struct/member[name='type']/value/string";
    static final String XP_RECORD_TTL = "struct/member[name='ttl']/value/int";
    static final String XP_RECORD_PRIO = "struct/member[name='prio']/value/int";

    public XmlRpcResult<List<InwxNameServerRecord>> parseNameServerInfoResponse(String xmlResponse) {
        try (var is = new ByteArrayInputStream(xmlResponse.getBytes(StandardCharsets.UTF_8))) {
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            builderFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            Document xmlDocument = builder.parse(is);
            XPath xPath = XPathFactory.newInstance().newXPath();

            var returnCode = (Number) xPath.compile(XP_RETURN_CODE).evaluate(xmlDocument, XPathConstants.NUMBER);
            var message = (String) xPath.compile(XP_MESSAGE).evaluate(xmlDocument, XPathConstants.STRING);
            var runtime = (Number) xPath.compile(XP_RUNTIME).evaluate(xmlDocument, XPathConstants.NUMBER);
            var records = (NodeList) xPath.compile(XP_MEMBER_RECORD).evaluate(xmlDocument, XPathConstants.NODESET);

            List<InwxNameServerRecord> resultRecords = new ArrayList<>();

            for (Node node : iterable(records)) {
                var resultRecord = parseNodeToRecord(node);
                resultRecord.ifPresent(resultRecords::add);
            }

            return XmlRpcResult.ok(new XmlRpcResponse(returnCode, message, runtime), List.copyOf(resultRecords));
        } catch (IOException
                | SAXException
                | ParserConfigurationException
                | XPathExpressionException javaIoIOException) {
            return XmlRpcResult.fail(javaIoIOException);
        }
    }

    private Optional<InwxNameServerRecord> parseNodeToRecord(Node node) {
        XPath xPath = XPathFactory.newInstance().newXPath();
        try {
            int recordId = (int) (double) xPath.compile(XP_RECORD_ID).evaluate(node, XPathConstants.NUMBER);
            if (recordId == 0) {
                return Optional.empty();
            }
            String recordName = (String) xPath.compile(XP_RECORD_NAME).evaluate(node, XPathConstants.STRING);
            String typeStr = (String) xPath.compile(XP_RECORD_TYPE).evaluate(node, XPathConstants.STRING);
            String recordContents = (String) xPath.compile(XP_RECORD_CONTENT).evaluate(node, XPathConstants.STRING);
            int recordTtlSeconds = (int) (double) xPath.compile(XP_RECORD_TTL).evaluate(node, XPathConstants.NUMBER);
            int recordPrio = (int) (double) xPath.compile(XP_RECORD_PRIO).evaluate(node, XPathConstants.NUMBER);
            DnsRecordType dnsRecordType = DnsRecordType.valueOf(typeStr.toUpperCase(Locale.ROOT));

            return Optional.of(new InwxNameServerRecord(
                    new InwxRecordId(recordId),
                    recordName,
                    dnsRecordType,
                    recordContents,
                    Duration.ofSeconds(recordTtlSeconds),
                    recordPrio));
        } catch (XPathExpressionException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public XmlRpcResult<Void> parseUpdateResponse(String xmlResponse) {
        try (var is = new ByteArrayInputStream(xmlResponse.getBytes(StandardCharsets.UTF_8))) {
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            builderFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            Document xmlDocument = builder.parse(is);
            XPath xPath = XPathFactory.newInstance().newXPath();

            var returnCode = (Number) xPath.compile(XP_RETURN_CODE).evaluate(xmlDocument, XPathConstants.NUMBER);
            var message = (String) xPath.compile(XP_MESSAGE).evaluate(xmlDocument, XPathConstants.STRING);
            var runtime = (Number) xPath.compile(XP_RUNTIME).evaluate(xmlDocument, XPathConstants.NUMBER);

            return XmlRpcResult.ok(new XmlRpcResponse(returnCode, message, runtime), null);
        } catch (IOException
                | SAXException
                | ParserConfigurationException
                | XPathExpressionException javaIoIOException) {
            return XmlRpcResult.fail(javaIoIOException);
        }
    }

    public record XmlRpcResponse(Number code, String message, Number runtime) {}

    public record XmlRpcResult<T>(XmlRpcResponse response, T data, Throwable error) {
        private XmlRpcResult(XmlRpcResponse response) {
            this(response, null, null);
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
            this(null, null, error);
        }

        static XmlRpcResult<?> ok(XmlRpcResponse success) {
            return new XmlRpcResult<>(Objects.requireNonNull(success));
        }

        static <T> XmlRpcResult<T> ok(XmlRpcResponse success, T data) {
            return new XmlRpcResult<>(Objects.requireNonNull(success), data, null);
        }

        static <T> XmlRpcResult<T> fail(Throwable error) {
            return new XmlRpcResult<>(Objects.requireNonNull(error));
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
