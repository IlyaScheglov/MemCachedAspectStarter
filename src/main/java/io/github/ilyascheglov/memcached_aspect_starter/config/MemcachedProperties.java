package io.github.ilyascheglov.memcached_aspect_starter.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.memcached")
public class MemcachedProperties {

    private String host;

    private int port;

    private int ttlSeconds;

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public int getTtlSeconds() {
        return ttlSeconds;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setTtlSeconds(int ttlSeconds) {
        this.ttlSeconds = ttlSeconds;
    }
}
