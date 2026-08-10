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

package com.alibaba.nacos.core.cluster.remote.request;

import com.alibaba.nacos.core.cluster.Member;

/**
 * 集群成员上报 RPC 请求：携带本节点 {@link Member} 快照供对端同步集群视图。
 * MemberReportRequest.
 *
 * @author : huangtianhui
 */
public class MemberReportRequest extends AbstractClusterRequest {
    
    /** 待上报的集群成员节点信息。 */
    private Member node;
    
    /** 无参构造，供序列化框架反序列化使用。 */
    public MemberReportRequest() {
    }
    
    /**
     * 构造成员上报请求。
     *
     * @param node 本节点成员信息
     */
    public MemberReportRequest(Member node) {
        this.node = node;
    }
    
    /** 获取上报的成员节点。 */
    public Member getNode() {
        return node;
    }
    
    /** 设置上报的成员节点。 */
    public void setNode(Member node) {
        this.node = node;
    }
}
