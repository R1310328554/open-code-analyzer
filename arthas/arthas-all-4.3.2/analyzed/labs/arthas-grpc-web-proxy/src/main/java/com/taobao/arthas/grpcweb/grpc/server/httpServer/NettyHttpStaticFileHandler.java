package com.taobao.arthas.grpcweb.grpc.server.httpServer;
 
import com.alibaba.arthas.deps.org.slf4j.Logger;
import com.alibaba.arthas.deps.org.slf4j.LoggerFactory;
import io.netty.channel.*;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpChunkedInput;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedFile;
 
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;

import javax.activation.MimetypesFileTypeMap;

/**
 * Netty HTTP 静态文件 Handler：按 URI 映射本地文件并以零拷贝或分块方式响应。
 * <p>
 * 根路径 {@code /} 重定向到 {@code index.html}；隐藏、目录或不存在文件返回 404。
 */
public class NettyHttpStaticFileHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass().getName());
    /** 静态资源根目录 */
    private final String STATIC_LOCATION;

    public NettyHttpStaticFileHandler(String staticLocation){
        this.STATIC_LOCATION = staticLocation;
    }
 
    /**
     * 解析请求 URI，定位磁盘文件并写入 HTTP 响应（支持 Keep-Alive 与 SSL 分块传输）。
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws URISyntaxException, IOException {
        String uri = new URI(request.uri()).getPath();
        // 忽略 favicon 请求
        if ("/favicon.ico".equals(uri)) {
            return;
        }
        if ("/".equals(uri)) {
            uri = "/index.html";
        }
        String path = Paths.get(this.STATIC_LOCATION, uri).toString();
        File file = new File(path);
        if (HttpUtil.is100ContinueExpected(request)) {
            send100Continue(ctx);
        }
        // 隐藏、不存在、目录或非普通文件均返回 404
        if (file.isHidden() || !file.exists() || file.isDirectory() || !file.isFile()) {
            sendNotFound(ctx);
            return;
        }
        final RandomAccessFile randomAccessFile;
        try {
            randomAccessFile = new RandomAccessFile(file, "r");
        } catch (FileNotFoundException e) {
            sendNotFound(ctx);
            throw new RuntimeException(e);
        }
        HttpResponse response = new DefaultHttpResponse(request.protocolVersion(), HttpResponseStatus.OK);
 
        // 按扩展名设置 Content-Type
        if (path.endsWith(".html")){
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=UTF-8");
        }else if(path.endsWith(".js")){
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/x-javascript");
        }else if(path.endsWith(".css")){
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/css; charset=UTF-8");
        }else{
        	MimetypesFileTypeMap mimetypesFileTypeMap = new MimetypesFileTypeMap();
        	response.headers().set(HttpHeaderNames.CONTENT_TYPE, mimetypesFileTypeMap.getContentType(path));
        }

        boolean keepAlive =  HttpUtil.isKeepAlive(request);
 
        if (keepAlive) {
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH, randomAccessFile.length());
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        }
        ctx.write(response);
 
        ChannelFuture sendFileFuture;
        ChannelFuture lastContentFuture;
        // 明文连接使用 FileRegion 零拷贝；SSL 管道需分块写入
        if (ctx.pipeline().get(SslHandler.class) == null) {
            sendFileFuture =
                    ctx.write(new DefaultFileRegion(randomAccessFile.getChannel(), 0, randomAccessFile.length()), ctx.newProgressivePromise());
            lastContentFuture = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
        } else {
            sendFileFuture =
                    ctx.writeAndFlush(new HttpChunkedInput(new ChunkedFile(randomAccessFile, 0, randomAccessFile.length(), 10 * 1024 * 1024)),
                            ctx.newProgressivePromise());
            lastContentFuture = sendFileFuture;
        }
 
        sendFileFuture.addListener(new ChannelProgressiveFutureListener() {
            @Override
            public void operationProgressed(ChannelProgressiveFuture future, long progress, long total) {
                if (total < 0) {
                    logger.info(future.channel() + " Transfer progress: " + progress);
                } else {
                    logger.info(future.channel() + " Transfer progress: " + progress + " / " + total);
                }
            }
 
            @Override
            public void operationComplete(ChannelProgressiveFuture future) {
                logger.info(future.channel() + " Transfer complete.");
            }
        });

        if (!HttpUtil.isKeepAlive(request)) {
            lastContentFuture.addListener(ChannelFutureListener.CLOSE);
        }
    }
 
    /** 响应 HTTP/1.1 100 Continue */
    private static void send100Continue(ChannelHandlerContext ctx) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE);
        ctx.writeAndFlush(response);
    }
    
    /** 返回空体的 404 Not Found */
    private static void sendNotFound(ChannelHandlerContext ctx){
    	FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND);
    	response.headers().set(HttpHeaderNames.CONTENT_LENGTH, 0);
    	ctx.writeAndFlush(response);
    }
}
