/*
 * Copyright 1999-2025 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.copilot.adapter;

/**
 * 流式响应回调接口：用于 Copilot 与大模型流式交互时分片接收、错误处理与完成通知。
 * Stream response callback interface.
 *
 * @param <T> 响应分片类型
 * @author nacos
 */
public interface StreamResponseCallback<T> {
    
    /**
     * 收到新的流式响应分片时回调。
     *
     * @param response 响应分片内容
     */
    void onNext(T response);
    
    /**
     * 流式传输发生异常时回调。
     *
     * @param t 异常对象
     */
    void onError(Throwable t);
    
    /**
     * 流式响应全部接收完毕时回调。
     */
    void onComplete();
}
