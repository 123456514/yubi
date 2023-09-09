package com.yupi.yubi_backend.model.enums;


import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import lombok.Getter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ChartStatusEnums {
    @Getter
    public enum ChartStatusEnum {
        wait("等待", 0),
        running("执行中", 1),
        failed("失败", 2),
        succeed("成功", 3),
        reload("重试中", 4),
        ;

        private final String text;

        private final Integer value;

        ChartStatusEnum(String text, Integer value) {
            this.text = text;
            this.value = value;
        }

        /**
         * 获取值列表
         */
        public static List<Integer> getValues() {
            return Arrays.stream(values()).map(item -> item.value).collect(Collectors.toList());
        }

        /**
         * 根据 value 获取枚举
         */
        public static ChartStatusEnum getEnumByValue(Integer value) {
            if (ObjectUtils.isEmpty(value)) {
                return null;
            }
            for (ChartStatusEnum anEnum : ChartStatusEnum.values()) {
                if (anEnum.value.intValue() == value.intValue()) {
                    return anEnum;
                }
            }
            return null;
        }
    }

}
