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
package org.apache.rocketmq.auth.authentication.context;

/**
 * 默认认证上下文：包含用户名、待签名内容与客户端提交的签名。
 */
public class DefaultAuthenticationContext extends AuthenticationContext {

    private String username;

    private byte[] content;

    private String signature;

    /** 返回认证用户名（accessKey）。 */
    public String getUsername() {
        return username;
    }

    /** 设置认证用户名。 */
    public void setUsername(String username) {
        this.username = username;
    }

    /** 返回参与签名的原始字节内容。 */
    public byte[] getContent() {
        return content;
    }

    /** 设置参与签名的原始字节内容。 */
    public void setContent(byte[] content) {
        this.content = content;
    }

    /** 返回客户端提交的签名字符串。 */
    public String getSignature() {
        return signature;
    }

    /** 设置客户端提交的签名字符串。 */
    public void setSignature(String signature) {
        this.signature = signature;
    }
}
