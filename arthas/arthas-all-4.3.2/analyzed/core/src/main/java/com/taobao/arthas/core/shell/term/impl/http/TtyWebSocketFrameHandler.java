/*
 * Copyright 2015 Julien Viet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.taobao.arthas.core.shell.term.impl.http;

import com.alibaba.arthas.deps.org.slf4j.Logger;
import com.alibaba.arthas.deps.org.slf4j.LoggerFactory;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;
import io.termd.core.function.Consumer;
import io.termd.core.http.HttpTtyConnection;
import io.termd.core.tty.TtyConnection;

import java.util.List;

/**
 * WebSocket 文本帧与 TTY 桥接处理器。
 * <p>
 * 握手完成后创建 {@link ExtHttpTtyConnection} 并移除 {@link HttpRequestHandler}；
 * 空闲时发送 Ping；支持 {@code quiet=true} 查询参数。
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class TtyWebSocketFrameHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

  private static final Logger logger = LoggerFactory.getLogger(TtyWebSocketFrameHandler.class);
  /** 暂存 WebSocket 握手前的原始 request URI（含 query） */
  static final AttributeKey<String> REQUEST_URI = AttributeKey.valueOf("arthas.websocket.requestUri");

  /** 全局 channel 组，握手后加入便于 shutdown */
  private final ChannelGroup group;
  private final Consumer<TtyConnection> handler;
  /** 当前 channel 绑定的 TTY 连接 */
  private HttpTtyConnection conn;

  public TtyWebSocketFrameHandler(ChannelGroup group, Consumer<TtyConnection> handler) {
    this.group = group;
    this.handler = handler;
  }

  @Override
  public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
    if (evt == WebSocketServerProtocolHandler.ServerHandshakeStateEvent.HANDSHAKE_COMPLETE) {
      // Netty 可能先触发旧事件；延迟执行兜底，优先使用带 requestUri 的 HandshakeComplete
      ctx.executor().execute(new Runnable() {
        @Override
        public void run() {
          handleHandshakeComplete(ctx, null);
        }
      });
    } else if (evt instanceof WebSocketServerProtocolHandler.HandshakeComplete) {
      WebSocketServerProtocolHandler.HandshakeComplete handshakeComplete =
          (WebSocketServerProtocolHandler.HandshakeComplete) evt;
      handleHandshakeComplete(ctx, handshakeComplete.requestUri());
    } else if (evt instanceof IdleStateEvent) {
      ctx.writeAndFlush(new PingWebSocketFrame());
    } else {
      super.userEventTriggered(ctx, evt);
    }
  }

  @Override
  public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    HttpTtyConnection tmp = conn;
    conn = null;
    if (tmp != null) {
      Consumer<Void> closeHandler = tmp.getCloseHandler();
      if (closeHandler != null) {
        closeHandler.accept(null);
      }
    }
  }

  @Override
  public void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
    HttpTtyConnection tmp = conn;
    if (tmp == null) {
      logger.warn("websocket frame received before handshake completed, closing channel");
      ctx.close();
      return;
    }
    tmp.writeToDecoder(msg.text());
  }

  /** 握手完成：注册 channel、创建 ExtHttpTtyConnection 并通知 Shell */
  private void handleHandshakeComplete(ChannelHandlerContext ctx, String requestUri) {
    if (conn != null) {
      return;
    }
    ctx.pipeline().remove(HttpRequestHandler.class);
    group.add(ctx.channel());
    conn = new ExtHttpTtyConnection(ctx, isQuietRequest(ctx, requestUri));
    handler.accept(conn);
  }

  static boolean isQuietRequest(ChannelHandlerContext ctx, String requestUri) {
    if (requestUri == null && ctx != null) {
      requestUri = ctx.channel().attr(REQUEST_URI).get();
    }
    return isQuietRequest(requestUri);
  }

  /** 解析 URI 中 {@code quiet=true} 表示静默模式 */
  static boolean isQuietRequest(String requestUri) {
    if (requestUri == null) {
      return false;
    }
    List<String> values = new QueryStringDecoder(requestUri).parameters().get("quiet");
    if (values == null) {
      return false;
    }
    for (String value : values) {
      if ("true".equalsIgnoreCase(value)) {
        return true;
      }
    }
    return false;
  }
}
