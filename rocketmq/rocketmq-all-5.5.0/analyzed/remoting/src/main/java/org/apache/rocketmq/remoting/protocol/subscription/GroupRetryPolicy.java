/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.rocketmq.remoting.protocol.subscription;

import com.alibaba.fastjson2.annotation.JSONField;
import com.google.common.base.MoreObjects;

/**
 * 消费组重试策略配置：按 {@link GroupRetryPolicyType} 选择
 * 自定义阶梯或指数退避 {@link RetryPolicy} 实现。
 */
public class GroupRetryPolicy {
    /** 默认重试策略（CustomizedRetryPolicy）。 */
    private final static RetryPolicy DEFAULT_RETRY_POLICY = new CustomizedRetryPolicy();
    /** 重试策略类型，默认 CUSTOMIZED。 */
    private GroupRetryPolicyType type = GroupRetryPolicyType.CUSTOMIZED;
    /** 指数退避策略参数（type 为 EXPONENTIAL 时使用）。 */
    private ExponentialRetryPolicy exponentialRetryPolicy;
    /** 自定义阶梯策略参数（type 为 CUSTOMIZED 时使用）。 */
    private CustomizedRetryPolicy customizedRetryPolicy;

    /** 返回重试策略类型。 */
    public GroupRetryPolicyType getType() {
        return type;
    }

    /** 设置重试策略类型。 */
    public void setType(GroupRetryPolicyType type) {
        this.type = type;
    }

    /** 返回指数退避策略配置。 */
    public ExponentialRetryPolicy getExponentialRetryPolicy() {
        return exponentialRetryPolicy;
    }

    public void setExponentialRetryPolicy(ExponentialRetryPolicy exponentialRetryPolicy) {
        this.exponentialRetryPolicy = exponentialRetryPolicy;
    }

    /** 返回自定义阶梯策略配置。 */
    public CustomizedRetryPolicy getCustomizedRetryPolicy() {
        return customizedRetryPolicy;
    }

    public void setCustomizedRetryPolicy(CustomizedRetryPolicy customizedRetryPolicy) {
        this.customizedRetryPolicy = customizedRetryPolicy;
    }

    /** 按 type 解析并返回实际 {@link RetryPolicy}（缺省回退 DEFAULT）。 */
    @JSONField(serialize = false, deserialize = false)
    public RetryPolicy getRetryPolicy() {
        if (GroupRetryPolicyType.EXPONENTIAL.equals(type)) {
            if (exponentialRetryPolicy == null) {
                return DEFAULT_RETRY_POLICY;
            }
            return exponentialRetryPolicy;
        } else if (GroupRetryPolicyType.CUSTOMIZED.equals(type)) {
            if (customizedRetryPolicy == null) {
                return DEFAULT_RETRY_POLICY;
            }
            return customizedRetryPolicy;
        } else {
            return DEFAULT_RETRY_POLICY;
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("type", type)
            .add("exponentialRetryPolicy", exponentialRetryPolicy)
            .add("customizedRetryPolicy", customizedRetryPolicy)
            .toString();
    }
}
