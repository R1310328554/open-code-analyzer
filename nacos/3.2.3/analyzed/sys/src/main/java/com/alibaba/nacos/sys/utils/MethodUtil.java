/*
 *  Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.alibaba.nacos.sys.utils;

import java.lang.reflect.Method;

/**
 * 反射方法安全调用工具类。
 *
 * <p>封装 {@link Method#invoke} 的异常处理，在 JMX 指标采集等场景下 安全读取 double/long 返回值，失败时返回 NaN 或 -1。</p>
 *
 * @author yanhom
 */
public final class MethodUtil {
    
    /** 工具类禁止实例化。 */
    private MethodUtil() {
    }
    
    /**
     * 反射调用并返回 double 结果。
     *
     * @param method 目标方法，null 时返回 {@link Double#NaN}
     * @param targetObj 方法所属对象
     * @return 调用成功时的 double 值，异常时返回 NaN
     */
    public static double invokeAndReturnDouble(Method method, Object targetObj) {
        try {
            return method != null ? (double) method.invoke(targetObj) : Double.NaN;
        } catch (Exception e) {
            return Double.NaN;
        }
    }
    
    /**
     * 反射调用并返回 long 结果。
     *
     * @param method 目标方法，null 时返回 -1
     * @param targetObj 方法所属对象
     * @return 调用成功时的 long 值，异常时返回 -1
     */
    public static long invokeAndReturnLong(Method method, Object targetObj) {
        try {
            return method != null ? (long) method.invoke(targetObj) : -1;
        } catch (Exception e) {
            return -1;
        }
    }
}
