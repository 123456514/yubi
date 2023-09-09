package com.yupi.yubi_backend.constant;


import java.util.Arrays;
import java.util.List;

public interface BIConstant {
    /**
     * 上传文件后缀类型
     */
    List<String> VALID_FILE_SUFFIX_LIST = Arrays.asList("xlsx","xls","csv");
    /**
     * 限流
     */
    String BI_REDIS_LIMITER_KEY = "getChartByAi-";
    /**
     * AI模型Id
     */
    long BI_MODEL_ID = 1689914240074297346L;
    /**
     * 间隔字符串
     */
    String BI_SPLIT_STR = "【【【【【";
}
