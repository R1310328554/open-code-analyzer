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

package com.alibaba.nacos.client.naming.backups;

import java.util.Map;

/**
 * 容灾数据源 SPI 接口。
 *
 * <p>由 {@link com.alibaba.nacos.client.naming.backups.datasource.DiskFailoverDataSource} 等实现，向 {@link FailoverReactor} 提供开关状态与容灾服务数据。</p>
 *
 * @author Nacos
 */
public interface FailoverDataSource {
    
    /**
     * 读取当前容灾开关状态。
     *
     * @return 容灾开关；无法读取时可能为 null
     */
    FailoverSwitch getSwitch();
    
    /**
     * 获取当前容灾服务数据映射。
     *
     * @return groupKey 到 {@link FailoverData} 的映射
     */
    Map<String, FailoverData> getFailoverData();
    
}
