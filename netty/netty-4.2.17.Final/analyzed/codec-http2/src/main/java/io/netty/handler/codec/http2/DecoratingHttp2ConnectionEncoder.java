/*
 * Copyright 2015 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License, version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package io.netty.handler.codec.http2;

import static io.netty.util.internal.ObjectUtil.checkNotNull;

/**
 * {@link Http2ConnectionEncoder} 的装饰器基类，同时继承 {@link DecoratingHttp2FrameWriter} 的写帧委托。
 * <p>除帧写入外，还将连接、远端流控、SETTINGS 交换等编码器职责转发给 {@code delegate}，
 * 便于在编码路径上叠加压缩、限流等横切能力。
 */
public class DecoratingHttp2ConnectionEncoder extends DecoratingHttp2FrameWriter implements Http2ConnectionEncoder,
        Http2SettingsReceivedConsumer {
    private final Http2ConnectionEncoder delegate;

    public DecoratingHttp2ConnectionEncoder(Http2ConnectionEncoder delegate) {
        super(delegate);
        this.delegate = checkNotNull(delegate, "delegate");
    }

    @Override
    public void lifecycleManager(Http2LifecycleManager lifecycleManager) {
        delegate.lifecycleManager(lifecycleManager);
    }

    @Override
    public Http2Connection connection() {
        return delegate.connection();
    }

    @Override
    public Http2RemoteFlowController flowController() {
        return delegate.flowController();
    }

    @Override
    public Http2FrameWriter frameWriter() {
        return delegate.frameWriter();
    }

    @Override
    public Http2Settings pollSentSettings() {
        return delegate.pollSentSettings();
    }

    @Override
    public void remoteSettings(Http2Settings settings) throws Http2Exception {
        delegate.remoteSettings(settings);
    }

    @Override
    public void consumeReceivedSettings(Http2Settings settings) {
        if (delegate instanceof Http2SettingsReceivedConsumer) {
            ((Http2SettingsReceivedConsumer) delegate).consumeReceivedSettings(settings);
        } else {
            throw new IllegalStateException("delegate " + delegate + " is not an instance of " +
                    Http2SettingsReceivedConsumer.class);
        }
    }
}
