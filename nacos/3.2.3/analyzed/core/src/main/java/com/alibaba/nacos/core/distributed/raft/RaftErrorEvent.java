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

package com.alibaba.nacos.core.distributed.raft;

import com.alibaba.nacos.common.notify.Event;

/**
 * Raft 协议致命异常事件：发布后表示对应 Raft Group 已无法正常运行，需上层介入处理。
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public class RaftErrorEvent extends Event {
    
    /** 序列化版本号。 */
    private static final long serialVersionUID = 3016514657754158167L;
    
    /** 发生异常的 Raft Group 名称。 */
    private String groupName;
    
    /** 返回异常 Group 名称。 */
    public String getGroupName() {
        return groupName;
    }
    
    /** 设置异常 Group 名称。 */
    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
}
