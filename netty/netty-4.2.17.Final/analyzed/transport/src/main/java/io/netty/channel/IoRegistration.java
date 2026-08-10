/*
 * Copyright 2024 The Netty Project
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
package io.netty.channel;

/**
 * A registration for IO.
 * <p>I/O 注册句柄，用于向底层提交 {@link IoOps} 并管理注册生命周期。</p>
 *
 */
public interface IoRegistration {

    /**
     * Implementation specific attachment, which might be {@code null}.
     * <p>实现相关的附加对象，可能为 {@code null}。</p>
     *
     * @return  attachment.
     */
    <T> T attachment();

    /**
     * Submit the {@link IoOps} to the registration.
     * <p>向本注册提交 {@link IoOps}。</p>
     *
     * @param   ops ops.
     * @return  an identifier for the operation, which might be unique or not (depending on the implementation).
     */
    long submit(IoOps ops);

    /**
     * Returns {@code true} if the registration is still valid. Once {@link #cancel()} is called this
     * will return {@code false}.
     * <p>注册仍有效时返回 {@code true}；调用 {@link #cancel()} 后为 {@code false}。</p>
     *
     * @return  valid.
     */
    boolean isValid();

    /**
     * Cancel the registration.
     * <p>取消注册。</p>
     *
     * @return {@code true} if cancellation was successful, {@code false} otherwise.
     */
    boolean cancel();
}
