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
 * LabelsCollectorManager.
 * <p>标签收集器管理接口：聚合所有 {@link LabelsCollector} SPI 实现，刷新并返回合并后的完整标签 Map，供客户端或服务端注册时使用。</p>
 *
 * @author rong
 * @date 2024/2/4
 */
public interface LabelsCollectorManager {
    
    /**
     * refresh all labels.
     * <p>按 order 调用各 {@link LabelsCollector}，合并 {@code collectLabels} 结果为统一标签视图。</p>
     *
     * @date 2024/3/7
     * @param properties    Properties.
     * @return all labels.
     */
    Map<String, String> getLabels(Properties properties);
}
