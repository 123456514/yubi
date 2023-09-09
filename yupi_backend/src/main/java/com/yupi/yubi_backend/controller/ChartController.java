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
import com.yupi.yubi_backend.mapper.ChartMapper;
import com.yupi.yubi_backend.model.dto.chart.*;
import com.yupi.yubi_backend.model.entity.Chart;
import com.yupi.yubi_backend.model.entity.User;
import com.yupi.yubi_backend.model.enums.ChartStatusEnums;
import com.yupi.yubi_backend.model.vo.BIResponse;
import com.yupi.yubi_backend.service.ChartService;
import com.yupi.yubi_backend.service.UserService;
import com.yupi.yubi_backend.utils.ExcelUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
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

    private ChartMapper chartMapper;

    @Resource
    private AIManage aiManage;
//    @Resource
//    private RedisLimitManager redisLimitManager;
    @Resource
    private ThreadPoolExecutor threadPoolExecutor;
    @Resource
    private BiMessageProducer biMessageProducer;

    /**
     * 添加图表
     *
     * @param chartAddRequest 表示的是 添加图表的请求
     * @param request         可以通过 这个对象 得到此时登录用户的身份信息
     * @return 返回添加到数据库中图表的id
     */
    @PostMapping("/add")
    public BaseResponse<Long> addChart(@RequestBody ChartAddRequest chartAddRequest, HttpServletRequest request) {
        //判断添加图表的请求是否为空，为空返回参数异常
        if (chartAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return chartService.addChart(chartAddRequest, request);
    }

    /**
     * 删除图表
     *
     * @param deleteRequest 删除请求  根据图表的id 删除数据库中指定的图表
     * @param request       得到用户的身份信息
     * @return 返回是否删除成功
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteChart(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        //如果此时的删除请求为空，或者是此时的删除请求中的id < 0 那么就报参数异常
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return chartService.deleteChart(deleteRequest,request);
    }

    /**
     * 更新（仅管理员）
     *
     * @param chartUpdateRequest 更新图表信息
     * @return 返回是否更新成功
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateChart(@RequestBody ChartUpdateRequest chartUpdateRequest) {
        //如果此时的更新请求为空，或者 更新请求的id < 0 就返回参数异常
        if (chartUpdateRequest == null || chartUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return chartService.updateChart(chartUpdateRequest);
    }

    /**
     * 根据 id 获取
     *
     * @param id 图表Id
     * @return 返回图表信息
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
                                                 GenChartByRequest genChartByRequest, HttpServletRequest request) {
        //参数校验
        ThrowUtils.throwIf(multipartFile == null, ErrorCode.PARAMS_ERROR, "文件为空");
        java.lang.String goal = genChartByRequest.getGoal();
        ThrowUtils.throwIf(StringUtils.isBlank(goal), ErrorCode.PARAMS_ERROR, "目标为空");
        java.lang.String chartName = genChartByRequest.getChartName();
        ThrowUtils.throwIf(StringUtils.isNotBlank(chartName) && chartName.length() > 100, ErrorCode.PARAMS_ERROR, "name过长");
        java.lang.String chartType = genChartByRequest.getChartType();
        //校验文件
        long size = multipartFile.getSize();
        ThrowUtils.throwIf(size > FileConstant.MAX_FILE_SIZE, ErrorCode.PARAMS_ERROR, "文件大小超过1M");
        String originalFilename = multipartFile.getOriginalFilename();
        String suffix = FileUtil.getSuffix(originalFilename);
        ThrowUtils.throwIf(!BIConstant.VALID_FILE_SUFFIX_LIST.contains(suffix), ErrorCode.PARAMS_ERROR, "文件格式非法");
//获取用户信息
        User loginUser = userService.getLoginUser(request);
        //分布式限流
//        final String key = BIConstant.BI_REDIS_LIMITER_KEY + loginUser;
//        redisLimitManager.doRateLimit(key);
        //得到输入的信息
        StringBuilder userInput = new StringBuilder();
        userInput.append("分析请求：").append("\n");
        String userGoal = goal;
        if (StringUtils.isNotBlank(chartType)) {
            userGoal += ",请使用" + chartType;
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
        chart.setChartName(chartName);
        //chart.setChartData(csvResult);
        chart.setChartType(chartType);
        chart.setGenChart(genChart);
        chart.setGenResult(genResult);
        chart.setUserId(loginUser.getId());
        chart.setStatus(String.valueOf(ChartStatusEnums.ChartStatusEnum.succeed));

        //把用户发送的请求中的表的具体数据存储到一个单独的表里
        //首先创建一张单独的数据表，在这个表中有这个表的id 和 图表的字段类型和字段值
        //得到csv数据中的内容，就是表头,然后根据varchar(255)
//        String[] split = csvResult.split("\n");
//        log.info(split[0]);
//        String[] split1 = split[0].split(",");
//        List<SqlEntity> list = new ArrayList<>();
//        for (String s : split1) {
//            SqlEntity sqlEntity = new SqlEntity();
//            sqlEntity.setColumnType("varchar(255)");
//            sqlEntity.setColumnName(s);
//            list.add(sqlEntity);
//        }
//        chartMapper.createChartTable(chart.getId(),list);
//        //然后把数据写到单独的数据表中
//        // 把 csv 变成一个二维数组
//        List<String> list1 = new ArrayList<>();
//        list1.addAll(Arrays.asList(split1));
//        String[][] strs = new String[split.length - 1][split1.length];
//        int k = 0;
//        for(int i = 1;i < split.length;i++){
//            String[] split2 = split[i].split(",");
//
//            for(int j = 0;j < split2.length;j++){
//                strs[k][j] = split2[j];
//            }
//            k++;
//        }
//        List<Map<String, Object>> listMap = new ArrayList<>();
//        for (int i = 0; i < strs.length; i++) {
//            Map<String, Object> hashMap = new HashMap<>();
//            int p = 0;
//            for(int j = 0;j < split1.length;j++){
//                hashMap.put(split1[j], strs[i][p]);
//                p++;
//            }
//            listMap.add(hashMap);
//        }
//
//        chartMapper.insertBatchChart(chart.getId(),Arrays.asList(split1),listMap);
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
     * @param genChartByRequest
     * @param request
     * @return
     */
    @PostMapping("/gen/async")
    public BaseResponse<BIResponse> genChartByAiAsync(@RequestPart("file") MultipartFile multipartFile,
                                                      GenChartByRequest genChartByRequest, HttpServletRequest request) {
        String chartName = genChartByRequest.getChartName();
        String goal = genChartByRequest.getGoal();
        String chartType = genChartByRequest.getChartType();
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
        //redisLimitManager.doRateLimit(BIConstant.BI_REDIS_LIMITER_KEY + loginUser.getId());

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

        // todo 建议处理任务队列满了后，抛异常的情况
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
     * @param genChartByRequest 图表的请求
     * @param request 获得用户的登录态 中的用户信息
     * @return 返回图表生成信息
     */
    @PostMapping("/gen/async/mq")
    public BaseResponse<BIResponse> genChartByAiAsyncMq(@RequestPart("file") MultipartFile multipartFile,
                                                      GenChartByRequest genChartByRequest, HttpServletRequest request) {
        String chartName = genChartByRequest.getChartName();
        String goal = genChartByRequest.getGoal();
        String chartType = genChartByRequest.getChartType();
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
        //redisLimitManager.doRateLimit(BIConstant.BI_REDIS_LIMITER_KEY + loginUser.getId());

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
