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
import java.net.*;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import org.apache.hc.client5.http.SystemDefaultDnsResolver;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.impl.classic.BasicHttpClientResponseHandler;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;

/**
 * Apache HttpClient-based IP Address Service.
 */
public class ApacheHttpClientIpAddressService extends AbstractConfigurableHttpClientIpAddressService
        implements IpAddressService {

    @Serial
    private static final long serialVersionUID = 3653973017046313858L;

    /**
     * Constructs a default, immutable instance.
     */
    public ApacheHttpClientIpAddressService() {
        // cdi etc
    }

    @Override
    public Result<Inet4Address> getInet4Address(URI ipv4resolver) {
        return getResolverResponseForFamily(ipv4resolver, IpFamily.IPV4);
    }

    @SuppressWarnings("unchecked")
    private <T extends InetAddress> Result<T> getResolverResponseForFamily(URI resolverUri, IpFamily ipFamily) {
        try (CloseableHttpClient client = createApacheHttpClient(ipFamily)) {
            HttpGet getIpRequest = new HttpGet(resolverUri);
            getIpRequest.addHeader("accept", "text/plain");
            String execute = client.execute(getIpRequest, new BasicHttpClientResponseHandler());

            if (execute.isBlank()) {
                return Result.fail(new IllegalStateException("empty result"));
            }

            return Result.ok((T) ipFamily.getByName(execute));
        } catch (IOException | IllegalStateException e) {
            return Result.fail(e);
        }
    }

    CloseableHttpClient createApacheHttpClient(IpFamily ipFamily) {
        return HttpClientBuilder.create()
                .useSystemProperties()
                .setConnectionManager(createConnectionManager(ipFamily))
                .build();
    }

    private HttpClientConnectionManager createConnectionManager(IpFamily ipFamily) {
        final DnsResolver dnsResolver = new DnsResolver(ipFamily);
        final ConnectionConfig connConfig = ConnectionConfig.custom()
                .setConnectTimeout(getConnectTimeout().toMillis(), TimeUnit.MILLISECONDS)
                .setSocketTimeout(Math.toIntExact(getRequestTimeout().toMillis()), TimeUnit.MILLISECONDS)
                .build();

        return PoolingHttpClientConnectionManagerBuilder.create()
                .useSystemProperties()
                .setDnsResolver(dnsResolver)
                .setDefaultConnectionConfig(connConfig)
                .build();
    }

    @Override
    public Result<Inet6Address> getInet6Address(URI ipv6resolver) {
        return getResolverResponseForFamily(ipv6resolver, IpFamily.IPV6);
    }

    enum IpFamily {
        IPV4(Inet4Address.class, (String host) -> {
            try {
                return Inet4Address.getByName(host);
            } catch (UnknownHostException javaNetUnknownHostException) {
                // TODO: implement
                throw new UnsupportedOperationException("not yet implemented: [${CLASS_NAME}::${METHOD_NAME}].");
            }
        }),
        IPV6(Inet6Address.class, (String host) -> {
            try {
                return Inet6Address.getByName(host);
            } catch (UnknownHostException javaNetUnknownHostException) {
                // TODO: implement
                throw new UnsupportedOperationException("not yet implemented: [${CLASS_NAME}::${METHOD_NAME}].");
            }
        });

        private final Class<? extends InetAddress> implementation;
        private final Function<String, ? extends InetAddress> resolver;

        IpFamily(Class<? extends InetAddress> implementation, Function<String, ? extends InetAddress> resolver) {
            this.implementation = implementation;
            this.resolver = resolver;
        }

        InetAddress getByName(String host) {
            return resolver.apply(host);
        }
    }

    static class DnsResolver extends SystemDefaultDnsResolver {

        private final Class<? extends InetAddress> inetAddressImpl;

        public DnsResolver(IpFamily ipFamily) {
            this.inetAddressImpl = ipFamily.implementation;
        }

        @Override
        public InetAddress[] resolve(String host) throws UnknownHostException {
            return Arrays.stream(super.resolve(host))
                    .filter(inetAddressImpl::isInstance)
                    .toArray(InetAddress[]::new);
        }
    }
}
