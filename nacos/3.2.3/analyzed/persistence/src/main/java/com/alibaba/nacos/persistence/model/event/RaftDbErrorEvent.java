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

package com.alibaba.nacos.persistence.model.event;

import com.alibaba.nacos.common.notify.SlowEvent;

/**
 * Raft 嵌入式数据库错误事件。
 *
 * <p>当 Raft 持久化存储发生异常时发布，携带 {@link #ex} 供监听器记录告警或触发降级。</p>
 *
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 */
public class RaftDbErrorEvent extends SlowEvent {
    
    private static final long serialVersionUID = 101591819161802336L;
    
    /** 导致 Raft DB 错误的异常对象。 */
    private Throwable ex;
    
    public RaftDbErrorEvent() {
    }
    
    /** 构造事件并绑定异常。 */
    public RaftDbErrorEvent(Throwable ex) {
        this.ex = ex;
    }
    
    /** 返回关联的异常。 */
    public Throwable getEx() {
        return ex;
    }
}
