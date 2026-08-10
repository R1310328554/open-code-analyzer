/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.common.utils;

import java.util.Arrays;

/**
 * 数组工具类：提供空数组判断与元素包含性检查等常用操作，
 * 私有构造防止实例化。
 * Array utils.
 *
 * @author zzq
 */
public class ArrayUtils {
    
    private ArrayUtils() {
    }
    
    /**
     * 判断对象数组是否为 null 或长度为 0。
     *
     * @param array  待检测数组
     * @return 为 null 或空数组时返回 {@code true}
     */
    public static boolean isEmpty(final Object[] array) {
        return array == null || array.length == 0;
    }
    
    /**
     * 判断数组是否包含指定元素（使用 {@link java.util.Arrays#asList} 比较）。
     *
     * @param array         待搜索数组，null 时返回 false
     * @param objectToFind  目标元素
     * @return 包含则 true
     */
    public static boolean contains(final Object[] array, final Object objectToFind) {
        if (array == null) {
            return false;
        }
        
        return Arrays.asList(array).contains(objectToFind);
    }
    
}
