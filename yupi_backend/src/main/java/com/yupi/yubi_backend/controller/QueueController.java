package com.yupi.yubi_backend.controller;

import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

@RestController
@Slf4j
@RequestMapping("/queue")
//指定开发，本地环境生效，正式上线以前要把测试去掉，不要把测试暴露出来
@Profile({"dev","local"})
public class QueueController {
    @Resource
    //注入线程池实例
    private ThreadPoolExecutor threadPoolExecutor;
    @GetMapping("/add")
    public void add(String name){
        //异步化执行
        CompletableFuture.runAsync(()->{
            log.info("任务执行中 " + name + ", 执行人： " + Thread.currentThread().getName());
            try {
                Thread.sleep(600000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            //异步任务在threadPoolExecutor中执行
        },threadPoolExecutor);
    }
    @GetMapping("/get")
    public String get(){
        Map<String,Object> map = new HashMap<>();
        //得到线程池的队列长度
        int size = threadPoolExecutor.getQueue().size();
        //将队列长度放到map中
        map.put("队列长度" ,size);
        //得到线程池已接收的任务总数
        long taskCount = threadPoolExecutor.getTaskCount();
        //将任务总数放到map中
        map.put("任务总数" , taskCount);
        //获取线程池，已经完成的任务总数
        long completeTaskCount = threadPoolExecutor.getCompletedTaskCount();
        map.put("已经完成的任务总数",completeTaskCount);
        //得到线程池中正在工作的线程总数
        int activeCount = threadPoolExecutor.getActiveCount();
        map.put("正在工作的线程数",activeCount);
        return JSONUtil.toJsonStr(map);
    }
}

