package io.github.ilyascheglov.memcached_aspect_starter.aspect.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MemCacheEvict {

    String prefix() default "";

    String keyArgumentName() default "";
}
