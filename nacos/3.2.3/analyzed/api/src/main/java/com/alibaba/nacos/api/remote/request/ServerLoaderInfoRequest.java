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

/**
 * 查询服务端节点负载信息的内部请求。
 *
 * <p>集群管理或运维场景下获取各节点连接数、负载等指标。</p>
 *
 * @author liuzunfei
 * @version $Id: ServerLoaderInfoRequest.java, v 0.1 2020年09月03日 2:45 PM liuzunfei Exp $
 */
public class ServerLoaderInfoRequest extends InternalRequest {
    
    /** 无参构造，供序列化框架使用。 */
    public ServerLoaderInfoRequest() {
    }
}
