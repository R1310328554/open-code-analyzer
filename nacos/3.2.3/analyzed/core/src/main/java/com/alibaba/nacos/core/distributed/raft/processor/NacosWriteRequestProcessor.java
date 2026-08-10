/*
 *  Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.alibaba.nacos.core.distributed.raft.processor;

import com.alibaba.nacos.consistency.entity.WriteRequest;
import com.alibaba.nacos.core.distributed.raft.JRaftServer;
import com.alipay.sofa.jraft.rpc.RpcContext;
import com.alipay.sofa.jraft.rpc.RpcProcessor;

/**
 * {@link WriteRequest} 的 JRaft RPC 处理器：Follower 收到 Leader 转发的写请求时在本机 apply。
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public class NacosWriteRequestProcessor extends AbstractProcessor
    implements RpcProcessor<WriteRequest> {
    
    /** RpcProcessor 注册的兴趣类名。 */
    private static final String INTEREST_NAME = WriteRequest.class.getName();
    
    /** 关联的 JRaft 服务端。 */
    private final JRaftServer server;
    
    /** 注入 JRaftServer 并注册为 WriteRequest 处理器。 */
    public NacosWriteRequestProcessor(JRaftServer server) {
        super();
        this.server = server;
    }
    
    @Override
    /** 处理入站 WriteRequest RPC。 */
    public void handleRequest(RpcContext rpcCtx, WriteRequest request) {
        handleRequest(server, request.getGroup(), rpcCtx, request);
    }
    
    @Override
    /** 返回 {@link WriteRequest} 全限定类名。 */
    public String interest() {
        return INTEREST_NAME;
    }
}
