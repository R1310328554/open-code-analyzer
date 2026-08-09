/*
 * Copyright 2015 The Netty Project
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

/** 协议探测的当前状态。 */
public enum ProtocolDetectionState {
    /** 数据不足，需继续读取。 */
    NEEDS_MORE_DATA,

    /** 数据无效，无法识别协议。 */
    INVALID,

    /** 已成功识别协议。 */
    DETECTED
}
