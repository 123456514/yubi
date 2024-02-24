package com.yupi.yubi_backend.controller;

import cn.hutool.core.io.FileUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.yubi_backend.annotation.AuthCheck;
import com.yupi.yubi_backend.bizmq.BiMessageProducer;
import com.yupi.yubi_backend.common.BaseResponse;
import com.yupi.yubi_backend.common.DeleteRequest;
import com.yupi.yubi_backend.common.ErrorCode;
import com.yupi.yubi_backend.common.ResultUtils;
import com.yupi.yubi_backend.constant.BIConstant;
import com.yupi.yubi_backend.constant.FileConstant;
import com.yupi.yubi_backend.constant.UserConstant;
import com.yupi.yubi_backend.exception.BusinessException;
import com.yupi.yubi_backend.exception.ThrowUtils;
import com.yupi.yubi_backend.manager.AIManage;
import com.yupi.yubi_backend.manager.RedisLimitManager;
import com.yupi.yubi_backend.model.dto.chart.*;
import com.yupi.yubi_backend.model.entity.Chart;
import com.yupi.yubi_backend.model.entity.User;
import com.yupi.yubi_backend.model.enums.ChartStatusEnums;
import com.yupi.yubi_backend.model.vo.BIResponse;
import com.yupi.yubi_backend.service.ChartService;
import com.yupi.yubi_backend.service.UserService;
import com.yupi.yubi_backend.utils.ExcelUtils;
import io.github.briqt.spark4j.SparkClient;
import io.github.briqt.spark4j.constant.SparkApiVersion;
import io.github.briqt.spark4j.exception.SparkException;
import io.github.briqt.spark4j.model.SparkMessage;
import io.github.briqt.spark4j.model.SparkSyncChatResponse;
import io.github.briqt.spark4j.model.request.SparkRequest;
import io.github.briqt.spark4j.model.response.SparkTextUsage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

@RestController
@RequestMapping("/chart")
@Slf4j
public class ChartController {

    @Resource
    private ChartService chartService;

    @Resource
    private UserService userService;

    @Resource
    private AIManage aiManage;
    @Resource
    private RedisLimitManager redisLimitManager;
    @Resource
    private ThreadPoolExecutor threadPoolExecutor;
    @Resource
    private BiMessageProducer biMessageProducer;

    /**
     * 添加图表
     */
    @PostMapping("/add")
    public BaseResponse<Long> addChart(@RequestBody ChartAddRequest chartAddRequest, HttpServletRequest request) {
        if (chartAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return chartService.addChart(chartAddRequest, request);
    }

    /**
     * 删除图表
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteChart(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return chartService.deleteChart(deleteRequest,request);
    }

    /**
     * 更新（仅管理员）
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateChart(@RequestBody ChartUpdateRequest chartUpdateRequest) {
        if (chartUpdateRequest == null || chartUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return chartService.updateChart(chartUpdateRequest);
    }

    /**
     * 根据 id 获取图表信息
     */
    @GetMapping("/get")
    public BaseResponse<Chart> getChartById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
       return chartService.getChartById(id,request);
    }

    /**
     * 分页获取列表（封装类）
     *
     * @param chartQueryRequest
     * @param request
     * @return 返回分页数据
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<Chart>> listChartByPage(@RequestBody ChartQueryRequest chartQueryRequest,
                                                     HttpServletRequest request) {
        return chartService.listChartByPage(chartQueryRequest,request);
    }

    /**
     * 分页获取当前用户创建的资源列表
     */
    @PostMapping("/my/list/page")
    public BaseResponse<Page<Chart>> listMyChartByPage(@RequestBody ChartQueryRequest chartQueryRequest,
                                                       HttpServletRequest request) {
        if (chartQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return chartService.listMyChartByPage(chartQueryRequest, request);
    }


    /**
     * 分页搜索（从 ES 查询，封装类）
     */
    @PostMapping("/search/page")
    public BaseResponse<QueryWrapper<Chart>> searchChartByPage(@RequestBody ChartQueryRequest chartQueryRequest,
                                                               HttpServletRequest request) {
       return chartService.searchChartByPage(chartQueryRequest,request);
    }

    /**
     * 编辑（用户）
     *
     * @param
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editChart(@RequestBody ChartEditRequest chartEditRequest, HttpServletRequest request) {
        if (chartEditRequest == null || chartEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return chartService.editChart(chartEditRequest,request);
    }




    /**
     * 智能上传文件
     *
     * @param multipartFile     上传的文件
     * @param genChartByRequest 表示上传的请求
     * @return 智能返回的结论
     */
    @PostMapping("/gen")
    public BaseResponse<BIResponse> genChartByAi(@RequestPart("file") MultipartFile multipartFile,
                                                 GenChartByAiRequest genChartByRequest, HttpServletRequest request) {
        String goal = genChartByRequest.getGoal();
        String name = genChartByRequest.getName();
        String type = genChartByRequest.getType();
        SparkClient sparkClient=new SparkClient();

        // 设置认证信息
        sparkClient.appid = "587ec41b";
        sparkClient.apiKey = "0bd48335c8aed23c598872abc8adec98";
        sparkClient.apiSecret = "YWVjODYwNDg5NjBlMmQ2MTY4NDg3Mjc3";
        // 消息列表，可以在此列表添加历史对话记录
        List<SparkMessage> messages = new ArrayList<>();
        messages.add(SparkMessage.systemContent("请你扮演我的语文老师李老师，问我讲解问题问题，希望你可以保证知识准确，逻辑严谨。"));
        messages.add(SparkMessage.userContent("鲁迅和周树人小时候打过架吗？"));
        // 构造请求
        SparkRequest sparkRequest = SparkRequest.builder()
        // 消息列表
                .messages(messages)
        // 模型回答的tokens的最大长度,非必传，默认为2048。
        // V1.5取值为[1,4096]
        // V2.0取值为[1,8192]
        // V3.0取值为[1,8192]
                .maxTokens(2048)
                // 核采样阈值。用于决定结果随机性,取值越高随机性越强即相同的问题得到的不同答案的可能性越高 非必传,取值为[0,1],默认为0.5
                .temperature(0.2)
        // 指定请求版本，默认使用最新3.0版本
                .apiVersion(SparkApiVersion.V3_0)
                .build();

        try {
            // 同步调用
            SparkSyncChatResponse chatResponse = sparkClient.chatSync(sparkRequest);
            SparkTextUsage textUsage = chatResponse.getTextUsage();

            System.out.println("\n回答：" + chatResponse.getContent());
            System.out.println("\n提问tokens：" + textUsage.getPromptTokens()
                    + "，回答tokens：" + textUsage.getCompletionTokens()
                    + "，总消耗tokens：" + textUsage.getTotalTokens());
        } catch (SparkException e) {
            System.out.println("发生异常了：" + e.getMessage());
        }
        // 校验
        long size = multipartFile.getSize();
        String originalFilename = multipartFile.getOriginalFilename();
        String suffix = FileUtil.getSuffix(originalFilename);
        ThrowUtils.throwIf(StringUtils.isNotBlank(name) && name.length() > 100, ErrorCode.PARAMS_ERROR, "name过长");
        ThrowUtils.throwIf(multipartFile.getSize() == 0 || multipartFile.isEmpty(), ErrorCode.PARAMS_ERROR, "文件为空");
        ThrowUtils.throwIf(size > FileConstant.MAX_FILE_SIZE, ErrorCode.PARAMS_ERROR, "文件大小超过1M");
        ThrowUtils.throwIf(!BIConstant.VALID_FILE_SUFFIX_LIST.contains(suffix), ErrorCode.PARAMS_ERROR, "文件后缀非法");
        ThrowUtils.throwIf(StringUtils.isBlank(goal), ErrorCode.PARAMS_ERROR, "目标为空");

        //对每个用户限流
        User loginUser = userService.getLoginUser(request);
       //  redisLimitManager.doRateLimit(BIConstant.BI_REDIS_LIMITER_KEY + loginUser);
        //构造用户输入信息
        StringBuilder userInput = new StringBuilder();
        userInput.append("分析请求：").append("\n");
        String userGoal = "";
        if (StringUtils.isNotBlank(goal)) {
            userGoal = goal;
        }
        if (StringUtils.isNotBlank(type)) {
            userGoal += ",请使用" + type;
        }
        userInput.append(userGoal).append("\n");
        userInput.append("原始数据： ").append("\n");
        String csvResult = ExcelUtils.excelToCsv(multipartFile);
        userInput.append(csvResult).append("\n");
        //和AI对话

        String aiRes = aiManage.doChat(BIConstant.BI_MODEL_ID, String.valueOf(userInput));
        //截取数据
        String[] aiData = aiRes.split(BIConstant.BI_SPLIT_STR);
        log.info("aiData len = {} data = {}", aiData.length, aiRes);
        ThrowUtils.throwIf(aiData.length < 3, ErrorCode.SYSTEM_ERROR, "AI生成错误");
        String genChart = aiData[1].trim();
        String genResult = aiData[2].trim();
        //插入数据
        Chart chart = new Chart();
        chart.setGoal(goal);
        chart.setChartName(name);
        chart.setChartType(type);
        chart.setGenChart(genChart);
        chart.setGenResult(genResult);
        chart.setUserId(loginUser.getId());
        chart.setStatus(String.valueOf(ChartStatusEnums.ChartStatusEnum.succeed));
        //返回AI会话结果
        ThrowUtils.throwIf(!chartService.save(chart), ErrorCode.SYSTEM_ERROR, "图表保存失败");//最终 给前端返回AI 回答后的信息
        BIResponse biResponse = new BIResponse();
        biResponse.setGenResult(genResult);
        biResponse.setGenChart(genChart);
        biResponse.setChartId(chart.getId());
        return ResultUtils.success(biResponse);
    }

    /**
     * 上传 诉求和execl文件 智能解析 生成图表和分析结论
     * @param multipartFile
     * @param genChartByAiRequest
     * @param request
     * @return
     */
    @PostMapping("/gen/async")
    public BaseResponse<BIResponse> genChartByAiAsync(@RequestPart("file") MultipartFile multipartFile,
                                                      GenChartByAiRequest genChartByAiRequest, HttpServletRequest request) {

        String chartName = genChartByAiRequest.getName();
        String goal = genChartByAiRequest.getGoal();
        String chartType = genChartByAiRequest.getType();
        // 校验

        ThrowUtils.throwIf(StringUtils.isBlank(goal), ErrorCode.PARAMS_ERROR, "目标为空");
        ThrowUtils.throwIf(StringUtils.isNotBlank(chartName) && chartName.length() > 100, ErrorCode.PARAMS_ERROR, "名称过长");
        String originalFilename = multipartFile.getOriginalFilename();
        ThrowUtils.throwIf(multipartFile.getSize() >  FileConstant.MAX_FILE_SIZE, ErrorCode.PARAMS_ERROR, "文件超过 1M");
        String suffix = FileUtil.getSuffix(originalFilename);
        final List<String> validFileSuffixList = Arrays.asList("xlsx", "xls");
        ThrowUtils.throwIf(!validFileSuffixList.contains(suffix), ErrorCode.PARAMS_ERROR, "文件后缀非法");

        User loginUser = userService.getLoginUser(request);
        // 限流判断，每个用户一个限流器
        redisLimitManager.doRateLimit(BIConstant.BI_REDIS_LIMITER_KEY + loginUser.getId());

        // 构造用户输入
        StringBuilder userInput = new StringBuilder();
        userInput.append("分析需求：").append("\n");
        String userGoal = goal;
        if (StringUtils.isNotBlank(chartType)) {
            userGoal += "，请使用" + chartType;
        }
        userInput.append(userGoal).append("\n");
        userInput.append("原始数据：").append("\n");
        String csvData = ExcelUtils.excelToCsv(multipartFile);
        userInput.append(csvData).append("\n");

        // 插入到数据库
        Chart chart = new Chart();
        chart.setChartName(chartName);
        chart.setGoal(goal);
        chart.setChartData(csvData);
        chart.setChartType(chartType);
        chart.setStatus("wait");
        chart.setUserId(loginUser.getId());
        ThrowUtils.throwIf(!chartService.save(chart), ErrorCode.SYSTEM_ERROR, "图表保存失败");

        CompletableFuture.runAsync(() -> {
            // 先修改图表任务状态为 “执行中”。等执行成功后，修改为 “已完成”、保存执行结果；执行失败后，状态修改为 “失败”，记录任务失败信息。
            Chart updateChart = new Chart();
            updateChart.setId(chart.getId());
            updateChart.setStatus(String.valueOf(ChartStatusEnums.ChartStatusEnum.running));
            if (!chartService.updateById(updateChart)) {
                log.info("chartId = {},更新图表执行中状态失败",chart.getId());
                return;
            }
            // 调用 AI
            String result = aiManage.doChat(BIConstant.BI_MODEL_ID, userInput.toString());
            String[] splits = result.split(BIConstant.BI_SPLIT_STR);
            if (splits.length < 3) {
                log.info("chartId = {},AI生成错误",chart.getId());
                return;
            }
            String genChart = splits[1].trim();
            String genResult = splits[2].trim();
            Chart updateChartResult = new Chart();
            updateChartResult.setId(chart.getId());
            updateChartResult.setGenChart(genChart);
            updateChartResult.setGenResult(genResult);
            updateChartResult.setStatus(String.valueOf(ChartStatusEnums.ChartStatusEnum.succeed));
            if (!chartService.updateById(updateChartResult)) {
                log.info("chartId = {},更新图表执行中状态失败",chart.getId());
            }
        }, threadPoolExecutor);

        BIResponse biResponse = new BIResponse();
        biResponse.setChartId(chart.getId());
        return ResultUtils.success(biResponse);
    }

    /**
     * 智能解析用户的分析诉求和上传图表
     * @param multipartFile 上传文件 execl
     * @param genChartByAiRequest 图表的请求
     * @param request 获得用户的登录态 中的用户信息
     * @return 返回图表生成信息
     */
    @PostMapping("/gen/async/mq")
    public BaseResponse<BIResponse> genChartByAiAsyncMq(@RequestPart("file") MultipartFile multipartFile,
                                                        GenChartByAiRequest genChartByAiRequest, HttpServletRequest request) {
        String chartName = genChartByAiRequest.getName();
        String goal = genChartByAiRequest.getGoal();
        String chartType = genChartByAiRequest.getType();
        // 校验
        ThrowUtils.throwIf(StringUtils.isBlank(goal), ErrorCode.PARAMS_ERROR, "目标为空");
        ThrowUtils.throwIf(StringUtils.isNotBlank(chartName) && chartName.length() > 100, ErrorCode.PARAMS_ERROR, "名称过长");
        String originalFilename = multipartFile.getOriginalFilename();
        ThrowUtils.throwIf(multipartFile.getSize() >  FileConstant.MAX_FILE_SIZE, ErrorCode.PARAMS_ERROR, "文件超过 1M");
        String suffix = FileUtil.getSuffix(originalFilename);
        final List<String> validFileSuffixList = Arrays.asList("xlsx", "xls");
        ThrowUtils.throwIf(!validFileSuffixList.contains(suffix), ErrorCode.PARAMS_ERROR, "文件后缀非法");

        User loginUser = userService.getLoginUser(request);
        // 限流判断，每个用户一个限流器
        redisLimitManager.doRateLimit(BIConstant.BI_REDIS_LIMITER_KEY + loginUser.getId());
        // 构造用户输入
        StringBuilder userInput = new StringBuilder();
        userInput.append("分析需求：").append("\n");
        String userGoal = goal;
        if (StringUtils.isNotBlank(chartType)) {
            userGoal += "，请使用" + chartType;
        }
        userInput.append(userGoal).append("\n");
        userInput.append("原始数据：").append("\n");
        String csvData = ExcelUtils.excelToCsv(multipartFile);
        userInput.append(csvData).append("\n");

        // 插入到数据库
        Chart chart = new Chart();
        chart.setChartName(chartName);
        chart.setGoal(goal);
        chart.setChartData(csvData);
        chart.setChartType(chartType);
        chart.setStatus("wait");
        chart.setUserId(loginUser.getId());
        ThrowUtils.throwIf(!chartService.save(chart), ErrorCode.SYSTEM_ERROR, "图表保存失败");
        long newChartId = chart.getId();
        //利用rabbitMQ
        biMessageProducer.sendMessage(String.valueOf(newChartId));
        BIResponse biResponse = new BIResponse();
        biResponse.setChartId(chart.getId());
        return ResultUtils.success(biResponse);
    }
}
