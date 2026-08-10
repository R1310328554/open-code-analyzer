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

package com.alibaba.nacos.common.http;

import com.alibaba.nacos.common.model.RestResult;

/**
 * Http callback.
 * <p>异步 HTTP 请求结果回调接口：成功、失败与取消三种路径。</p>
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public interface Callback<T> {
    
    /**
     * Callback after the request is responded.
     * <p>收到 HTTP 响应并成功解析为 {@link RestResult} 时调用。</p>
     *
     * @param result {@link RestResult}
     */
    void onReceive(RestResult<T> result);
    
    /**
     * An error occurred during the request.
     * <p>请求或解析过程中抛出异常时调用。</p>
     *
     * @param throwable {@link Throwable}
     */
    void onError(Throwable throwable);
    
    /**
     * Callback when the request is cancelled.
     * <p>请求被取消（如超时或主动取消）时调用。</p>
     */
    void onCancel();
    
}
