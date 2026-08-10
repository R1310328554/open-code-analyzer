/*
 * Copyright 1999-2025 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.api.common;

/**
 * 集群节点生命周期状态。
 *
 * <p>自 1.3.0 起先统一下沉状态判定逻辑，再向外辐射节点状态，
 * 主要用于控制节点是否可处理请求等场景。</p>
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public enum NodeState {
    
    /** 节点正在启动。 */
    STARTING,
    
    /** 节点已就绪，可处理请求。 */
    UP,
    
    /** 节点状态可疑，可能发生故障。 */
    SUSPICIOUS,
    
    /** 节点不可用，发生异常。 */
    DOWN,
    
    /** 节点已被隔离。 */
    ISOLATION,
    
}
