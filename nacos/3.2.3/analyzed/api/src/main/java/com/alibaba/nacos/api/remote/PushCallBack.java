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
 * 服务端向客户端推送消息时的结果回调。
 *
 * <p>推送方在超时或收到 ACK 后分别调用 {@link #onFail(Throwable)} 与 {@link #onSuccess()}。</p>
 *
 * @author liuzunfei
 * @version $Id: PushCallBack.java, v 0.1 2020年07月20日 1:13 PM liuzunfei Exp $
 */
public interface PushCallBack {
    
    /**
     * 推送等待超时时间（毫秒）。
     *
     * @return 超时毫秒数
     */
    long getTimeout();
    
    /** 推送成功（收到客户端 ACK）时调用。 */
    void onSuccess();
    
    /**
     * 推送失败或超时时调用。
     *
     * @param e 抛出的异常
     */
    void onFail(Throwable e);
    
}
