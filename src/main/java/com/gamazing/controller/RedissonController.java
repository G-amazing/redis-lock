package com.gamazing.controller;

import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 使用redisson框架实现分布式锁
 */
@RestController
public class RedissonController {
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private Redisson redisson;

    @RequestMapping("/RedisStock")
    public String deductStock() throws InterruptedException {
        String lockKey = "lock_key";
        // 获取redis锁对象
        RLock lock = redisson.getLock(lockKey);
        try {
            // 加锁
            lock.tryLock(30, TimeUnit.SECONDS);
            int stock = Integer.parseInt(redisTemplate.opsForValue().get("stock"));
            if (stock > 0) {
                int realStock = stock - 1;
                redisTemplate.opsForValue().set("stock", realStock + "");
                System.out.println("扣除成功,库存剩余:" + realStock);
            } else {
                System.out.println("扣除失败,库存不足");
            }
        } finally {
            // 释放锁
            lock.unlock();
        }
        return "success";
    }
}
