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
    long BI_MODEL_ID = 1651468516836098050L;
    /**
     * 间隔字符串
     */
    String BI_SPLIT_STR = "\n";
    /**
     * 对讯飞星火的 AI的预设
     */
    String prompt = "现在你是一名资深的数据分析师，我将给你一个CSV格式的文件内容，请你分析之后把结论,结论描述的越详细越好，把我给你的数据通过Echarts生成的代码转化成为 Json格式";
}
