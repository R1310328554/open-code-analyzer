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
package io.netty.handler.codec.quic;

import java.util.function.BiConsumer;

/**
 * 私钥方法相关 {@link BoringSSLTask} 的抽象基类。
 * <p>
 * 子类实现 {@link #runMethod} 调用签名或解密；成功时将结果写入 {@link #resultBytes} 供 JNI 读取。
 */
abstract class BoringSSLPrivateKeyMethodTask extends BoringSSLTask {

    private final BoringSSLPrivateKeyMethod method;

    // 由 JNI 通过字段反射读取
    private byte[] resultBytes;

    BoringSSLPrivateKeyMethodTask(long ssl, BoringSSLPrivateKeyMethod method) {
        super(ssl);
        this.method = method;
    }

    @Override
    protected final void runTask(long ssl, TaskCallback callback) {
        runMethod(ssl, method, (result, error) -> {
            if (result == null || error != null) {
                // -1 表示失败，native 层据此终止当前 SSL 操作
                callback.onResult(ssl, -1);
            } else {
                resultBytes = result;
                callback.onResult(ssl, 1);
            }
        });
    }

    /** 执行具体的签名或解密逻辑，完成后通过 callback 回传字节或异常。 */
    protected abstract void runMethod(long ssl, BoringSSLPrivateKeyMethod method,
                                      BiConsumer<byte[], Throwable> callback);
}
