/*
 *
 *  * Copyright 1999-2021 Alibaba Group Holding Ltd.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.alibaba.nacos.core.cluster.remote.response;

import com.alibaba.nacos.api.remote.response.Response;
import com.alibaba.nacos.core.cluster.Member;

/**
 * 集群成员上报 RPC 响应：回传对端节点的 {@link Member} 快照。
 * MemberReportResponse.
 *
 * @author : huangtianhui
 */
public class MemberReportResponse extends Response {
    
    /** 响应中携带的成员节点信息。 */
    private Member node;
    
    /** 无参构造，供序列化反序列化使用。 */
    public MemberReportResponse() {
    }
    
    /**
     * 构造带成员信息的响应。
     *
     * @param node 成员节点
     */
    public MemberReportResponse(Member node) {
        this.node = node;
    }
    
    /** 获取响应中的成员节点。 */
    public Member getNode() {
        return node;
    }
    
    /** 设置响应中的成员节点。 */
    public void setNode(Member node) {
        this.node = node;
    }
}
