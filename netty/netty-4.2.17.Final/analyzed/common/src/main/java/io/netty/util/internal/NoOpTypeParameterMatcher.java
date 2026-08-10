/*
 * Copyright 2013 The Netty Project
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

package io.netty.util.internal;

/**
 * 空操作的类型参数匹配器：对所有消息均返回 true，用于无泛型约束的 Handler。
 */
public final class NoOpTypeParameterMatcher extends TypeParameterMatcher {
    /** 不做类型检查，始终接受。 */
    @Override
    public boolean match(Object msg) {
        return true;
    }
}
