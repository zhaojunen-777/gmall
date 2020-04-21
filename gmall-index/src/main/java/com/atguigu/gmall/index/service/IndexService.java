package com.atguigu.gmall.index.service;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.index.config.GmallCache;
import com.atguigu.gmall.index.feign.GmallPmsClient;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.vo.CategoryVO;
import org.redisson.api.RCountDownLatch;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class IndexService {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private GmallPmsClient pmsClient;

    @Autowired
    private RedissonClient redissonClient;

    private static final String KEY_PREFIX = "index:cates:";

    public List<CategoryEntity> queryLvl1Categories() {
        Resp<List<CategoryEntity>> categoriesResp = pmsClient.queryCategoriesByLevelOrPid(1, null);
        List<CategoryEntity> categoryEntities = categoriesResp.getData();
        return categoryEntities;
    }
    @GmallCache(value = "index:cates:",timeout = 7200,bound = 100,lockName = "lock")
    public List<CategoryVO> queryCategoriesWithSub(Long pid) {
//        String cateJson = stringRedisTemplate.opsForValue().get(KEY_PREFIX + pid);
//        if (StringUtils.isNotBlank(cateJson)) {
//            return JSON.parseArray(cateJson,CategoryVO.class);
//        }
//        RLock lock = redissonClient.getLock("lock" + pid);
//        lock.lock();
//        String cateJson2 = stringRedisTemplate.opsForValue().get(KEY_PREFIX + pid);
//        if (StringUtils.isNotBlank(cateJson)) {
//            lock.unlock();
//            return JSON.parseArray(cateJson,CategoryVO.class);
//        }
        Resp<List<CategoryVO>> listResp = pmsClient.queryCategoriesWithSub(pid);
        List<CategoryVO> categoryVOS = listResp.getData();
//        stringRedisTemplate.opsForValue().set(KEY_PREFIX+pid,JSON.toJSONString(categoryVOS),new Random().nextInt(5), TimeUnit.DAYS);
//        lock.unlock();
        return categoryVOS;
    }

    public synchronized void testLock() {
        RLock lock = redissonClient.getLock("lock");
        lock.lock();
        String numString = stringRedisTemplate.opsForValue().get("num");
        if (numString == null) {
            return;
        }
        Integer num = new Integer(numString);
        stringRedisTemplate.opsForValue().set("num",String.valueOf(++num));
        lock.unlock();
    }
    public synchronized void testLock1() {
        String uuid = UUID.randomUUID().toString();
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent("lock", uuid,5,TimeUnit.SECONDS);
        if (flag) {
            String numString = stringRedisTemplate.opsForValue().get("num");
            if (numString == null) {
                return;
            }
            Integer num = new Integer(numString);
            stringRedisTemplate.opsForValue().set("num",String.valueOf(++num));
            String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
            this.stringRedisTemplate.execute(new DefaultRedisScript<>(script, Long.class), Arrays.asList("lock"), uuid);
//            String lock = stringRedisTemplate.opsForValue().get("lock");
//            if (StringUtils.equals(lock,uuid)) {
//                stringRedisTemplate.delete("lock");
//            }

        }else {
            try {
                TimeUnit.SECONDS.sleep(1);
                testLock();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public String testRead() {
        RReadWriteLock rwLock = redissonClient.getReadWriteLock("rwLock");
        rwLock.readLock().lock(10,TimeUnit.SECONDS);
        String msg = stringRedisTemplate.opsForValue().get("msg");
        return "读取了数据："+msg;
    }

    public String testWrite() {
        RReadWriteLock rwLock = redissonClient.getReadWriteLock("rwLock");
        rwLock.writeLock().lock(10,TimeUnit.SECONDS);
        String uuid = UUID.randomUUID().toString();
        stringRedisTemplate.opsForValue().set("msg",uuid);
        return "写了数据："+uuid;


    }

    public String testLatch() throws InterruptedException {
        RCountDownLatch latch = redissonClient.getCountDownLatch("latch");
        latch.trySetCount(6);
        latch.await();
        return "班长锁门";
    }

    public String testCountdown() {
        RCountDownLatch latch = redissonClient.getCountDownLatch("latch");
        latch.countDown();
        return "数量减1";
    }
}
