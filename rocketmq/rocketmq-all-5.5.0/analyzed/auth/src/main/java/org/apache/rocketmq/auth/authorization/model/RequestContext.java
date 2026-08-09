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
package org.apache.rocketmq.auth.authorization.model;

import org.apache.rocketmq.auth.authentication.model.Subject;
import org.apache.rocketmq.common.action.Action;

/**
 * 授权请求上下文：携带主体、资源、动作与来源 IP，供策略匹配使用。
 */
public class RequestContext {

    private Subject subject;

    private Resource resource;

    private Action action;

    private String sourceIp;

    /** 返回请求主体。 */
    public Subject getSubject() {
        return subject;
    }

    /** 设置请求主体。 */
    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    /** 返回请求目标资源。 */
    public Resource getResource() {
        return resource;
    }

    /** 设置请求目标资源。 */
    public void setResource(Resource resource) {
        this.resource = resource;
    }

    /** 返回请求动作。 */
    public Action getAction() {
        return action;
    }

    /** 设置请求动作。 */
    public void setAction(Action action) {
        this.action = action;
    }

    /** 返回客户端来源 IP。 */
    public String getSourceIp() {
        return sourceIp;
    }

    /** 设置客户端来源 IP。 */
    public void setSourceIp(String sourceIp) {
        this.sourceIp = sourceIp;
    }
}
