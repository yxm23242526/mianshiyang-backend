package com.xm.mianshiyoung.annotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * 分布式定时任务锁
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedLock {

    /**
     * 锁的名称
     */
    String key();


    /**
     * 持锁时间，默认30秒
     */
    long leaseTime() default 30000;

    /**
     * 等待时间，默认10秒
     */
    long waitTime() default 30000;


    /**
     * 时间单位，默认为毫秒
     */
    TimeUnit timeUnit() default TimeUnit.MILLISECONDS;
}
