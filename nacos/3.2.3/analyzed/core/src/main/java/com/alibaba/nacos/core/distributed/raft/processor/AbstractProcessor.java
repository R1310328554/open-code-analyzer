/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.core.distributed.raft.processor;

import com.alibaba.nacos.consistency.entity.Response;
import com.alibaba.nacos.core.distributed.raft.JRaftServer;
import com.alibaba.nacos.core.distributed.raft.utils.FailoverClosure;
import com.alibaba.nacos.core.utils.Loggers;
import com.alipay.sofa.jraft.Status;
import com.alipay.sofa.jraft.rpc.RpcContext;
import com.google.protobuf.Message;

import java.util.Objects;

/**
 * Raft RPC 处理器抽象基类：校验 Group 与 Leader 后在本机 apply 或返回错误响应。
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public abstract class AbstractProcessor {
    
    /** 默认构造。 */
    public AbstractProcessor() {
    }
    
    /** 统一入口：查找 Group、校验 Leader，非 Leader 则返回错误。 */
    protected void handleRequest(final JRaftServer server, final String group,
        final RpcContext rpcCtx, Message message) {
        try {
            final JRaftServer.RaftGroupTuple tuple = server.findTupleByGroup(group);
            if (Objects.isNull(tuple)) {
                rpcCtx.sendResponse(Response.newBuilder().setSuccess(false)
                    .setErrMsg("Could not find the corresponding Raft Group : " + group).build());
                return;
            }
            if (tuple.getNode().isLeader()) {
                execute(server, rpcCtx, message, tuple);
            } else {
                rpcCtx.sendResponse(
                    Response.newBuilder().setSuccess(false)
                        .setErrMsg("Could not find leader : " + group).build());
            }
        } catch (Throwable e) {
            Loggers.RAFT.error("handleRequest has error : ", e);
            rpcCtx.sendResponse(
                Response.newBuilder().setSuccess(false).setErrMsg(e.toString()).build());
        }
    }
    
    /** Leader 节点执行 applyOperation，结果经 FailoverClosure 异步写回 RPC。 */
    protected void execute(JRaftServer server, final RpcContext asyncCtx, final Message message,
        final JRaftServer.RaftGroupTuple tuple) {
        FailoverClosure closure = new FailoverClosure() {
            
            /** apply 成功后的业务响应。 */
            Response data;
            
            /** apply 过程中的异常。 */
            Throwable ex;
            
            @Override
            public void setResponse(Response data) {
                this.data = data;
            }
            
            @Override
            public void setThrowable(Throwable throwable) {
                this.ex = throwable;
            }
            
            @Override
            public void run(Status status) {
                if (Objects.nonNull(ex)) {
                    Loggers.RAFT.error("execute has error : ", ex);
                    asyncCtx.sendResponse(
                        Response.newBuilder().setErrMsg(ex.toString()).setSuccess(false).build());
                } else {
                    asyncCtx.sendResponse(data);
                }
            }
        };
        
        server.applyOperation(tuple.getNode(), message, closure);
    }
    
}
