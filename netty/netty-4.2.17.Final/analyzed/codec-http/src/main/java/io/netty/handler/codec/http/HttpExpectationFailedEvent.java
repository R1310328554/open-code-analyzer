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
package io.netty.handler.codec.http;

/**
 * 用户事件：表示 {@code Expect: 100-continue} 期望失败（如 417），
 * 告知下游不应再等待请求体。
 */
public final class HttpExpectationFailedEvent {
    /** 单例实例，通过 {@code fireUserEventTriggered} 传递 */
    public static final HttpExpectationFailedEvent INSTANCE = new HttpExpectationFailedEvent();
    private HttpExpectationFailedEvent() { }
}
