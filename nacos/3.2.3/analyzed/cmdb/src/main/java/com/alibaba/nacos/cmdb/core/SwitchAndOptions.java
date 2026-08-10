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

package com.alibaba.nacos.cmdb.core;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Switch and options.
 * <p>CMDB 模块运行时开关与定时任务间隔配置，绑定 {@code nacos.cmdb.*} 属性，由 {@link com.alibaba.nacos.cmdb.memory.CmdbProvider} 调度 dump/事件/标签同步任务。</p>
 *
 * @author nkorange
 * @since 0.7.0
 */
@Component
public class SwitchAndOptions {
    
    /** 全量实体 dump 任务间隔（秒），默认 3600 */
    @Value("${nacos.cmdb.dumpTaskInterval:3600}")
    private int dumpTaskInterval;
    
    /** 实体变更事件拉取间隔（秒），默认 10 */
    @Value("${nacos.cmdb.eventTaskInterval:10}")
    private int eventTaskInterval;
    
    /** 标签元数据刷新间隔（秒），默认 300 */
    @Value("${nacos.cmdb.labelTaskInterval:300}")
    private int labelTaskInterval;
    
    /** 启动时是否从 {@link com.alibaba.nacos.api.cmdb.spi.CmdbService} 全量加载 CMDB 数据 */
    @Value("${nacos.cmdb.loadDataAtStart:false}")
    private boolean loadDataAtStart;
    
    /** 返回 dump 任务间隔（秒） */
    public int getDumpTaskInterval() {
        return dumpTaskInterval;
    }
    
    /** 返回事件任务间隔（秒） */
    public int getEventTaskInterval() {
        return eventTaskInterval;
    }
    
    /** 返回标签任务间隔（秒） */
    public int getLabelTaskInterval() {
        return labelTaskInterval;
    }
    
    /** 是否在启动阶段加载 CMDB 全量数据 */
    public boolean isLoadDataAtStart() {
        return loadDataAtStart;
    }
}
