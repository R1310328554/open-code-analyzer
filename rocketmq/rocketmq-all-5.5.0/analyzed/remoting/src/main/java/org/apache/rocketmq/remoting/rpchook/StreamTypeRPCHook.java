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

package org.apache.rocketmq.remoting.rpchook;

import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.remoting.RPCHook;
import org.apache.rocketmq.remoting.protocol.RemotingCommand;
import org.apache.rocketmq.remoting.protocol.RequestType;

/**
 * 流式请求 RPC 钩子：在请求扩展字段中标记 RequestType.STREAM。
 */
public class StreamTypeRPCHook implements RPCHook {
    /** 请求发出前：写入 REQ_T 扩展字段，值为 STREAM 类型码。 */
    @Override
    public void doBeforeRequest(String remoteAddr, RemotingCommand request) {
        request.addExtField(MixAll.REQ_T, String.valueOf(RequestType.STREAM.getCode()));
    }

    /** 响应返回后：本钩子无需处理，留空实现。 */
    @Override
    public void doAfterResponse(String remoteAddr, RemotingCommand request,
        RemotingCommand response) {

    }
}
