package com.taobao.arthas.core.shell.term.impl.http;

import com.alibaba.arthas.deps.org.slf4j.Logger;
import com.alibaba.arthas.deps.org.slf4j.LoggerFactory;
import com.taobao.arthas.common.IOUtils;
import com.taobao.arthas.core.server.ArthasBootstrap;
import com.taobao.arthas.core.shell.term.impl.http.api.HttpApiHandler;
import com.taobao.arthas.mcp.server.protocol.server.handler.McpHttpRequestHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.termd.core.http.HttpTtyConnection;
import io.termd.core.util.Logging;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

import static com.taobao.arthas.core.util.HttpUtils.createRedirectResponse;
import static com.taobao.arthas.core.util.HttpUtils.createResponse;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * HTTP 请求总入口：REST API、MCP、Web UI 静态资源与输出目录浏览。
 * <p>
 * WebSocket 升级路径透传给后续 handler；其余路径按 api → mcp → classpath
 * → {@link DirectoryBrowser} 顺序解析。
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 * @author hengyunabc 2019-11-06
 * @author gongdewei 2020-03-18
 */
public class HttpRequestHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private static final Logger logger = LoggerFactory.getLogger(HttpRequestHandler.class);

    /** WebSocket 升级路径（如 /ws） */
    private final String wsUri;

    /** 可浏览的输出根目录（arthas-output） */
    private File dir;

    /** {@code /api} REST 处理器 */
    private HttpApiHandler httpApiHandler;

    /** MCP 协议 HTTP 处理器（可为 null） */
    private McpHttpRequestHandler mcpRequestHandler;

    public HttpRequestHandler(String wsUri) {
        this(wsUri, ArthasBootstrap.getInstance().getOutputPath());
    }

    /** @param wsUri WebSocket 路径；@param dir 输出目录根路径 */
    public HttpRequestHandler(String wsUri, File dir) {
        this.wsUri = wsUri;
        this.dir = dir;
        dir.mkdirs();
        this.httpApiHandler = ArthasBootstrap.getInstance().getHttpApiHandler();
        this.mcpRequestHandler = ArthasBootstrap.getInstance().getMcpRequestHandler();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        String path = new URI(request.uri()).getPath();
        if (wsUri.equalsIgnoreCase(path)) {
            ctx.channel().attr(TtyWebSocketFrameHandler.REQUEST_URI).set(request.uri());
            ctx.fireChannelRead(request.retain());
        } else {
            if (HttpUtil.is100ContinueExpected(request)) {
                send100Continue(ctx);
            }

            HttpResponse response = null;
            if ("/".equals(path)) {
                path = "/index.html";
            }

            boolean isFileResponseFinished = false;
            boolean isMcpHandled = false;
            try {
                // 处理 /api REST 接口
                if ("/api".equals(path)) {
                    response = httpApiHandler.handle(ctx, request);
                }

                // 处理 MCP 端点请求
                if (mcpRequestHandler != null) {
                    String mcpEndpoint = mcpRequestHandler.getMcpEndpoint();
                    if (mcpEndpoint.equals(path)) {
                        mcpRequestHandler.handle(ctx, request);
                        isMcpHandled = true;
                        return;
                    }
                }

                // Web UI 路径重定向与 index
                if (path.equals("/ui")) {
                    response = createRedirectResponse(request, "/ui/");
                }
                if (path.equals("/ui/")) {
                    path += "index.html";
                }

                // 优先从 classpath 加载静态资源
                if (response == null) {
                    response = readFileFromResource(request, path);
                }

                // 再从输出目录查找，避免覆盖 classpath 资源
                if (response == null) {
                    response = DirectoryBrowser.directView(dir, path, request, ctx);
                    isFileResponseFinished = response != null;
                }

                // 均未命中则 404
                if (response == null) {
                    response = createResponse(request, HttpResponseStatus.NOT_FOUND, "Not found");
                }
            } catch (Throwable e) {
                logger.error("arthas process http request error: " + request.uri(), e);
            } finally {
                // 异常或未命中时兜底 500
                if (response == null) {
                    response = createResponse(request, HttpResponseStatus.INTERNAL_SERVER_ERROR, "Server error");
                }
                if (!isFileResponseFinished && !isMcpHandled) {
                    ChannelFuture future = writeResponse(ctx, response);
                    future.addListener(ChannelFutureListener.CLOSE);
                }
            }
        }
    }

    private ChannelFuture writeResponse(ChannelHandlerContext ctx, HttpResponse response) {
        // 为 FullHttpResponse 补充 Content-Length 并关闭连接
        if (!HttpUtil.isTransferEncodingChunked(response)
                && response instanceof DefaultFullHttpResponse) {
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH,
                    ((DefaultFullHttpResponse) response).content().readableBytes());
            return ctx.writeAndFlush(response);
        }

        // 分块响应：写 header 后发送 EMPTY_LAST_CONTENT
        ctx.write(response);
        return ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
    }

    private HttpResponse readFileFromResource(FullHttpRequest request, String path) throws IOException {
        DefaultFullHttpResponse fullResp = null;
        InputStream in = null;
        try {
            URL res = HttpTtyConnection.class.getResource("/com/taobao/arthas/core/http" + path);
            if (res != null) {
                fullResp = new DefaultFullHttpResponse(request.protocolVersion(),
                        HttpResponseStatus.OK);
                in = res.openStream();
                byte[] tmp = new byte[256];
                for (int l = 0; l != -1; l = in.read(tmp)) {
                    fullResp.content().writeBytes(tmp, 0, l);
                }
                int li = path.lastIndexOf('.');
                if (li != -1 && li != path.length() - 1) {
                    String ext = path.substring(li + 1);
                    String contentType;
                    if ("html".equals(ext)) {
                        contentType = "text/html";
                    } else if ("js".equals(ext)) {
                        contentType = "application/javascript";
                    } else if ("css".equals(ext)) {
                        contentType = "text/css";
                    } else {
                        contentType = null;
                    }

                    if (contentType != null) {
                        fullResp.headers().set(HttpHeaderNames.CONTENT_TYPE, contentType);
                    }
                }
            }
        } finally {
            IOUtils.close(in);
        }
        return fullResp;
    }

    private static void send100Continue(ChannelHandlerContext ctx) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.CONTINUE);
        ctx.writeAndFlush(response);
    }

    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        Logging.logReportedIoError(cause);
        ctx.close();
    }
}
