package com.yupi.yubi_backend.manager;

import com.yupi.yubi_backend.common.ErrorCode;
import com.yupi.yubi_backend.exception.BusinessException;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

//@Service
///**
// * 提供限流服务
// */
//public class RedisLimitManager {
//    @Resource
//    private RedissonClient redissonClient;
//
//    /**
//     * 限流操作
//     *
//     * @param key 区分不同的限流器 比如不同的用户id 应该分别统计
//     */
//    public void doRateLimit(String key){
//        RRateLimiter rateLimiter = redissonClient.getRateLimiter(key);
//        rateLimiter.trySetRate(RateType.OVERALL,2,1, RateIntervalUnit.SECONDS);
//        //每当一个操作来了之后，请求一个令牌
//        boolean canOp = rateLimiter.tryAcquire(1);
//        if(!canOp){
//            throw new BusinessException(ErrorCode.TOO_MANY_REQUEST);
//        }
//    }
//}
