/*
 * Copyright 2014 The Netty Project
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
package io.netty.handler.codec.http;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.channel.ChannelPromise;
import io.netty.util.AsciiString;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.internal.ObjectUtil;

import java.net.SocketAddress;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static io.netty.handler.codec.http.HttpResponseStatus.SWITCHING_PROTOCOLS;
import static io.netty.util.ReferenceCountUtil.release;

/**
 * 客户端 HTTP 协议升级处理器，负责发起 Upgrade 握手并切换 pipeline。
 * <p>
 * 首个 {@link HttpRequest} 发出时自动添加 Upgrade/Connection 头；
 * 若响应非 101 则移除自身继续 HTTP；成功则调用 {@link UpgradeCodec} 替换协议栈。
 */
public class HttpClientUpgradeHandler extends HttpObjectAggregator implements ChannelOutboundHandler {

    /** 升级过程触发的用户事件，供下游 handler 感知状态 */

    public enum UpgradeEvent {
        /** 已向服务端发送 Upgrade 请求 */

        UPGRADE_ISSUED,

        /** 协议升级成功 */

        UPGRADE_SUCCESSFUL,

        /** 服务端未返回 101，升级被拒绝 */

        UPGRADE_REJECTED
    }

    /** 升级前 pipeline 中的源协议编解码器（如 {@link HttpClientCodec}） */

    public interface SourceCodec {

        /**
         * Removes or disables the encoder of this codec so that the {@link UpgradeCodec} can send an initial greeting
         * (if any).
         */
        void prepareUpgradeFrom(ChannelHandlerContext ctx);

        /**
         * Removes this codec (i.e. all associated handlers) from the pipeline.
         */
        void upgradeFrom(ChannelHandlerContext ctx);
    }

    /** 目标升级协议编解码器（如 HTTP/2、WebSocket） */

    public interface UpgradeCodec {
        /**
         * Returns the name of the protocol supported by this codec, as indicated by the {@code 'UPGRADE'} header.
         */
        CharSequence protocol();

        /**
         * Sets any protocol-specific headers required to the upgrade request. Returns the names of
         * all headers that were added. These headers will be used to populate the CONNECTION header.
         */
        Collection<CharSequence> setUpgradeHeaders(ChannelHandlerContext ctx, HttpRequest upgradeRequest);

        /**
         * Performs an HTTP protocol upgrade from the source codec. This method is responsible for
         * adding all handlers required for the new protocol.
         *
         * @param ctx the context for the current handler.
         * @param upgradeResponse the 101 Switching Protocols response that indicates that the server
         *            has switched to this protocol.
         */
        void upgradeTo(ChannelHandlerContext ctx, FullHttpResponse upgradeResponse) throws Exception;
    }

    private final SourceCodec sourceCodec;
    private final UpgradeCodec upgradeCodec;
    private UpgradeEvent currentUpgradeEvent;

    /**
     * Constructs the client upgrade handler.
     *
     * @param sourceCodec the codec that is being used initially.
     * @param upgradeCodec the codec that the client would like to upgrade to.
     * @param maxContentLength the maximum length of the aggregated content.
     */
    public HttpClientUpgradeHandler(SourceCodec sourceCodec, UpgradeCodec upgradeCodec,
                                    int maxContentLength) {
        super(maxContentLength);
        this.sourceCodec = ObjectUtil.checkNotNull(sourceCodec, "sourceCodec");
        this.upgradeCodec = ObjectUtil.checkNotNull(upgradeCodec, "upgradeCodec");
    }

    @Override
    public void bind(ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise promise) throws Exception {
        ctx.bind(localAddress, promise);
    }

    @Override
    public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress,
                        ChannelPromise promise) throws Exception {
        ctx.connect(remoteAddress, localAddress, promise);
    }

    @Override
    public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        ctx.disconnect(promise);
    }

    @Override
    public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        ctx.close(promise);
    }

    @Override
    public void deregister(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        ctx.deregister(promise);
    }

    @Override
    public void read(ChannelHandlerContext ctx) throws Exception {
        ctx.read();
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise)
            throws Exception {
        if (!(msg instanceof HttpRequest) || currentUpgradeEvent == UpgradeEvent.UPGRADE_SUCCESSFUL) {
            ctx.write(msg, promise);
            return;
        }

        if (currentUpgradeEvent == UpgradeEvent.UPGRADE_ISSUED) {
            // 升级进行中禁止再写 HTTP 请求，先释放消息再失败 promise
            ReferenceCountUtil.release(msg);
            promise.setFailure(new IllegalStateException(
                    "Attempting to write HTTP request with upgrade in progress"));
            return;
        }

        currentUpgradeEvent = UpgradeEvent.UPGRADE_ISSUED;
        setUpgradeRequestHeaders(ctx, (HttpRequest) msg);

        // Continue writing the request.
        ctx.write(msg, promise);

        // 通知下游：Upgrade 请求已发出
        ctx.fireUserEventTriggered(UpgradeEvent.UPGRADE_ISSUED);
        // Now we wait for the next HTTP response to see if we switch protocols.
    }

    @Override
    public void flush(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, HttpObject msg, List<Object> out)
            throws Exception {
        FullHttpResponse response = null;
        try {
            if (currentUpgradeEvent != UpgradeEvent.UPGRADE_ISSUED) {
                throw new IllegalStateException("Read HTTP response without requesting protocol switch");
            }

            if (msg instanceof HttpResponse) {
                HttpResponse rep = (HttpResponse) msg;
                if (!SWITCHING_PROTOCOLS.equals(rep.status())) {
                    // 非 101 响应：升级失败，移除 handler 并继续 HTTP 处理
                    currentUpgradeEvent = null;
                    ctx.fireUserEventTriggered(UpgradeEvent.UPGRADE_REJECTED);
                    removeThisHandler(ctx);
                    ctx.fireChannelRead(msg);
                    return;
                }
            }

            if (msg instanceof FullHttpResponse) {
                response = (FullHttpResponse) msg;
                // 基类 decode 返回后会 release，此处 retain 保活
                response.retain();
                out.add(response);
            } else {
                // Call the base class to handle the aggregation of the full request.
                super.decode(ctx, msg, out);
                if (out.isEmpty()) {
                    // The full request hasn't been created yet, still awaiting more data.
                    return;
                }

                assert out.size() == 1;
                response = (FullHttpResponse) out.get(0);
            }

            CharSequence upgradeHeader = response.headers().get(HttpHeaderNames.UPGRADE);
            if (upgradeHeader != null && !AsciiString.contentEqualsIgnoreCase(upgradeCodec.protocol(), upgradeHeader)) {
                throw new IllegalStateException(
                        "Switching Protocols response with unexpected UPGRADE protocol: " + upgradeHeader);
            }

            // 校验 UPGRADE 头后执行协议切换
            sourceCodec.prepareUpgradeFrom(ctx);
            upgradeCodec.upgradeTo(ctx, response);

            // Let's set currentUpgradeEvent to UPGRADE_SUCCESSFUL as we will notify the pipeline about the successful
            // upgrade now, which might results in a write that needs to be passed through.
            currentUpgradeEvent = UpgradeEvent.UPGRADE_SUCCESSFUL;

            // Notify that the upgrade to the new protocol completed successfully.
            ctx.fireUserEventTriggered(UpgradeEvent.UPGRADE_SUCCESSFUL);

            // 保证 UPGRADE_SUCCESSFUL 事件先于新协议帧到达下游
            sourceCodec.upgradeFrom(ctx);

            // We switched protocols, so we're done with the upgrade response.
            // Release it and clear it from the output.
            response.release();
            out.clear();
            removeThisHandler(ctx);
        } catch (Throwable t) {
            ctx.fireExceptionCaught(t);
            removeThisHandler(ctx);
        }
    }

    private static void removeThisHandler(ChannelHandlerContext ctx) {
        ctx.pipeline().remove(ctx.name());
    }

    /**
     * 为 Upgrade 请求设置 UPGRADE、CONNECTION 及协议相关头。
     */
    private void setUpgradeRequestHeaders(ChannelHandlerContext ctx, HttpRequest request) {
        // Set the UPGRADE header on the request.
        request.headers().set(HttpHeaderNames.UPGRADE, upgradeCodec.protocol());

        // Add all protocol-specific headers to the request.
        Set<CharSequence> connectionParts = new LinkedHashSet<CharSequence>(2);
        connectionParts.addAll(upgradeCodec.setUpgradeHeaders(ctx, request));

        // Set the CONNECTION header from the set of all protocol-specific headers that were added.
        StringBuilder builder = new StringBuilder();
        for (CharSequence part : connectionParts) {
            builder.append(part);
            builder.append(',');
        }
        builder.append(HttpHeaderValues.UPGRADE);
        request.headers().add(HttpHeaderNames.CONNECTION, builder.toString());
    }
}
