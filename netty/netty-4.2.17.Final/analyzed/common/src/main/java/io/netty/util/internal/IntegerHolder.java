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

package io.netty.util.internal;

/**
 * @deprecated For removal in netty 4.2
 *
 * <p>可变的 int 包装器，曾用于线程本地计数器哈希码；Netty 4.2 起移除。</p>
 */
@Deprecated
public final class IntegerHolder {
    /** 持有的整数值，公开字段供旧 API 直接读写。 */
    public int value;
}
