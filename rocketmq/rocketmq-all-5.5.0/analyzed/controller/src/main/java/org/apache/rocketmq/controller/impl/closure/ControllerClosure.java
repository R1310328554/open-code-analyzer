/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.rocketmq.controller.impl.closure;

import com.alipay.sofa.jraft.Closure;
import com.alipay.sofa.jraft.Status;
import com.alipay.sofa.jraft.entity.Task;
import org.apache.rocketmq.common.constant.LoggerName;
import org.apache.rocketmq.controller.impl.event.ControllerResult;
import org.apache.rocketmq.logging.org.slf4j.Logger;
import org.apache.rocketmq.logging.org.slf4j.LoggerFactory;
import org.apache.rocketmq.remoting.CommandCustomHeader;
import org.apache.rocketmq.remoting.protocol.RemotingCommand;
import org.apache.rocketmq.remoting.protocol.ResponseCode;

import java.util.concurrent.CompletableFuture;

/**
 * JRaft 请求闭包：将 Remoting 请求提交到 Raft 并在提交完成后返回响应。
 */
public class ControllerClosure implements Closure {
    private static final Logger log = LoggerFactory.getLogger(LoggerName.CONTROLLER_LOGGER_NAME);
    /** 待提交的 Remoting 请求。 */
    private final RemotingCommand requestEvent;
    /** 异步响应 Future，供调用方等待结果。 */
    private final CompletableFuture<RemotingCommand> future;
    /** 状态机处理完成后填充的业务结果。 */
    private ControllerResult<?> controllerResult;
    /** 关联的 JRaft Task 对象。 */
    private Task task;

    /** 构造闭包并初始化异步 Future。 */
    public ControllerClosure(RemotingCommand requestEvent) {
        this.requestEvent = requestEvent;
        this.future = new CompletableFuture<>();
        this.task = null;
    }

    /** 返回等待 Raft 提交完成的 Future。 */
    public CompletableFuture<RemotingCommand> getFuture() {
        return future;
    }

    /** 设置状态机处理结果，供 run 时构建响应。 */
    public void setControllerResult(ControllerResult<?> controllerResult) {
        this.controllerResult = controllerResult;
    }

    @Override
    /** Raft 提交完成回调：成功则封装响应，失败则返回内部错误码。 */
    public void run(Status status) {
        if (status.isOk()) {
            final RemotingCommand response = RemotingCommand.createResponseCommandWithHeader(controllerResult.getResponseCode(), (CommandCustomHeader) controllerResult.getResponse());
            if (controllerResult.getBody() != null) {
                response.setBody(controllerResult.getBody());
            }
            if (controllerResult.getRemark() != null) {
                response.setRemark(controllerResult.getRemark());
            }
            future.complete(response);
        } else {
            log.error("Failed to append to jRaft node, error is: {}.", status);
            future.complete(RemotingCommand.createResponseCommand(ResponseCode.CONTROLLER_JRAFT_INTERNAL_ERROR, status.getErrorMsg()));
        }
    }

    /** 构建携带本闭包的 JRaft Task（编码请求体为日志数据）。 */
    public Task taskWithThisClosure() {
        if (task != null) {
            return task;
        }
        task = new Task();
        task.setDone(this);
        task.setData(requestEvent.encode());
        return task;
    }

    /** 返回原始 Remoting 请求。 */
    public RemotingCommand getRequestEvent() {
        return requestEvent;
    }
}
