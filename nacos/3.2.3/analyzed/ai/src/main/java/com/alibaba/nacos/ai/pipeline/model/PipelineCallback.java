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

package com.alibaba.nacos.ai.pipeline.model;

/**
 * Callback interface for pipeline execution completion notification.
 * <p>流水线执行完成回调接口，执行结束时恰好调用一次 {@link #onComplete}。</p>
 *
 * @author kiro
 * @since 3.2.0
 */
public interface PipelineCallback {
    
    /**
     * Called when pipeline execution completes.
     * <p>流水线全部节点执行完毕（或中途失败终止）时触发。</p>
     *
     * @param result the execution result containing status and node details
     */
    void onComplete(PipelineExecutionResult result);
}
