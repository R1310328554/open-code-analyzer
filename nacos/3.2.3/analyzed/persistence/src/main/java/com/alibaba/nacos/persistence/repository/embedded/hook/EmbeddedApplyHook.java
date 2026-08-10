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

package com.alibaba.nacos.persistence.repository.embedded.hook;

import com.alibaba.nacos.consistency.entity.WriteRequest;

/**
 * 嵌入式存储 Raft 日志 Apply 完成后的钩子基类。
 *
 * <p>子类构造时自动注册到 {@link EmbeddedApplyHookHolder}，在共识层持久化 SQL 后可异步触发下游刷新或通知。</p>
 *
 * @author xiweng.yy
 */
public abstract class EmbeddedApplyHook {
    
    protected EmbeddedApplyHook() {
        EmbeddedApplyHookHolder.getInstance().register(this);
    }
    
    /**
     * Raft 日志 Apply 成功后的回调。
     *
     * @param log raft log
     */
    public abstract void afterApply(WriteRequest log);
}
