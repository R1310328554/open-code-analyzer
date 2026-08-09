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

package io.netty.handler.codec;

/**
 * 为已解码消息提供 {@link DecoderResult} 的读写访问。
 */
public interface DecoderResultProvider {
    /** 返回该对象的解码结果。 */
    DecoderResult decoderResult();

    /** 由解码器更新解码结果；非解码逻辑请勿调用。 */
    void setDecoderResult(DecoderResult result);
}
