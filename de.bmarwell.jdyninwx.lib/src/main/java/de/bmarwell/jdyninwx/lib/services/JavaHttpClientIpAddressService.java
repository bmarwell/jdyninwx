package de.bmarwell.jdyninwx.lib.services;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;

public class JavaHttpClientIpAddressService implements IpAddressService {

    private Duration requestTimeout = Duration.ofMillis(2000L);
    private Duration connectTimeout = Duration.ofMillis(2000L);

    public HttpRequest createHttpRequest(URI resolver) {
        return java.net.http.HttpRequest.newBuilder()
                .uri(resolver)
                .GET()
                .timeout(requestTimeout)
                .header("accept", "text/plain")
                .build();
    }

    public HttpClient createHttpClient() {
        return HttpClient.newBuilder().connectTimeout(connectTimeout).build();
    }

    @Override
    public Result<Inet4Address> getInet4Address(URI ipv4resolver) {
        HttpRequest httpRequest = createHttpRequest(ipv4resolver);

        try {
            HttpClient httpClient = createHttpClient();
            HttpResponse<String> response = httpClient.send(httpRequest, BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                // ?
            }

            String responseBody = response.body();
            Inet4Address inet4Address = (Inet4Address) Inet4Address.getByName(responseBody);

            return Result.ok(inet4Address);
        } catch (IOException e) {
            return Result.fail(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    @Override
    public IpAddressService withRequestTimeout(Duration requestTimeout) {
        this.requestTimeout = requestTimeout;
        return this;
    }

    @Override
    public IpAddressService withConnectTimeout(Duration connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }
}
