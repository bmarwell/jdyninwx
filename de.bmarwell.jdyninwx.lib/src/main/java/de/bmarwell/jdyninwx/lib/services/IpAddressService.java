package de.bmarwell.jdyninwx.lib.services;

import java.io.Serializable;
import java.net.Inet4Address;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

public interface IpAddressService extends Serializable {

    default Optional<Inet4Address> getFirstResolvedInet4Address(List<URI> ipv4resolvers) {
        return ipv4resolvers.stream()
                .map(this::getInet4Address)
                .filter(Result::isSuccess)
                .flatMap(Result::stream)
                .findFirst();
    }

    Result<Inet4Address> getInet4Address(URI ipv4resolver);

    IpAddressService withRequestTimeout(Duration timeout);

    IpAddressService withConnectTimeout(Duration timeout);
}
