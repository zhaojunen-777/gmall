package com.atguigu.gmall.index.config;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface GmallCache {

    String value() default "";

    int timeout() default 30;

    int bound() default 5;

    String lockName() default "lock";
}
