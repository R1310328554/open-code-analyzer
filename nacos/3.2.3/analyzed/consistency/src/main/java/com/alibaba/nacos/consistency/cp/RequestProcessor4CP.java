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

package com.alibaba.nacos.consistency.cp;

import com.alibaba.nacos.consistency.RequestProcessor;
import com.alibaba.nacos.consistency.snapshot.SnapshotOperation;

import java.util.Collections;
import java.util.List;

/**
 * CP 协议专用请求处理器抽象基类，继承 {@link com.alibaba.nacos.consistency.RequestProcessor}。
 * 可覆写 {@link #loadSnapshotOperate()} 声明本模块参与快照的 {@link SnapshotOperation}。
 *
 * log processor for cp.
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
@SuppressWarnings("all")
public abstract class RequestProcessor4CP extends RequestProcessor {
    
    /**
     * 返回本处理器负责的快照操作列表；默认空列表，子类按需注册。
     * Discovery snapshot handler It is up to LogProcessor to decide which SnapshotOperate should be loaded and saved by
     * itself.
     *
     * @return {@link List <SnapshotOperate>}
     */
    public List<SnapshotOperation> loadSnapshotOperate() {
        return Collections.emptyList();
    }
    
}
