package com.yupi.yubi_backend.mapper;

import com.yupi.yubi_backend.model.entity.Chart;
import com.yupi.yubi_backend.base.SqlEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
* @author 26702
* @description 针对表【chart(图表信息表)】的数据库操作Mapper
* @createDate 2023-08-09 21:10:18
* @Entity com.yupi.yubi_backend.model.entity.Chart
*/
public interface ChartMapper extends BaseMapper<Chart> {
    /**
     * 表示的是此时要 分表 根据用户的ID 另外的创建一个数据表，在这个数据表中存储的都是用户的图表内容
     * @param chartId 表示的是图表的id
     * @param colList 表示的是在图表中传递的数据类型和对应的字段值
     * @return 返回的是这个数据表是否创建成功
     */
   boolean createChartTable(@Param("chartId") Long chartId,@Param("colList") List<SqlEntity> colList);


    /**
     * 对根据chartId 创建的数据表 添加数据
     * @param chartId 表示的是这个图表的id
     * @param columns 表示字段
     * @param dataMap 表示数据
     * @return 返回结果为数据是否添加成功
     */
    int insertBatchChart(@Param("chartId") long chartId,@Param("columns") List<String> columns,@Param("data") List<Map<String,Object>> dataMap);
}




