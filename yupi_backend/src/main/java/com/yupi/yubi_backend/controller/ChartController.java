package com.yupi.yubi_backend.controller;

import cn.hutool.core.io.FileUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.yubi_backend.annotation.AuthCheck;
import com.yupi.yubi_backend.common.BaseResponse;
import com.yupi.yubi_backend.common.DeleteRequest;
import com.yupi.yubi_backend.common.ErrorCode;
import com.yupi.yubi_backend.constant.BIConstant;
import com.yupi.yubi_backend.constant.FileConstant;
import com.yupi.yubi_backend.constant.UserConstant;
import com.yupi.yubi_backend.exception.BusinessException;
import com.yupi.yubi_backend.exception.ThrowUtils;
import com.yupi.yubi_backend.model.dto.chart.*;
import com.yupi.yubi_backend.model.entity.Chart;
import com.yupi.yubi_backend.model.vo.BIResponse;
import com.yupi.yubi_backend.service.ChartService;
import com.yupi.yubi_backend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/chart")
@Slf4j
public class ChartController {

    @Resource
    private ChartService chartService;

    @Resource
    private UserService userService;


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
        String chartName = genChartByRequest.getChartName();
        String charType = genChartByRequest.getChartType();
        long size = multipartFile.getSize();
        String originalFilename = multipartFile.getOriginalFilename();
        String suffix = FileUtil.getSuffix(originalFilename);
        ThrowUtils.throwIf(StringUtils.isNotBlank(chartName) && chartName.length() > 100, ErrorCode.PARAMS_ERROR, "name过长");
        ThrowUtils.throwIf(multipartFile.getSize() == 0 || multipartFile.isEmpty(), ErrorCode.PARAMS_ERROR, "文件为空");
        ThrowUtils.throwIf(size > FileConstant.MAX_FILE_SIZE, ErrorCode.PARAMS_ERROR, "文件大小超过1M");
        ThrowUtils.throwIf(!BIConstant.VALID_FILE_SUFFIX_LIST.contains(suffix), ErrorCode.PARAMS_ERROR, "文件后缀非法");
        ThrowUtils.throwIf(StringUtils.isBlank(goal), ErrorCode.PARAMS_ERROR, "目标为空");
        return chartService.genChartByAi(multipartFile,goal, chartName, charType,request);
    }

    /**
     * 上传 诉求和execl文件 智能解析 生成图表和分析结论
     * @param multipartFile
     * @param genChartByAiRequest
     * @param request
     * @return
     */
    @PostMapping("/gen/async")
    public BaseResponse<BIResponse> genChartByAiAsync(@RequestPart("file") MultipartFile multipartFile, GenChartByAiRequest genChartByAiRequest, HttpServletRequest request) {
        String goal = genChartByAiRequest.getGoal();
        String chartName = genChartByAiRequest.getChartName();
        String chartType = genChartByAiRequest.getChartType();
        // 校验
        long size = multipartFile.getSize();
        String originalFilename = multipartFile.getOriginalFilename();
        String suffix = FileUtil.getSuffix(originalFilename);
        ThrowUtils.throwIf(StringUtils.isNotBlank(chartName) && chartName.length() > 100, ErrorCode.PARAMS_ERROR, "name过长");
        ThrowUtils.throwIf(multipartFile.getSize() == 0 || multipartFile.isEmpty(), ErrorCode.PARAMS_ERROR, "文件为空");
        ThrowUtils.throwIf(size > FileConstant.MAX_FILE_SIZE, ErrorCode.PARAMS_ERROR, "文件大小超过1M");
        ThrowUtils.throwIf(!BIConstant.VALID_FILE_SUFFIX_LIST.contains(suffix), ErrorCode.PARAMS_ERROR, "文件后缀非法");
        return chartService.genChartByAiAsync(multipartFile, goal, chartName, chartType, request);
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
        String goal = genChartByAiRequest.getGoal();
        String chartName = genChartByAiRequest.getChartName();
        String chartType = genChartByAiRequest.getChartType();
        // 校验
        long size = multipartFile.getSize();
        String originalFilename = multipartFile.getOriginalFilename();
        String suffix = FileUtil.getSuffix(originalFilename);
        ThrowUtils.throwIf(StringUtils.isNotBlank(chartName) && chartName.length() > 100, ErrorCode.PARAMS_ERROR, "name过长");
        ThrowUtils.throwIf(multipartFile.getSize() == 0 || multipartFile.isEmpty(), ErrorCode.PARAMS_ERROR, "文件为空");
        ThrowUtils.throwIf(size > FileConstant.MAX_FILE_SIZE, ErrorCode.PARAMS_ERROR, "文件大小超过1M");
        ThrowUtils.throwIf(!BIConstant.VALID_FILE_SUFFIX_LIST.contains(suffix), ErrorCode.PARAMS_ERROR, "文件后缀非法");
        return chartService.genChartByAiAsyncMq(multipartFile, goal, chartName, chartType, request);
    }
}
