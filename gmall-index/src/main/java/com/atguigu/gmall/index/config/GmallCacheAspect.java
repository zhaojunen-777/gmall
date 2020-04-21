package com.atguigu.gmall.index.config;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Component
@Aspect
public class GmallCacheAspect {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Around("@annotation(com.atguigu.gmall.index.config.GmallCache)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature)joinPoint.getSignature();
        Method method = signature.getMethod();
        GmallCache gmallCache = method.getAnnotation(GmallCache.class);
        Class returnType = signature.getReturnType();
        List<Object> args = Arrays.asList(joinPoint.getArgs());
        String prefix = gmallCache.value();
        String key = prefix + args;
        Object cache = getCache(key, returnType);
        if (cache != null) {
            return cache;
        }
        String lockName = gmallCache.lockName();
        RLock fairLock = redissonClient.getFairLock(lockName);
        fairLock.lock();
        Object cache1 = getCache(key, returnType);
        if (cache != null) {
            fairLock.unlock();
            return cache;
        }
        Object result = joinPoint.proceed(joinPoint.getArgs());
        stringRedisTemplate.opsForValue().set(key,JSON.toJSONString(result),new Random().nextInt(gmallCache.bound()),TimeUnit.DAYS);
        fairLock.unlock();
        return result;
    }
    private Object getCache(String key,Class returnType) {
        String jsonString = stringRedisTemplate.opsForValue().get(key);
        if (StringUtils.isNotBlank(jsonString)) {
            return JSON.parseObject(jsonString,returnType);
        }
        return null;
    }



}
