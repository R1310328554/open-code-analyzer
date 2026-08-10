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

package com.alibaba.nacos.sys.env;

import com.alibaba.nacos.sys.utils.MethodUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * 操作系统 MXBean 跨 JVM 实现适配管理器。
 *
 * <p>兼容 HotSpot 与 IBM J9 的 {@link OperatingSystemMXBean} 扩展接口，通过反射调用 CPU 与物理内存指标，供 {@link EnvUtil} 监控展示。</p>
 *
 * @author yanhom
 */
public class OperatingSystemBeanManager {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(OperatingSystemBeanManager.class);
    
    /** HotSpot 与 IBM J9 各自的操作系统 MXBean 实现类名。 */
    private static final List<String> OPERATING_SYSTEM_BEAN_CLASS_NAMES = Arrays.asList(
        "com.sun.management.OperatingSystemMXBean",
        "com.ibm.lang.management.OperatingSystemMXBean");
    
    private static final OperatingSystemMXBean OPERATING_SYSTEM_BEAN;
    
    private static final Class<?> OPERATING_SYSTEM_BEAN_CLASS;
    
    private static final Method SYSTEM_CPU_USAGE_METHOD;
    
    private static final Method PROCESS_CPU_USAGE_METHOD;
    
    private static final Method FREE_PHYSICAL_MEM_METHOD;
    
    private static final Method TOTAL_PHYSICAL_MEM_METHOD;
    
    static {
        OPERATING_SYSTEM_BEAN = ManagementFactory.getOperatingSystemMXBean();
        OPERATING_SYSTEM_BEAN_CLASS = loadOne(OPERATING_SYSTEM_BEAN_CLASS_NAMES);
        SYSTEM_CPU_USAGE_METHOD = deduceMethod("getSystemCpuLoad");
        PROCESS_CPU_USAGE_METHOD = deduceMethod("getProcessCpuLoad");
        
        Method totalPhysicalMem = deduceMethod("getTotalPhysicalMemorySize");
        // IBM JDK 7 使用 getTotalPhysicalMemory 而非 getTotalPhysicalMemorySize
        TOTAL_PHYSICAL_MEM_METHOD =
            totalPhysicalMem != null ? totalPhysicalMem : deduceMethod("getTotalPhysicalMemory");
        
        FREE_PHYSICAL_MEM_METHOD = deduceMethod("getFreePhysicalMemorySize");
    }
    
    private OperatingSystemBeanManager() {
    }
    
    /** 返回 JMX 标准操作系统 MXBean 实例。 */
    public static OperatingSystemMXBean getOperatingSystemBean() {
        return OPERATING_SYSTEM_BEAN;
    }
    
    /** 获取系统整体 CPU 使用率（0.0～1.0）。 */
    public static double getSystemCpuUsage() {
        return MethodUtil.invokeAndReturnDouble(SYSTEM_CPU_USAGE_METHOD, OPERATING_SYSTEM_BEAN);
    }
    
    /** 获取当前 Nacos 进程 CPU 使用率。 */
    public static double getProcessCpuUsage() {
        return MethodUtil.invokeAndReturnDouble(PROCESS_CPU_USAGE_METHOD, OPERATING_SYSTEM_BEAN);
    }
    
    /** 获取物理内存总量（字节）。 */
    public static long getTotalPhysicalMem() {
        return MethodUtil.invokeAndReturnLong(TOTAL_PHYSICAL_MEM_METHOD, OPERATING_SYSTEM_BEAN);
    }
    
    /** 获取可用物理内存（字节）。 */
    public static long getFreePhysicalMem() {
        return MethodUtil.invokeAndReturnLong(FREE_PHYSICAL_MEM_METHOD, OPERATING_SYSTEM_BEAN);
    }
    
    private static Class<?> loadOne(List<String> classNames) {
        for (String className : classNames) {
            try {
                return Class.forName(className);
            } catch (ClassNotFoundException e) {
                LOGGER.warn(
                    "[OperatingSystemBeanManager] Failed to load operating system bean class.", e);
            }
        }
        return null;
    }
    
    private static Method deduceMethod(String name) {
        if (Objects.isNull(OPERATING_SYSTEM_BEAN_CLASS)) {
            return null;
        }
        try {
            OPERATING_SYSTEM_BEAN_CLASS.cast(OPERATING_SYSTEM_BEAN);
            return OPERATING_SYSTEM_BEAN_CLASS.getDeclaredMethod(name);
        } catch (Exception e) {
            return null;
        }
    }
}
