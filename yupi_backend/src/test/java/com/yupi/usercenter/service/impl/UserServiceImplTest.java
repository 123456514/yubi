package com.yupi.usercenter.service.impl;//package com.yupi.usercenter.service.impl;


import com.yupi.yubi_backend.MainApplication;
import com.yupi.yubi_backend.model.entity.User;
import com.yupi.yubi_backend.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.redisson.api.RList;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


//@SpringBootTest(classes = MainApplication.class)
//class UserServiceImplTest {
//
//    @Resource
//    private UserServiceImpl userService;
//    @Resource
//    private RedissonClient redissonClient;
//    //自定义线程池
//    private ExecutorService executorService = new ThreadPoolExecutor(40, 1000, 10000, TimeUnit.MINUTES, new ArrayBlockingQueue<>(10000));
//    /**
//     * 在内存中查询
//     */
//    @Test
//    void searchUserByTags() {
//        List<String> tagNameList = Arrays.asList("Java","Python");
//        List<User> users = userService.searchUserByTags(tagNameList);
//        Assertions.assertNotNull(users);
//    }
//
//    /**
//     * 在数据库中查询
//     */
//    @Test
//    void searchUserByTagsBySQl() {
//        List<String> tagNameList = Arrays.asList("Java","Python");
//        List<User> users = userService.searchUserByTagsBySQl(tagNameList);
//        Assertions.assertNotNull(users);
//    }
//    /**
//     * 数据库中批量插入用户数据
//     */
//    @Test
//    void insertUser() {
//        List<User> list = new ArrayList<>();
//        StopWatch stopWatch = new StopWatch();
//        stopWatch.start();
//        int INSERT_NUMS = 100000;
//        for(int i = 0;i < INSERT_NUMS;i++){
//            User user = new User();
//            user.setUsername("123");
//            user.setUserAccount("2323");
//            user.setAvatarUrl("https://636f-codenav-8grj8px727565176-1256524210.tcb.qcloud.la/img/logo.png");
//            user.setGender(0);
//            user.setUserPassword("`1234555");
//            user.setPhone("123466554");
//            user.setEmail("1232444");
//            user.setUserStatus(0);
//            user.setTags("[]");
//            user.setIsDelete(0);
//            user.setUserRole(0);
//            user.setPlanetCode("255");
//            user.setProfile("2333");
//            list.add(user);
//        }
//        userService.saveBatch(list,10000);
//        // 停止当前时间
//        stopWatch.stop();
//        // 计算插入数据所花时间总数
//        System.out.println(stopWatch.getTotalTimeMillis());
//    }
//    //并发批量插入用户
//    @Test
//    public void doConcurrencyInsertUsers(){
//        StopWatch stopWatch = new StopWatch();
//        stopWatch.start();
//        int batchSize = 5000;
//        int j = 0;
//        List<CompletableFuture<Void>> futureList = new ArrayList<>();
//        for(int i = 0;i < 100;i++){
//            List<User> list = new ArrayList<>();
//            while(true){
//                j++;
//                User user = new User();
//                user.setUsername("1111");
//                user.setUserAccount("1111");
//                user.setAvatarUrl("");
//                user.setGender(0);
//                user.setUserPassword("1111111");
//                user.setPhone("111111");
//                user.setEmail("111111");
//                user.setUserStatus(0);
//                user.setTags("");
//                user.setIsDelete(0);
//                user.setUserRole(0);
//                user.setPlanetCode("111111");
//                user.setProfile("111111");
//                list.add(user);
//                if(j % batchSize == 0){
//                    break;
//                }
//            }
//            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
//                System.out.println("threadName: " + Thread.currentThread().getName());
//                userService.saveBatch(list,batchSize);
//            },executorService);
//            futureList.add(future);
//        }
//        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[]{})).join();
//        stopWatch.stop();
//        System.out.println(stopWatch.getTotalTimeMillis());
//    }
//    @Resource
//    private RedisTemplate redisTemplate;
//    @Test
//    public void insert(){
//
//        ValueOperations valueOperations = redisTemplate.opsForValue();
//        valueOperations.set("zhoujin","1234");
//        System.out.println(valueOperations.get("zhoujin"));
//    }
//    @Test
//    public void test(){
//        // list
//        List<Integer> list = new ArrayList<>();
//        list.add(1);
//        System.out.println(list.get(0));
//        RList<Object> listRedis = redissonClient.getList("list-name");
//        listRedis.add("zhoujin");
//        System.out.println(listRedis.get(0));
//        // map
//        Map<Integer,Integer> map = new HashMap<>();
//        map.put(1,2);
//        System.out.println(map.get(1));
//        RMap<Object, Object> map1 = redissonClient.getMap("map-redis");
//        map1.put("zhoujin","lisi");
//        System.out.println(map1.get("zhoujin"));
//    }
//}