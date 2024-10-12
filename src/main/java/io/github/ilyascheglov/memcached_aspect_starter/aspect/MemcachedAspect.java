package io.github.ilyascheglov.memcached_aspect_starter.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.ilyascheglov.memcached_aspect_starter.aspect.annotations.MemCacheEvict;
import io.github.ilyascheglov.memcached_aspect_starter.aspect.annotations.MemCachePut;
import io.github.ilyascheglov.memcached_aspect_starter.aspect.annotations.MemCacheable;
import net.spy.memcached.MemcachedClient;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;

@Aspect
public class MemcachedAspect {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final MemcachedClient memcachedClient;
    private final int ttlSeconds;

    public MemcachedAspect(MemcachedClient memcachedClient, int ttlSeconds) {
        this.memcachedClient = memcachedClient;
        this.ttlSeconds = ttlSeconds;
    }

    @Around("@annotation(io.github.ilyascheglov.memcached_aspect_starter.aspect.annotations.MemCacheable)")
    public Object cacheable(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Class cacheType = methodSignature.getReturnType();
        String cacheKeyPrefix = methodSignature.getMethod().getAnnotation(MemCacheable.class).prefix();
        String cacheKeyArgumentName = methodSignature.getMethod().getAnnotation(MemCacheable.class).keyArgumentName();

        String key = createCacheKey(cacheKeyPrefix, cacheKeyArgumentName,
                methodSignature.getParameterNames(), joinPoint.getArgs());

        Object cacheValue = memcachedClient.get(key);
        if (cacheValue != null) {
            return objectMapper.readValue((String) cacheValue, cacheType);
        } else{
            return proceedResult(joinPoint, key);
        }
    }

    @Around("@annotation(io.github.ilyascheglov.memcached_aspect_starter.aspect.annotations.MemCachePut)")
    public Object cachePut(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        String cacheKeyPrefix = methodSignature.getMethod().getAnnotation(MemCachePut.class).prefix();
        String cacheKeyArgumentName = methodSignature.getMethod().getAnnotation(MemCachePut.class).keyArgumentName();

        String key = createCacheKey(cacheKeyPrefix, cacheKeyArgumentName,
                methodSignature.getParameterNames(), joinPoint.getArgs());
        return proceedResult(joinPoint, key);
    }

    @Before("@annotation(io.github.ilyascheglov.memcached_aspect_starter.aspect.annotations.MemCacheEvict)")
    public void cacheEvict(JoinPoint joinPoint) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        String cacheKeyPrefix = methodSignature.getMethod().getAnnotation(MemCacheEvict.class).prefix();
        String cacheKeyArgumentName = methodSignature.getMethod().getAnnotation(MemCacheEvict.class).keyArgumentName();

        String key = createCacheKey(cacheKeyPrefix, cacheKeyArgumentName,
                methodSignature.getParameterNames(), joinPoint.getArgs());
        memcachedClient.delete(key);
    }

    private Object proceedResult(ProceedingJoinPoint joinPoint, String key) throws Throwable {
        Object result = joinPoint.proceed();
        memcachedClient.set(key, ttlSeconds, objectMapper.writeValueAsString(result));
        return result;
    }

    private String createCacheKey(String cacheKeyPrefix, String cacheKeyArgumentName,
                                  String[] argsNames, Object[] args) {
        String keyFromArgs = "";

        for (int i = 0; i < argsNames.length; i++) {
            if (argsNames[i].equals(cacheKeyArgumentName)) {
                keyFromArgs = args[i].toString();
                break;
            }
        }

        if (keyFromArgs.isEmpty()) {
            return cacheKeyPrefix;
        }

        return cacheKeyPrefix + "::" + keyFromArgs;
    }
}
