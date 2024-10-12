package io.github.ilyascheglov.memcached_aspect_starter.config;

import io.github.ilyascheglov.memcached_aspect_starter.aspect.MemcachedAspect;
import net.spy.memcached.MemcachedClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.net.InetSocketAddress;

@Configuration
@EnableConfigurationProperties(MemcachedProperties.class)
public class MemcachedStarterConfig {

    @Bean
    @ConditionalOnMissingBean
    public MemcachedClient memcachedClient(MemcachedProperties memcachedProperties) throws IOException {
        return new MemcachedClient(new InetSocketAddress(memcachedProperties.getHost(),
                memcachedProperties.getPort()));
    }

    @Bean
    @ConditionalOnMissingBean
    public MemcachedAspect memcachedAspect(MemcachedClient memcachedClient, MemcachedProperties memcachedProperties) {
        return new MemcachedAspect(memcachedClient, memcachedProperties.getTtlSeconds());
    }
}
