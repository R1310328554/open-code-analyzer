/*
 * Copyright 1999-2023 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.common.labels;

import java.util.Map;
import java.util.Properties;

/**
 * LabelsCollector.
 * <p>标签采集 SPI：从 {@link Properties} 配置中提取键值对标签，供 Nacos 实例元数据或路由策略使用；多实现时按 {@link #getOrder()} 排序合并。</p>
 *
 * @author rong
 * @date 2024/2/4
 */
public interface LabelsCollector {
    
    /**
     * getLabels.
     * <p>根据运行时配置采集本收集器负责的标签子集。</p>
     *
     * @param properties properties
     * @return Map labels.
     * @date 2024/2/4
     * @description get all labels
     */
    Map<String, String> collectLabels(Properties properties);
    
    /**
     * getOrder.
     * <p>合并优先级，数值越小越先执行或覆盖权越高（由 Manager 约定）。</p>
     *
     * @return the order value
     * @date 2024/2/4
     * @description get order value of labels in case of multiple labels
     */
    int getOrder();
    
    /**
     * get collector name.
     * <p>收集器唯一标识，用于日志与冲突诊断。</p>
     *
     * @return name of collector
     * @date 2024/2/4
     * @description name of collector
     */
    String getName();
    
}
