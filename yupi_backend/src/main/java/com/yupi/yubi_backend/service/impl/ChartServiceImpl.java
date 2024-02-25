package com.yupi.yubi_backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.yubi_backend.bizmq.BiMessageProducer;
import com.yupi.yubi_backend.common.BaseResponse;
import com.yupi.yubi_backend.common.DeleteRequest;
import com.yupi.yubi_backend.common.ErrorCode;
import com.yupi.yubi_backend.common.ResultUtils;
import com.yupi.yubi_backend.constant.BIConstant;
import com.yupi.yubi_backend.constant.CommonConstant;
import com.yupi.yubi_backend.exception.BusinessException;
import com.yupi.yubi_backend.exception.ThrowUtils;
import com.yupi.yubi_backend.manager.AIManage;
import com.yupi.yubi_backend.manager.RedisLimitManager;
import com.yupi.yubi_backend.mapper.ChartMapper;
import com.yupi.yubi_backend.model.dto.chart.*;
import com.yupi.yubi_backend.model.entity.Chart;
import com.yupi.yubi_backend.model.entity.User;
import com.yupi.yubi_backend.model.enums.ChartStatusEnums;
import com.yupi.yubi_backend.model.vo.BIResponse;
import com.yupi.yubi_backend.service.ChartService;
import com.yupi.yubi_backend.utils.ExcelUtils;
import com.yupi.yubi_backend.utils.SqlUtils;
import io.github.briqt.spark4j.SparkClient;
import io.github.briqt.spark4j.constant.SparkApiVersion;
import io.github.briqt.spark4j.exception.SparkException;
import io.github.briqt.spark4j.model.SparkMessage;
import io.github.briqt.spark4j.model.SparkSyncChatResponse;
import io.github.briqt.spark4j.model.request.SparkRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

// todo 支持用户查看原始数据
// todo 支持跳湖在哪到图表编辑页面，去编辑图表
// todo 如果用户上传一个超大的文件怎么办
// todo 如果用户用科技疯狂点击提交，怎么办？
// todo 如果AI的生成太慢 比如需要一分钟 又有很多用户要同时生成，给系统造成了压力，怎么兼顾用户体验和系统的可用性？
@Service
@Slf4j
public class ChartServiceImpl extends ServiceImpl<ChartMapper, Chart> implements ChartService {
    @Resource
    private UserServiceImpl userService;
    @Resource
    private RedisLimitManager redisLimitManager;
    @Resource
    private AIManage aiManage;

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;
    @Resource
    private BiMessageProducer biMessageProducer;


    /**
     * 添加图表
     * @param chartAddRequest
     * @param request
     * @return
     */
    public BaseResponse<Long> addChart(ChartAddRequest chartAddRequest, HttpServletRequest request) {
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartAddRequest, chart);
        User loginUser = userService.getLoginUser(request);
        chart.setUserId(loginUser.getId());
        boolean result = this.save(chart);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newPostId = chart.getId();
        return ResultUtils.success(newPostId);
    }

    /**
     * 删除图表
     * @param deleteRequest
     * @param request
     * @return
     */
    public BaseResponse<Boolean> deleteChart(DeleteRequest deleteRequest, HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        Chart oldChart = this.getById(id);
        ThrowUtils.throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
        if (!oldChart.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = this.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新图表
     * @param chartUpdateRequest
     * @return
     */
    public BaseResponse<Boolean> updateChart(@RequestBody ChartUpdateRequest chartUpdateRequest) {
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartUpdateRequest, chart);
        long id = chartUpdateRequest.getId();
        Chart oldChart = this.getById(id);
        ThrowUtils.throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = this.updateById(chart);
        return ResultUtils.success(result);
    }

    /**
     * 根据id 得到用户的图表
     * @param id
     * @param request
     * @return
     */
    public BaseResponse<Chart> getChartById(long id, HttpServletRequest request) {
        Chart chart = this.getById(id);
        if (chart == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(chart);
    }

    /**
     * 分页查询
     * @param chartQueryRequest
     * @param request
     * @return
     */
    public BaseResponse<Page<Chart>> listChartByPage(@RequestBody ChartQueryRequest chartQueryRequest,
                                              HttpServletRequest request) {
        long current = chartQueryRequest.getCurrent();
        long size = chartQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Chart> chartPage = this.page(new Page<>(current, size),
                getQueryWrapper(chartQueryRequest));
        return ResultUtils.success(chartPage);
    }

    /**
     * 分页获取当前用户创建的资源列表
     * @param chartQueryRequest
     * @param request
     * @return
     */
    @Override
    public BaseResponse<Page<Chart>> listMyChartByPage(ChartQueryRequest chartQueryRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        chartQueryRequest.setUserId(loginUser.getId());
        long current = chartQueryRequest.getCurrent();
        long size = chartQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Chart> chartPage = this.page(new Page<>(current, size),
                getQueryWrapper(chartQueryRequest));
        return ResultUtils.success(chartPage);
    }



    @Override
    public BaseResponse<QueryWrapper<Chart>> searchChartByPage(ChartQueryRequest chartQueryRequest, HttpServletRequest request) {
        long size = chartQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        QueryWrapper<Chart> chartPage = getQueryWrapper(chartQueryRequest);
        return ResultUtils.success(chartPage);
    }

    /**
     * 编辑 用户
     * @param chartEditRequest
     * @param request
     * @return
     */
    @Override
    public BaseResponse<Boolean> editChart(ChartEditRequest chartEditRequest, HttpServletRequest request) {
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartEditRequest, chart);

        User loginUser = userService.getLoginUser(request);
        long id = chartEditRequest.getId();
        // 判断是否存在
        Chart oldChart = this.getById(id);
        ThrowUtils.throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldChart.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = this.updateById(chart);
        return ResultUtils.success(result);
    }

    @Override
    public boolean handleUpdateChartStatus(long chartId, String chartStatus) {
        Chart updateChart = new Chart();
        updateChart.setId(chartId);
        updateChart.setStatus(chartStatus);
        return this.updateById(updateChart);
    }

    @Override
    public BaseResponse<BIResponse> genChartByAi(MultipartFile multipartFile, String goal, String chartName, String charType, HttpServletRequest request) {
        SparkClient sparkClient = new SparkClient();
        //对每个用户限流
        User loginUser = userService.getLoginUser(request);
        redisLimitManager.doRateLimit(BIConstant.BI_REDIS_LIMITER_KEY + loginUser);
        //构造用户输入信息
        StringBuilder userInput = new StringBuilder();
        userInput.append("分析请求：").append("\n");
        String userGoal = "";
        if (StringUtils.isNotBlank(goal)) {
            userGoal = goal;
        }
        if (StringUtils.isNotBlank(charType)) {
            userGoal += ",请使用" + charType;
        }
        userInput.append(userGoal).append("\n");
        userInput.append("原始数据： ").append("\n");
        String csvResult = ExcelUtils.excelToCsv(multipartFile);
        userInput.append(csvResult).append("\n");
        // 和AI对话
        sparkClient.appid = "587ec41b";
        sparkClient.apiKey = "0bd48335c8aed23c598872abc8adec98";
        sparkClient.apiSecret = "YWVjODYwNDg5NjBlMmQ2MTY4NDg3Mjc3";
        // 消息列表，可以在此列表添加历史对话记录
        List<SparkMessage> messages = new ArrayList<>();
        messages.add(SparkMessage.systemContent(BIConstant.prompt));
        messages.add(SparkMessage.userContent(userInput.toString()));
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
            String aiData = chatResponse.getContent();
            String genResult = aiData.substring(aiData.indexOf("：") + 2, aiData.lastIndexOf("。"));
            String genChart = aiData.substring(aiData.indexOf("{") , aiData.lastIndexOf("}") + 1);
            // todo 在这里要判断 如果此时返回的数据不符合要求和格式干怎么办
            Chart chart = new Chart();
            chart.setGoal(goal);
            chart.setChartName(chartName);
            chart.setChartType(charType);
            chart.setGenChart(genChart);
            chart.setGenResult(genResult);
            chart.setUserId(loginUser.getId());
            chart.setChartData(csvResult);
            chart.setStatus(String.valueOf(ChartStatusEnums.ChartStatusEnum.succeed));
            //返回AI会话结果
            ThrowUtils.throwIf(!save(chart), ErrorCode.SYSTEM_ERROR, "图表保存失败");//最终 给前端返回AI 回答后的信息
            BIResponse biResponse = new BIResponse();
            biResponse.setGenResult(genResult);
            biResponse.setGenChart(genChart);
            biResponse.setChartId(chart.getId());
            return ResultUtils.success(biResponse);
        } catch (SparkException e) {
            System.out.println("发生异常了：" + e.getMessage());
        }
        return null;
    }

    @Override
    public BaseResponse<BIResponse> genChartByAiAsync(MultipartFile multipartFile, String goal, String chartName, String chartType, HttpServletRequest request) {
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
        ThrowUtils.throwIf(!save(chart), ErrorCode.SYSTEM_ERROR, "图表保存失败");

        CompletableFuture.runAsync(() -> {
            // 先修改图表任务状态为 “执行中”。等执行成功后，修改为 “已完成”、保存执行结果；执行失败后，状态修改为 “失败”，记录任务失败信息。
            Chart updateChart = new Chart();
            updateChart.setId(chart.getId());
            updateChart.setStatus(String.valueOf(ChartStatusEnums.ChartStatusEnum.running));
            if (!updateById(updateChart)) {
                log.info("chartId = {},更新图表执行中状态失败", chart.getId());
                return;
            }
            // 调用 AI
            String result = aiManage.doChat(BIConstant.BI_MODEL_ID, userInput.toString());
            String[] splits = result.split(BIConstant.BI_SPLIT_STR);
            if (splits.length < 3) {
                log.info("chartId = {},AI生成错误", chart.getId());
                return;
            }
            String genChart = splits[1].trim();
            String genResult = splits[2].trim();
            Chart updateChartResult = new Chart();
            updateChartResult.setId(chart.getId());
            updateChartResult.setGenChart(genChart);
            updateChartResult.setGenResult(genResult);
            updateChartResult.setStatus(String.valueOf(ChartStatusEnums.ChartStatusEnum.succeed));
            if (updateById(updateChartResult)) {
                log.info("chartId = {},更新图表执行中状态失败", chart.getId());
            }
        }, threadPoolExecutor);

        BIResponse biResponse = new BIResponse();
        biResponse.setChartId(chart.getId());
        return ResultUtils.success(biResponse);
    }

    @Override
    public BaseResponse<BIResponse> genChartByAiAsyncMq(MultipartFile multipartFile, String goal, String chartName, String chartType, HttpServletRequest request) {
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
        ThrowUtils.throwIf(!save(chart), ErrorCode.SYSTEM_ERROR, "图表保存失败");
        long newChartId = chart.getId();
        //利用rabbitMQ
        biMessageProducer.sendMessage(String.valueOf(newChartId));
        BIResponse biResponse = new BIResponse();
        biResponse.setChartId(chart.getId());
        return ResultUtils.success(biResponse);
    }

    /**
     * 获取查询包装类
     *
     * @param
     * @return
     */

    private QueryWrapper<Chart> getQueryWrapper(ChartQueryRequest chartQueryRequest) {
        QueryWrapper<Chart> queryWrapper = new QueryWrapper<>();
        if (chartQueryRequest == null) {
            return queryWrapper;
        }
        Long id = chartQueryRequest.getId();
        String goal = chartQueryRequest.getGoal();
        String name = chartQueryRequest.getName();
        String chartType = chartQueryRequest.getChartType();
        Long userId = chartQueryRequest.getUserId();
        String sortField = chartQueryRequest.getSortField();
        String sortOrder = chartQueryRequest.getSortOrder();

        queryWrapper.eq(id != null && id > 0, "id", id);
        queryWrapper.eq(StringUtils.isNotBlank(goal), "goal", goal);
        queryWrapper.eq(StringUtils.isNotBlank(chartType), "chartType", chartType);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq("isDelete", false);
        queryWrapper.eq(StringUtils.isNotBlank(name), "name", name);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }
}




