/*
 * Copyright 2014 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.handler.ipfilter;

/**
 * Used in {@link IpFilterRule} to decide if a matching IP Address should be allowed or denied to connect.
 *
 * <p>{@link IpFilterRule} 匹配成功后的动作：允许连接或拒绝连接。</p>
 */
public enum IpFilterRuleType {
    /** 匹配时接受连接。 */
    ACCEPT,
    /** 匹配时拒绝连接。 */
    REJECT
}
