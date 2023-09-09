package com.yupi.yubi_backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.yubi_backend.common.BaseResponse;
import com.yupi.yubi_backend.common.DeleteRequest;
import com.yupi.yubi_backend.common.ErrorCode;
import com.yupi.yubi_backend.common.ResultUtils;
import com.yupi.yubi_backend.constant.CommonConstant;
import com.yupi.yubi_backend.exception.BusinessException;
import com.yupi.yubi_backend.exception.ThrowUtils;
import com.yupi.yubi_backend.mapper.ChartMapper;
import com.yupi.yubi_backend.model.dto.chart.ChartAddRequest;
import com.yupi.yubi_backend.model.dto.chart.ChartEditRequest;
import com.yupi.yubi_backend.model.dto.chart.ChartQueryRequest;
import com.yupi.yubi_backend.model.dto.chart.ChartUpdateRequest;
import com.yupi.yubi_backend.model.entity.Chart;
import com.yupi.yubi_backend.model.entity.User;
import com.yupi.yubi_backend.service.ChartService;
import com.yupi.yubi_backend.utils.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
* @author 26702
* @description 针对表【chart(图表信息表)】的数据库操作Service实现
* @createDate 2023-08-09 21:10:18
*/
@Service
@Slf4j
public class ChartServiceImpl extends ServiceImpl<ChartMapper, Chart>
    implements ChartService{
    @Resource
    private UserServiceImpl userService;


    /**
     * 添加图表
     * @param chartAddRequest
     * @param request
     * @return
     */
    public  BaseResponse<Long> addChart(ChartAddRequest chartAddRequest, HttpServletRequest request){
        Chart chart = new Chart();
        // 把添加图表对象中的属性值 ,copy到 chart中
        BeanUtils.copyProperties(chartAddRequest, chart);
        //得到此时登录的用户信息
        User loginUser = userService.getLoginUser(request);
        //得到登录用户的id 添加到 chart对象中   在chart 中也是有userId  这个表示的是哪个用户使用 数据分析出来的图表
        chart.setUserId(loginUser.getId());
        //把图表添加到数据库中
        boolean result = this.save(chart);
        //如果result为false 报错
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newPostId = chart.getId();
        //返回这个添加到数据库中的图表id
        return ResultUtils.success(newPostId);
    }

    /**
     * 删除用户
     * @param deleteRequest
     * @param request
     * @return
     */
    public BaseResponse<Boolean> deleteChart(DeleteRequest deleteRequest, HttpServletRequest request){
        //得到登录用户信息
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在,如果没有找到返回 没有找到异常
        Chart oldChart = this.getById(id);
        ThrowUtils.throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除 如果此时的 id 不相符 就不是本人删除的，还有就是判断此时的用户神否是管理员
        if (!oldChart.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        //返回数据： 是否删除成功  false true
        boolean b = this.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新图表
     * @param chartUpdateRequest
     * @return
     */
    public BaseResponse<Boolean> updateChart(@RequestBody ChartUpdateRequest chartUpdateRequest){

        Chart chart = new Chart();
        BeanUtils.copyProperties(chartUpdateRequest, chart);
        //更新后的图表信息copy到chart中，得到更新图表的id 判断这个id 对应的老的图表是否存在，如果不存在，返回没有找到异常
        long id = chartUpdateRequest.getId();
        // 判断是否存在
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
    public BaseResponse<Chart> getChartById(long id, HttpServletRequest request){
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
                                              HttpServletRequest request){
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
        java.lang.String goal = chartQueryRequest.getGoal();
        java.lang.String name = chartQueryRequest.getName();
        java.lang.String chartType = chartQueryRequest.getChartType();
        Long userId = chartQueryRequest.getUserId();
        java.lang.String sortField = chartQueryRequest.getSortField();
        java.lang.String sortOrder = chartQueryRequest.getSortOrder();

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




