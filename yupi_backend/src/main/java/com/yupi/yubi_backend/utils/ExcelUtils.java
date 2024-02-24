package com.yupi.yubi_backend.utils;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class ExcelUtils {
    /**
     * 读取execl中的文件信息 然后转化成为 csv格式 完成文件文件压缩
     */
    public static String excelToCsv(MultipartFile multipartFile) {

        //读取文件中的信息，并且按照map 就是键值对的形式展示出来
        List<Map<Integer,String>> list = null;
        try {
            list = EasyExcel.read(multipartFile.getInputStream()).
                    excelType(ExcelTypeEnum.XLSX).
                    sheet().
                    headRowNumber(0).
                    doReadSync();
        } catch (IOException e) {
            log.info("表格处理错误",e);
            throw new RuntimeException(e);
        }
        System.out.println(list);
        if(CollUtil.isEmpty(list)){
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();
        //读取表头 得到list 中 key 对应的 values list 中 key 表示的是 0 1 2 3
        // values 代表的是在文件中扫描到数据头，得到list中第一行的数据，就是excel中的表头数据

        LinkedHashMap<Integer,String> headerMap = (LinkedHashMap<Integer, String>) list.get(0);
        //然后再次扫表表头数据，对表头数据进行过滤，表头数据中不为空
        List<String> headerList = headerMap.
                values().
                stream().
                filter(ObjectUtils::isNotEmpty).collect(Collectors.toList());
        //拼接表头数据
        stringBuilder.append(StringUtils.join(headerList,",")).append('\n');
        //读取数据表中非表头的数据
        for(int i = 1;i < list.size();i++){
            LinkedHashMap<Integer,String> dataMap = (LinkedHashMap<Integer, String>) list.get(i);
            List<String> dataList = dataMap.values().stream().filter(ObjectUtils::isNotEmpty).collect(Collectors.toList());
            stringBuilder.append(StringUtils.join(dataList,",")).append('\n');
        }
        return stringBuilder.toString();
    }
    // 得到文件的表头
    public static LinkedHashMap<Integer,String> getHeaderMap(MultipartFile multipartFile){
        //读取文件中的信息，并且按照map 就是键值对的形式展示出来
        List<Map<Integer,String>> list = null;
        try {
            list = EasyExcel.read(multipartFile.getInputStream()).
                    excelType(ExcelTypeEnum.XLSX).
                    sheet().
                    headRowNumber(0).
                    doReadSync();
        } catch (IOException e) {
            log.info("表格处理错误",e);
            throw new RuntimeException(e);
        }
        System.out.println(list);
        if(CollUtil.isEmpty(list)){
            return null;
        }
        //读取表头 得到list 中 key 对应的 values list 中 key 表示的是 0 1 2
        // values 代表的是在文件中扫描到数据头，得到list中第一行的数据，就是excel中的表头数据
        return (LinkedHashMap<Integer, String>) list.get(0);
    }
}
