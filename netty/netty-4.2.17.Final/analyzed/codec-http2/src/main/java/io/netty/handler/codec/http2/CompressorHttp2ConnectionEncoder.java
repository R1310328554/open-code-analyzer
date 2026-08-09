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
package io.netty.handler.codec.http2;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelConfig;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import io.netty.channel.ChannelPromise;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.compression.BrotliEncoder;
import io.netty.handler.codec.compression.ZlibCodecFactory;
import io.netty.handler.codec.compression.ZlibWrapper;
import io.netty.handler.codec.compression.Brotli;
import io.netty.handler.codec.compression.BrotliOptions;
import io.netty.handler.codec.compression.CompressionOptions;
import io.netty.handler.codec.compression.DeflateOptions;
import io.netty.handler.codec.compression.GzipOptions;
import io.netty.handler.codec.compression.StandardCompressionOptions;
import io.netty.handler.codec.compression.Zstd;
import io.netty.handler.codec.compression.ZstdEncoder;
import io.netty.handler.codec.compression.ZstdOptions;
import io.netty.handler.codec.compression.SnappyFrameEncoder;
import io.netty.handler.codec.compression.SnappyOptions;
import io.netty.util.concurrent.PromiseCombiner;
import io.netty.util.internal.ObjectUtil;

import java.util.ArrayList;
import java.util.List;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_ENCODING;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaderValues.BR;
import static io.netty.handler.codec.http.HttpHeaderValues.DEFLATE;
import static io.netty.handler.codec.http.HttpHeaderValues.GZIP;
import static io.netty.handler.codec.http.HttpHeaderValues.IDENTITY;
import static io.netty.handler.codec.http.HttpHeaderValues.X_DEFLATE;
import static io.netty.handler.codec.http.HttpHeaderValues.X_GZIP;
import static io.netty.handler.codec.http.HttpHeaderValues.ZSTD;
import static io.netty.handler.codec.http.HttpHeaderValues.SNAPPY;

/**
 * HTTP/2 编码器装饰器：按每条流的 {@code content-encoding} 头对 DATA 帧做压缩。
 * <p>在 {@link #writeHeaders} 时解析编码类型、创建 per-stream {@link io.netty.channel.embedded.EmbeddedChannel} 压缩器，
 * 在 {@link #writeData} 中将明文写入压缩器再写出压缩后的 DATA；流结束时释放压缩器。
 */
public class CompressorHttp2ConnectionEncoder extends DecoratingHttp2ConnectionEncoder {
    // 保留以兼容旧 API，不可删除
    public static final int DEFAULT_COMPRESSION_LEVEL = 6;
    public static final int DEFAULT_WINDOW_BITS = 15;
    public static final int DEFAULT_MEM_LEVEL = 8;

    private int compressionLevel;
    private int windowBits;
    private int memLevel;
    private final Http2Connection.PropertyKey propertyKey;

    private final boolean supportsCompressionOptions;

    private BrotliOptions brotliOptions;
    private GzipOptions gzipCompressionOptions;
    private DeflateOptions deflateOptions;
    private ZstdOptions zstdOptions;
    private SnappyOptions snappyOptions;

    /**
     * 使用 {@link StandardCompressionOptions} 默认算法集（gzip、deflate、snappy，及可选 brotli/zstd）创建实例。
     */
    public CompressorHttp2ConnectionEncoder(Http2ConnectionEncoder delegate) {
        this(delegate, defaultCompressionOptions());
    }

    private static CompressionOptions[] defaultCompressionOptions() {
        List<CompressionOptions> compressionOptions = new ArrayList<CompressionOptions>();
        compressionOptions.add(StandardCompressionOptions.gzip());
        compressionOptions.add(StandardCompressionOptions.deflate());
        compressionOptions.add(StandardCompressionOptions.snappy());
        if (Brotli.isAvailable()) {
            compressionOptions.add(StandardCompressionOptions.brotli());
        }
        if (Zstd.isAvailable()) {
            compressionOptions.add(StandardCompressionOptions.zstd());
        }
        return compressionOptions.toArray(new CompressionOptions[0]);
    }

    /**
     * Create a new {@link CompressorHttp2ConnectionEncoder} instance
     */
    @Deprecated
    public CompressorHttp2ConnectionEncoder(Http2ConnectionEncoder delegate, int compressionLevel, int windowBits,
                                            int memLevel) {
        super(delegate);
        this.compressionLevel = ObjectUtil.checkInRange(compressionLevel, 0, 9, "compressionLevel");
        this.windowBits = ObjectUtil.checkInRange(windowBits, 9, 15, "windowBits");
        this.memLevel = ObjectUtil.checkInRange(memLevel, 1, 9, "memLevel");

        propertyKey = connection().newKey();
        connection().addListener(new Http2ConnectionAdapter() {
            @Override
            public void onStreamRemoved(Http2Stream stream) {
                final EmbeddedChannel compressor = stream.getProperty(propertyKey);
                if (compressor != null) {
                    cleanup(stream, compressor);
                }
            }
        });

        supportsCompressionOptions = false;
    }

    /**
     * 按显式 {@link CompressionOptions} 配置支持的压缩算法创建实例。
     */
    public CompressorHttp2ConnectionEncoder(Http2ConnectionEncoder delegate,
                                            CompressionOptions... compressionOptionsArgs) {
        super(delegate);
        ObjectUtil.checkNotNull(compressionOptionsArgs, "CompressionOptions");
        ObjectUtil.deepCheckNotNull("CompressionOptions", compressionOptionsArgs);

        for (CompressionOptions compressionOptions : compressionOptionsArgs) {
            // BrotliOptions' class initialization depends on Brotli classes being on the classpath.
            // The Brotli.isAvailable check ensures that BrotliOptions will only get instantiated if Brotli is on
            // the classpath.
            // This results in the static analysis of native-image identifying the instanceof BrotliOptions check
            // and thus BrotliOptions itself as unreachable, enabling native-image to link all classes at build time
            // and not complain about the missing Brotli classes.
            if (Brotli.isAvailable() && compressionOptions instanceof BrotliOptions) {
                brotliOptions = (BrotliOptions) compressionOptions;
            } else if (compressionOptions instanceof GzipOptions) {
                gzipCompressionOptions = (GzipOptions) compressionOptions;
            } else if (compressionOptions instanceof DeflateOptions) {
                deflateOptions = (DeflateOptions) compressionOptions;
            } else if (compressionOptions instanceof ZstdOptions) {
                zstdOptions = (ZstdOptions) compressionOptions;
            } else if (compressionOptions instanceof SnappyOptions) {
                snappyOptions = (SnappyOptions) compressionOptions;
            } else {
                throw new IllegalArgumentException("Unsupported " + CompressionOptions.class.getSimpleName() +
                        ": " + compressionOptions);
            }
        }

        supportsCompressionOptions = true;

        propertyKey = connection().newKey();
        connection().addListener(new Http2ConnectionAdapter() {
            @Override
            public void onStreamRemoved(Http2Stream stream) {
                final EmbeddedChannel compressor = stream.getProperty(propertyKey);
                if (compressor != null) {
                    cleanup(stream, compressor);
                }
            }
        });
    }

    @Override
    public ChannelFuture writeData(final ChannelHandlerContext ctx, final int streamId, ByteBuf data, int padding,
            final boolean endOfStream, ChannelPromise promise) {
        final Http2Stream stream = connection().stream(streamId);
        final EmbeddedChannel channel = stream == null ? null : (EmbeddedChannel) stream.getProperty(propertyKey);
        if (channel == null) {
            // 该流无压缩器（未设置 content-encoding 或不支持），直接透传
            return super.writeData(ctx, streamId, data, padding, endOfStream, promise);
        }

        try {
            // 压缩器会在 writeOutbound 后释放入站 buffer
            channel.writeOutbound(data);
            ByteBuf buf = nextReadableBuf(channel);
            if (buf == null) {
                if (endOfStream) {
                    if (channel.finish()) {
                        buf = nextReadableBuf(channel);
                    }
                    return super.writeData(ctx, streamId, buf == null ? Unpooled.EMPTY_BUFFER : buf, padding,
                            true, promise);
                }
                // 尚未 END_STREAM，压缩器可能还有缓冲，先成功返回等待后续 DATA
                promise.setSuccess();
                return promise;
            }

            PromiseCombiner combiner = new PromiseCombiner(ctx.executor());
            for (;;) {
                ByteBuf nextBuf = nextReadableBuf(channel);
                boolean compressedEndOfStream = nextBuf == null && endOfStream;
                if (compressedEndOfStream && channel.finish()) {
                    nextBuf = nextReadableBuf(channel);
                    compressedEndOfStream = nextBuf == null;
                }

                ChannelPromise bufPromise = ctx.newPromise();
                combiner.add(bufPromise);
                super.writeData(ctx, streamId, buf, padding, compressedEndOfStream, bufPromise);
                if (nextBuf == null) {
                    break;
                }

                padding = 0; // padding 仅在首个压缩 DATA 分片上携带
                buf = nextBuf;
            }
            combiner.finish(promise);
        } catch (Throwable cause) {
            promise.tryFailure(cause);
        } finally {
            if (endOfStream) {
                cleanup(stream, channel);
            }
        }
        return promise;
    }

    @Override
    public ChannelFuture writeHeaders(ChannelHandlerContext ctx, int streamId, Http2Headers headers, int padding,
            boolean endStream, ChannelPromise promise) {
        try {
            // 根据 content-encoding 决定是否压缩，并清理/改写相关头域
            EmbeddedChannel compressor = newCompressor(ctx, headers, endStream);

            // 写出 HEADERS 并创建 Http2Stream 对象
            ChannelFuture future = super.writeHeaders(ctx, streamId, headers, padding, endStream, promise);

            // stream 创建后将压缩器绑定到流属性，供后续 writeData 使用
            bindCompressorToStream(compressor, streamId);

            return future;
        } catch (Throwable e) {
            promise.tryFailure(e);
        }
        return promise;
    }

    @Override
    public ChannelFuture writeHeaders(final ChannelHandlerContext ctx, final int streamId, final Http2Headers headers,
            final int streamDependency, final short weight, final boolean exclusive, final int padding,
            final boolean endOfStream, final ChannelPromise promise) {
        try {
            EmbeddedChannel compressor = newCompressor(ctx, headers, endOfStream);

            ChannelFuture future = super.writeHeaders(ctx, streamId, headers, streamDependency, weight, exclusive,
                                                      padding, endOfStream, promise);

            bindCompressorToStream(compressor, streamId);

            return future;
        } catch (Throwable e) {
            promise.tryFailure(e);
        }
        return promise;
    }

    /**
     * 按 {@code content-encoding} 创建压缩用的 {@link EmbeddedChannel}。
     *
     * @param ctx the context.
     * @param contentEncoding {@code content-encoding} 头取值
     * @return 支持该编码的压缩通道；不支持或为 identity 时返回 {@code null}
     * @throws Http2Exception 编码非法且应中断连接时
     */
    protected EmbeddedChannel newContentCompressor(ChannelHandlerContext ctx, CharSequence contentEncoding)
            throws Http2Exception {
        if (GZIP.contentEqualsIgnoreCase(contentEncoding) || X_GZIP.contentEqualsIgnoreCase(contentEncoding)) {
            return newCompressionChannel(ctx, ZlibWrapper.GZIP);
        }
        if (DEFLATE.contentEqualsIgnoreCase(contentEncoding) || X_DEFLATE.contentEqualsIgnoreCase(contentEncoding)) {
            return newCompressionChannel(ctx, ZlibWrapper.ZLIB);
        }
        Channel channel = ctx.channel();
        if (Brotli.isAvailable() && brotliOptions != null && BR.contentEqualsIgnoreCase(contentEncoding)) {
            return EmbeddedChannel.builder()
                    .channelId(channel.id())
                    .hasDisconnect(channel.metadata().hasDisconnect())
                    .config(channel.config())
                    .handlers(new BrotliEncoder(brotliOptions.parameters()))
                    .build();
        }
        if (zstdOptions != null && ZSTD.contentEqualsIgnoreCase(contentEncoding)) {
            return EmbeddedChannel.builder()
                    .channelId(channel.id())
                    .hasDisconnect(channel.metadata().hasDisconnect())
                    .config(channel.config())
                    .handlers(new ZstdEncoder(zstdOptions.compressionLevel(),
                            zstdOptions.blockSize(), zstdOptions.maxEncodeSize()))
                    .build();
        }
        if (snappyOptions != null && SNAPPY.contentEqualsIgnoreCase(contentEncoding)) {
            return EmbeddedChannel.builder()
                    .channelId(channel.id())
                    .hasDisconnect(channel.metadata().hasDisconnect())
                    .config(channel.config())
                    .handlers(new SnappyFrameEncoder())
                    .build();
        }
        // 'identity' 或不支持的编码 — 不压缩
        return null;
    }

    /**
     * Returns the expected content encoding of the decoded content. Returning {@code contentEncoding} is the default
     * behavior, which is the case for most compressors.
     *
     * @param contentEncoding the value of the {@code content-encoding} header
     * @return the expected content encoding of the new content.
     * @throws Http2Exception if the {@code contentEncoding} is not supported and warrants an exception
     */
    protected CharSequence getTargetContentEncoding(CharSequence contentEncoding) throws Http2Exception {
        return contentEncoding;
    }

    /**
     * Generate a new instance of an {@link EmbeddedChannel} capable of compressing data
     * @param ctx the context.
     * @param wrapper Defines what type of encoder should be used
     */
    private EmbeddedChannel newCompressionChannel(final ChannelHandlerContext ctx, ZlibWrapper wrapper) {
        Channel channel = ctx.channel();
        if (supportsCompressionOptions) {
            if (wrapper == ZlibWrapper.GZIP && gzipCompressionOptions != null) {
                return EmbeddedChannel.builder()
                        .channelId(channel.id())
                        .hasDisconnect(channel.metadata().hasDisconnect())
                        .config(channel.config())
                        .handlers(ZlibCodecFactory.newZlibEncoder(wrapper,
                                gzipCompressionOptions.compressionLevel(),
                                gzipCompressionOptions.windowBits(),
                                gzipCompressionOptions.memLevel())
                        )
                        .build();
            } else if (wrapper == ZlibWrapper.ZLIB && deflateOptions != null) {
                return EmbeddedChannel.builder()
                        .channelId(channel.id())
                        .hasDisconnect(channel.metadata().hasDisconnect())
                        .config(channel.config())
                        .handlers(ZlibCodecFactory.newZlibEncoder(wrapper,
                                deflateOptions.compressionLevel(),
                                deflateOptions.windowBits(),
                                deflateOptions.memLevel())
                        )
                        .build();
            } else {
                throw new IllegalArgumentException("Unsupported ZlibWrapper: " + wrapper);
            }
        } else {
            return EmbeddedChannel.builder()
                    .channelId(channel.id())
                    .hasDisconnect(channel.metadata().hasDisconnect())
                    .config(channel.config())
                    .handlers(ZlibCodecFactory.newZlibEncoder(wrapper, compressionLevel, windowBits, memLevel))
                    .build();
        }
    }

    /**
     * 解析 {@code content-encoding}，必要时改写头域（移除 {@code Content-Length} 等）。
     *
     * @param ctx the context.
     * @param headers 待写出的 HEADERS
     * @param endOfStream 是否同时结束流（无 DATA 时不创建压缩器）
     * @return 绑定到该流的压缩通道，或 {@code null}
     */
    private EmbeddedChannel newCompressor(ChannelHandlerContext ctx, Http2Headers headers, boolean endOfStream)
            throws Http2Exception {
        if (endOfStream) {
            return null;
        }

        CharSequence encoding = headers.get(CONTENT_ENCODING);
        if (encoding == null) {
            encoding = IDENTITY;
        }
        final EmbeddedChannel compressor = newContentCompressor(ctx, encoding);
        if (compressor != null) {
            CharSequence targetContentEncoding = getTargetContentEncoding(encoding);
            if (IDENTITY.contentEqualsIgnoreCase(targetContentEncoding)) {
                headers.remove(CONTENT_ENCODING);
            } else {
                headers.set(CONTENT_ENCODING, targetContentEncoding);
            }

            // Content-Length 针对未压缩体；压缩后长度未知，故删除以免误导对端
            headers.remove(CONTENT_LENGTH);
        }

        return compressor;
    }

    /**
     * Called after the super class has written the headers and created any associated stream objects.
     * @param compressor The compressor associated with the stream identified by {@code streamId}.
     * @param streamId The stream id for which the headers were written.
     */
    private void bindCompressorToStream(EmbeddedChannel compressor, int streamId) {
        if (compressor != null) {
            Http2Stream stream = connection().stream(streamId);
            if (stream != null) {
                stream.setProperty(propertyKey, compressor);
            }
        }
    }

    /**
     * 释放 {@link EmbeddedChannel} 中剩余压缩输出，并从 {@link Http2Stream} 移除压缩器属性。
     */
    void cleanup(Http2Stream stream, EmbeddedChannel compressor) {
        compressor.finishAndReleaseAll();
        stream.removeProperty(propertyKey);
    }

    /**
     * Read the next compressed {@link ByteBuf} from the {@link EmbeddedChannel} or {@code null} if one does not exist.
     *
     * @param compressor The channel to read from
     * @return The next decoded {@link ByteBuf} from the {@link EmbeddedChannel} or {@code null} if one does not exist
     */
    private static ByteBuf nextReadableBuf(EmbeddedChannel compressor) {
        for (;;) {
            final ByteBuf buf = compressor.readOutbound();
            if (buf == null) {
                return null;
            }
            if (!buf.isReadable()) {
                buf.release();
                continue;
            }
            return buf;
        }
    }
}
