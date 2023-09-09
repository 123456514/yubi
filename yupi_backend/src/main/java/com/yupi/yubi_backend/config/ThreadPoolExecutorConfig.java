package com.yupi.yubi_backend.config;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
@Configuration
public class ThreadPoolExecutorConfig {
    @Bean
    public ThreadPoolExecutor threadPoolExecutor() {
        //自定义线程工厂
        ThreadFactory threadFactory = new ThreadFactory() {
            //初始化线程数为1
            private int count = 1;

            //每当线程池需要创建新线程时，就会调用newThread方法
            // r 表示方法参数 r 应该永远不能为null
            // 如果这个方法被调用的时候传递一个nul参数，就会报错
            @Override
            public Thread newThread(@NotNull Runnable r) {
                Thread thread = new Thread(r);
                thread.setName("线程" + count);
                count++;
                return thread;
            }
        };
        //创建一个新的线程池，线程池核心参数大小为2 最大线程数为4 非核心线程空闲时间为 100s 任务队列为阻塞队列长度为4 使用自定义线程工厂
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(2,4,100,TimeUnit.SECONDS,new ArrayBlockingQueue<>(4),threadFactory);
        return threadPoolExecutor;
    }
}
