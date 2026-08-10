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

package com.alibaba.nacos.naming.healthcheck.heartbeat;

/**
 * 临时实例心跳检查任务接口。
 *
 * <p>周期性扫描并更新临时实例状态，超时未心跳则触发下线逻辑。</p>
 *
 * @author xiweng.yy
 */
public interface BeatCheckTask extends Runnable {
    
    /**
     * 任务调度键，用于去重与分组。
     *
     * @return task key
     */
    String taskKey();
    
}
