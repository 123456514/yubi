package com.yupi.yubi_backend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.yubi_backend.common.BaseResponse;
import com.yupi.yubi_backend.common.DeleteRequest;
import com.yupi.yubi_backend.model.dto.chart.ChartAddRequest;
import com.yupi.yubi_backend.model.dto.chart.ChartEditRequest;
import com.yupi.yubi_backend.model.dto.chart.ChartQueryRequest;
import com.yupi.yubi_backend.model.dto.chart.ChartUpdateRequest;
import com.yupi.yubi_backend.model.entity.Chart;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.http.HttpServletRequest;

/**
* @author 26702
* @description 针对表【chart(图表信息表)】的数据库操作Service
* @createDate 2023-08-09 21:10:18
*/
public interface ChartService extends IService<Chart> {
    /**
     * 在数据库中添加图表
     * @param chartAddRequest
     * @param request
     * @return
     */
    BaseResponse<Long> addChart(ChartAddRequest chartAddRequest, HttpServletRequest request);

    /**
     * 删除图表
     * @param deleteRequest
     * @param request
     * @return
     */
    BaseResponse<Boolean> deleteChart(DeleteRequest deleteRequest, HttpServletRequest request);

    /**
     * 更新图表表
     * @param chartUpdateRequest
     * @return
     */
    BaseResponse<Boolean> updateChart(@RequestBody ChartUpdateRequest chartUpdateRequest);

    /**
     * 根据id 查询用户图表
     * @param id
     * @param request
     * @return
     */
    BaseResponse<Chart> getChartById(long id, HttpServletRequest request);

    /**
     * 分页查询
     * @param chartQueryRequest
     * @param request
     * @return
     */
    BaseResponse<Page<Chart>> listChartByPage(@RequestBody ChartQueryRequest chartQueryRequest,
                                              HttpServletRequest request);

    /**
     * 分页获取当前用户创建的资源列表
     * @param chartQueryRequest
     * @param request
     * @return
     */
    BaseResponse<Page<Chart>> listMyChartByPage(@RequestBody ChartQueryRequest chartQueryRequest,
                                                HttpServletRequest request);



    /**
     *
     * @param chartQueryRequest
     * @param request
     * @return
     */
    BaseResponse<QueryWrapper<Chart>> searchChartByPage(@RequestBody ChartQueryRequest chartQueryRequest,
                                                        HttpServletRequest request);

    /**
     * 编辑用户
     * @param chartEditRequest
     * @param request
     * @return
     */
    BaseResponse<Boolean> editChart(@RequestBody ChartEditRequest chartEditRequest, HttpServletRequest request);

    /**
     *  根据chartId 锁定图表 更新图表的状态
     * @param chartId 图表Id
     * @param chartStatus 图表状态
     * @return 是否修改状态成功
     */
     boolean handleUpdateChartStatus(long chartId, String chartStatus);
}
