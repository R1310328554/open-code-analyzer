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

package com.alibaba.nacos.api.remote.request;

import static com.alibaba.nacos.api.common.Constants.Remote.INTERNAL_MODULE;

/**
 * Nacos 内部模块 RPC 请求的抽象基类。
 *
 * <p>固定 {@link #getModule()} 返回 {@link com.alibaba.nacos.api.common.Constants.Remote#INTERNAL_MODULE}，用于连接管理、健康检查、推送 ACK 等非业务模块消息。</p>
 *
 * @author liuzunfei
 * @version $Id: InternalRequest.java, v 0.1 2020年07月22日 8:33 PM liuzunfei Exp $
 */
public abstract class InternalRequest extends Request {
    
    /** {@inheritDoc} 返回内部模块标识。 */
    @Override
    public String getModule() {
        return INTERNAL_MODULE;
    }
}
