package com.yupi.yubi_backend.model.dto.chart;


import lombok.Data;

import java.io.Serializable;

/**
 * 智能上传文件请求
 *
 */
@Data
public class GenChartByAiRequest implements Serializable {



    /**
     * 名称
     */
    private String chartName;

    /**
     * 分析目标
     */
    private String goal;



    /**
     * 图表类型
     */
    private String chartType;




    private static final long serialVersionUID = 1L;
}
