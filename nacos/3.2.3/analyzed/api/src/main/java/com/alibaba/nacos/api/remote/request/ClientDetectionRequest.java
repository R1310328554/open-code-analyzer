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
 * 服务端发起的客户端存活探测请求。
 *
 * <p>服务端定期发送此内部模块请求以检测连接是否仍活跃；客户端应回复 {@link com.alibaba.nacos.api.remote.response.ClientDetectionResponse}。</p>
 *
 * @author liuzunfei
 * @version $Id: ClientDetectionRequest.java, v 0.1 2021年01月20日 2:42 PM liuzunfei Exp $
 */
public class ClientDetectionRequest extends ServerRequest {
    
    /** {@inheritDoc} 返回内部模块标识 {@link com.alibaba.nacos.api.common.Constants.Remote#INTERNAL_MODULE}。 */
    @Override
    public String getModule() {
        return INTERNAL_MODULE;
    }
    
}
