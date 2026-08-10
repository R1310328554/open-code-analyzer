/*
 * Copyright 2011 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.sun.nio.sctp;

/**
 * {@link NotificationHandler} 处理 SCTP 通知后的控制结果。
 * <p>{@link #CONTINUE} 继续处理后续通知；{@link #RETURN} 终止当前 receive 循环。</p>
 */
public enum HandlerResult {
    /** 继续接收并分发通知 */
    CONTINUE,
    /** 立即结束本次 {@code receive} 中的通知处理 */
    RETURN
}
