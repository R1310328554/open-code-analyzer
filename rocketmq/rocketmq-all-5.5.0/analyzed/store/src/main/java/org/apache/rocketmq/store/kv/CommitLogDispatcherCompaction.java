/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.rocketmq.store.kv;

import org.apache.rocketmq.store.CommitLogDispatcher;
import org.apache.rocketmq.store.DispatchRequest;

/**
 * CommitLog 压缩分发器：将分发请求转发给 CompactionService。
 */
public class CommitLogDispatcherCompaction implements CommitLogDispatcher {
    /** 底层压缩服务实例。 */
    private final CompactionService cptService;

    /** 绑定压缩服务。 */
    public CommitLogDispatcherCompaction(CompactionService srv) {
        this.cptService = srv;
    }

    /** 将分发请求提交给压缩服务处理。 */
    @Override
    public void dispatch(DispatchRequest request) {
        if (cptService != null) {
            cptService.putRequest(request);
        }
    }
}
