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
package org.apache.rocketmq.acl.common;

import org.apache.rocketmq.remoting.RPCHook;
import org.apache.rocketmq.remoting.protocol.RemotingCommand;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * 客户端 ACL RPC 钩子：在请求发出前注入 AccessKey/SecurityToken
 * 并计算 {@link SessionCredentials#SIGNATURE} 签名。
 */
public class AclClientRPCHook implements RPCHook {
    private final SessionCredentials sessionCredentials;

    /** @param sessionCredentials ACL 会话凭证（accessKey/secretKey 等） */
    public AclClientRPCHook(SessionCredentials sessionCredentials) {
        this.sessionCredentials = sessionCredentials;
    }

    /** 请求前写入凭证字段并附加 HMAC 签名。 */
    @Override
    public void doBeforeRequest(String remoteAddr, RemotingCommand request) {
        // 将 AccessKey 与可选 SecurityToken 纳入签名字段
        request.addExtField(SessionCredentials.ACCESS_KEY, sessionCredentials.getAccessKey());
        // SecurityToken 可选，临时凭证场景才需要
        if (sessionCredentials.getSecurityToken() != null) {
            request.addExtField(SessionCredentials.SECURITY_TOKEN, sessionCredentials.getSecurityToken());
        }
        byte[] total = AclUtils.combineRequestContent(request, parseRequestContent(request));
        String signature = AclUtils.calSignature(total, sessionCredentials.getSecretKey());
        request.addExtField(SessionCredentials.SIGNATURE, signature);
    }

    /** 响应后钩子（当前无额外处理）。 */
    @Override
    public void doAfterResponse(String remoteAddr, RemotingCommand request, RemotingCommand response) {

    }

    /** 将扩展字段排序为 TreeMap，供签名计算使用。 */
    protected SortedMap<String, String> parseRequestContent(RemotingCommand request) {
        request.makeCustomHeaderToNet();
        Map<String, String> extFields = request.getExtFields();
        // 扩展字段按字典序排序以保证签名一致
        return new TreeMap<>(extFields);
    }

    /** 返回绑定的会话凭证。 */
    public SessionCredentials getSessionCredentials() {
        return sessionCredentials;
    }
}
