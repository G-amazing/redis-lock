package com.gamazing.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


/**
 * 使用传统的redis实现分布式锁
 */
@RestController
public class IndexController {
    @Autowired
    private StringRedisTemplate redisTemplate;

    @RequestMapping("/stock")
    public String deductStock() {
        String lockKey = "lock_key";
        String clientId = UUID.randomUUID().toString();
        try {
            Boolean result = redisTemplate.opsForValue().setIfAbsent(lockKey, clientId, 10, TimeUnit.SECONDS);
            if (!result) {
                return "error";
            }
            int stock = Integer.parseInt(redisTemplate.opsForValue().get("stock"));
            if (stock > 0) {
                int realStock = stock - 1;
                redisTemplate.opsForValue().set("stock", realStock + "");
                System.out.println("扣除成功,库存剩余:" + realStock);
            } else {
                System.out.println("扣除失败,库存不足");
            }
        } finally {
            if (clientId.equals(redisTemplate.opsForValue().get(lockKey))) {
                redisTemplate.delete(lockKey);
            }
        }
        return "success";
    }
}
