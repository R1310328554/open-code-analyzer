/*
 * Copyright 2025 The Netty Project
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
package io.netty.handler.codec.compression;

import io.netty.buffer.ByteBuf;
import io.netty.util.internal.ObjectUtil;

/**
 * 防御性 {@link Decompressor} 包装器：校验调用方是否遵守状态机与生命周期约定。
 * 多数具体实现并不严格检查 API 契约，本类在边界处强制校验。
 */
final class DefensiveDecompressor implements Decompressor {
    private final Decompressor delegate;
    private Status status;
    private boolean closed;
    private boolean failed;

    /** 包装给定解压器，对其所有调用做状态与就绪检查。 */
    DefensiveDecompressor(Decompressor delegate) {
        this.delegate = ObjectUtil.checkNotNull(delegate, "delegate");
    }

    @Override
    public Status status() throws DecompressionException {
        checkReady();
        try {
            status = delegate.status();
        } catch (Exception e) {
            failed = true;
            throw e;
        }
        return status;
    }

    @Override
    public void addInput(ByteBuf buf) throws DecompressionException {
        try {
            checkReady();
            checkState(Status.NEED_INPUT);
        } catch (Throwable t) {
            buf.release();
            throw t;
        }
        try {
            delegate.addInput(buf);
        } catch (Exception e) {
            failed = true;
            throw e;
        }
        status = null;
    }

    @Override
    public void endOfInput() throws DecompressionException {
        checkReady();
        checkState(Status.NEED_INPUT);
        try {
            delegate.endOfInput();
        } catch (Exception e) {
            failed = true;
            throw e;
        }
        status = null;
    }

    @Override
    public ByteBuf takeOutput() throws DecompressionException {
        checkReady();
        checkState(Status.NEED_OUTPUT);
        ByteBuf out;
        try {
            out = delegate.takeOutput();
        } catch (Exception e) {
            failed = true;
            throw e;
        }
        status = null;
        return out;
    }

    @Override
    public void close() {
        closed = true;
        delegate.close();
    }

    /** 确认未关闭且前次调用未失败。 */
    private void checkReady() {
        if (closed) {
            throw new IllegalStateException("Already closed");
        }
        if (failed) {
            throw new IllegalStateException("Previous call failed");
        }
    }

    /** 确认当前缓存状态与 {@code expected} 一致。 */
    private void checkState(Status expected) {
        if (this.status != expected) {
            throw new IllegalStateException("Not in expected state " + expected + ", was " + this.status);
        }
    }
}
