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
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
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

    enum IpFamily {
        IPV4,
        IPV6
    }

    /**
     * Constructs a default, immutable instance.
     */
    public ApacheHttpClientIpAddressService() {
        // cdi etc
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

    @SuppressWarnings("unchecked")
    private <T extends InetAddress> Result<T> getResolverResponseForFamily(URI ipv4resolver, IpFamily ipFamily) {
        try (CloseableHttpClient client = createApacheHttpClient(ipFamily)) {
            HttpGet getIp4 = new HttpGet(ipv4resolver);
            String execute = client.execute(getIp4, new BasicHttpClientResponseHandler());

            return Result.ok((T) Inet4Address.getByName(execute));
        } catch (IOException | IllegalStateException e) {
            return Result.fail(e);
        }
    }

    @Override
    public Result<Inet4Address> getInet4Address(URI ipv4resolver) {
        return getResolverResponseForFamily(ipv4resolver, IpFamily.IPV4);
    }

    @Override
    public Result<Inet6Address> getInet6Address(URI ipv6resolver) {
        return getResolverResponseForFamily(ipv6resolver, IpFamily.IPV6);
    }

    static class DnsResolver extends SystemDefaultDnsResolver {

        private final Class<?> inetAddressImpl;

        public DnsResolver(IpFamily ipFamily) {
            this.inetAddressImpl = switch (ipFamily) {
                case IPV6 -> Inet6Address.class;
                case IPV4 -> Inet4Address.class;};
        }

        @Override
        public InetAddress[] resolve(String host) throws UnknownHostException {
            return Arrays.stream(super.resolve(host))
                    .filter(inetAddressImpl::isInstance)
                    .toArray(InetAddress[]::new);
        }
    }
}
