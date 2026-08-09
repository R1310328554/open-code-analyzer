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

package org.apache.rocketmq.remoting.netty;

import io.netty.channel.Channel;
import org.apache.rocketmq.remoting.protocol.RemotingCommand;

/**
 * 服务端请求处理任务：封装 Runnable、通道与原始请求，支持停止执行与快速写回响应。
 */
public class RequestTask implements Runnable {
    /** 实际业务处理逻辑。 */
    private final Runnable runnable;
    /** 任务创建时间戳，用于去重与监控。 */
    private final long createTimestamp = System.currentTimeMillis();
    /** 请求来源通道。 */
    private final Channel channel;
    /** 原始 Remoting 请求。 */
    private final RemotingCommand request;
    /** 为 true 时 {@link #run()} 跳过业务逻辑。 */
    private volatile boolean stopRun = false;

    public RequestTask(final Runnable runnable, final Channel channel, final RemotingCommand request) {
        this.runnable = runnable;
        this.channel = channel;
        this.request = request;
    }

    @Override
    public int hashCode() {
        int result = runnable != null ? runnable.hashCode() : 0;
        result = 31 * result + (int) (getCreateTimestamp() ^ (getCreateTimestamp() >>> 32));
        result = 31 * result + (channel != null ? channel.hashCode() : 0);
        result = 31 * result + (request != null ? request.hashCode() : 0);
        result = 31 * result + (isStopRun() ? 1 : 0);
        return result;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o)
            return true;
        if (!(o instanceof RequestTask))
            return false;

        final RequestTask that = (RequestTask) o;

        if (getCreateTimestamp() != that.getCreateTimestamp())
            return false;
        if (isStopRun() != that.isStopRun())
            return false;
        if (channel != null ? !channel.equals(that.channel) : that.channel != null)
            return false;
        return request != null ? request.getOpaque() == that.request.getOpaque() : that.request == null;

    }

    public long getCreateTimestamp() {
        return createTimestamp;
    }

    public boolean isStopRun() {
        return stopRun;
    }

    public void setStopRun(final boolean stopRun) {
        this.stopRun = stopRun;
    }

    @Override
    /** 未被停止时执行业务 Runnable。 */
    public void run() {
        if (!this.stopRun)
            this.runnable.run();
    }

    /** 构造响应并写回客户端，opaque 与请求一致。 */
    public void returnResponse(int code, String remark) {
        final RemotingCommand response = RemotingCommand.createResponseCommand(code, remark);
        response.setOpaque(request.getOpaque());
        this.channel.writeAndFlush(response);
    }
}
