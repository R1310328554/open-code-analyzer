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

package com.alibaba.nacos.api.remote.response;

/**
 * 健康检查请求的响应。
 *
 * <p>服务端收到 {@link com.alibaba.nacos.api.remote.request.HealthCheckRequest} 后回复，表明链路畅通；空响应体，成功由 {@link Response#isSuccess()} 判定。</p>
 *
 * @author liuzunfei
 * @version $Id: ServerCheckResponse.java, v 0.1 2020年07月22日 8:37 PM liuzunfei Exp $
 */
public class HealthCheckResponse extends Response {
    
}
