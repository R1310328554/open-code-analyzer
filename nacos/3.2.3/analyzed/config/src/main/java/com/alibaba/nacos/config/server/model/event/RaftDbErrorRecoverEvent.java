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

package com.alibaba.nacos.config.server.model.event;

import com.alibaba.nacos.common.JustForTest;
import com.alibaba.nacos.common.notify.Event;

/**
 * Raft 数据库错误恢复事件：Raft 持久层发生异常并完成恢复后发布，
 * 供测试或运维链路感知存储层自愈（标记 {@link JustForTest}，生产慎用）。
 * RaftDBErrorRecoverEvent.
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
@JustForTest
public class RaftDbErrorRecoverEvent extends Event {
    
}
