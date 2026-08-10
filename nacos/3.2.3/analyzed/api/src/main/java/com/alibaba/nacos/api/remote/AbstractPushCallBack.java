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
 * 推送服务回调的抽象基类。
 *
 * <p>实现 {@link PushCallBack} 并封装 {@link #timeout} 超时毫秒数，子类只需实现 {@link PushCallBack#onSuccess()} 与 {@link PushCallBack#onFail(Throwable)}。</p>
 *
 * @author liuzunfei
 * @version $Id: PushCallBack.java, v 0.1 2020年07月20日 1:13 PM liuzunfei Exp $
 */
public abstract class AbstractPushCallBack implements PushCallBack {
    
    /** 推送等待超时时间（毫秒）。 */
    private long timeout;
    
    /**
     * 指定推送超时时间。
     *
     * @param timeout 超时毫秒数
     */
    public AbstractPushCallBack(long timeout) {
        this.timeout = timeout;
    }
    
    /** {@inheritDoc} 返回推送超时毫秒数。 */
    @Override
    public long getTimeout() {
        return timeout;
    }
}
