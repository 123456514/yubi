package com.yupi.yubi_backend.model.vo;

import lombok.Data;

/**
 * BI的返回结果
 */
@Data
public class BIResponse {
    /**
     * 返回的图表信息
     */
    private String genChart;
    /**
     * 返回的图表结论
     */
    private String genResult;
    /**
     * 返回的图表id
     */
    private Long chartId;
}
