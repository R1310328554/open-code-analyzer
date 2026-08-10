/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.api.remote;

/**
 * 异步 RPC 请求回调的抽象基类。
 *
 * <p>实现 {@link RequestCallBack} 并提供默认超时（3 秒）；子类需实现 {@link RequestCallBack#onResponse} 与 {@link RequestCallBack#onException}，可选覆盖 {@link RequestCallBack#getExecutor()}。</p>
 *
 * @author liuzunfei
 * @version $Id: AbstractRequestCallBack.java, v 0.1 2020年09月07日 3:30 PM liuzunfei Exp $
 */
public abstract class AbstractRequestCallBack implements RequestCallBack {
    
    /** 请求等待超时时间（毫秒）。 */
    long timeoutMills;
    
    /**
     * 指定请求超时时间。
     *
     * @param timeoutMill 超时毫秒数
     */
    public AbstractRequestCallBack(long timeoutMill) {
        this.timeoutMills = timeoutMill;
    }
    
    /** 使用默认超时 3000 毫秒。 */
    public AbstractRequestCallBack() {
        this(3000L);
    }
    
    /** {@inheritDoc} 返回请求超时毫秒数。 */
    @Override
    public long getTimeout() {
        return timeoutMills;
    }
}
