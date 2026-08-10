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
package io.netty.handler.codec.memcache;

import io.netty.handler.codec.DecoderResultProvider;
import io.netty.util.internal.UnstableApi;

/**
 * Defines a common interface for all {@link MemcacheObject} implementations.
 *
 * <p>Memcache 编解码 pipeline 中所有出站/入站对象的根标记接口，
 * 继承 {@link DecoderResultProvider} 以统一承载解码成败状态。
 */
@UnstableApi
public interface MemcacheObject extends DecoderResultProvider { }
