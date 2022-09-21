package net.akaritakai.stream.handler;

import io.vertx.core.http.HttpServerRequest;

import java.net.InetAddress;
import java.net.UnknownHostException;

public final class Util {
    private Util() {
    }

    public static InetAddress ANY;

    public static InetAddress getIpAddressFromRequest(HttpServerRequest request) {
        try {
            String headerValue = request.getHeader("X-Forwarded-For");
            if (headerValue == null) {
                return InetAddress.getByName(request.remoteAddress().host());
            }
            String[] values = headerValue.split(",");
            if (values.length >= 2) {
                // The last value will be CloudFront's IP
                // The 2nd to last will be the IP talking to CloudFront
                return InetAddress.getByName(values[values.length - 2].trim());
            } else if (values.length == 1) {
                return InetAddress.getByName(values[0]); // Shouldn't be possible, but might happen in a dev setup
            } else {
                return InetAddress.getByName(request.remoteAddress().host()); // Dev setup
            }
        } catch (Exception e) {
            return null; // No valid IP address found
        }
    }

    static {
        try {
            ANY = InetAddress.getByAddress(new byte[] {0,0,0,0});
        } catch (UnknownHostException e) {
            throw new Error(e);
        }
    }
}
