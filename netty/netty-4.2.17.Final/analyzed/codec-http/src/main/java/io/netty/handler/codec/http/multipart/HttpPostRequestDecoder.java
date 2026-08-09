/*
 * Copyright 2012 The Netty Project
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
package io.netty.handler.codec.http.multipart;

import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.http.HttpConstants;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.StringUtil;

import java.nio.charset.Charset;
import java.util.List;

/**
 * POST 请求体解码器门面，按 Content-Type 委托 multipart 或标准表单解码器。
 * <p>
 * 支持分块传输；完成后<strong>必须</strong>调用 {@link #destroy()}。
 * 默认丢弃阈值 {@value #DEFAULT_DISCARD_THRESHOLD} 字节，最大字段数 {@value #DEFAULT_MAX_FIELDS}。
 */
public class HttpPostRequestDecoder implements InterfaceHttpPostRequestDecoder {

    /** 默认缓冲丢弃阈值：10 MiB。 */
    static final int DEFAULT_DISCARD_THRESHOLD = 10 * 1024 * 1024;

    /** 默认单表单最大字段数。 */
    static final int DEFAULT_MAX_FIELDS = 128;

    /** 解码单字段时默认最大缓冲字节数。 */
    static final int DEFAULT_MAX_BUFFERED_BYTES = 1024;

    private final InterfaceHttpPostRequestDecoder decoder;

    /**
     *
     * @param request
     *            the request to decode
     * @throws NullPointerException
     *             for request
     * @throws ErrorDataDecoderException
     *             if the default charset was wrong when decoding or other
     *             errors
     */
    public HttpPostRequestDecoder(HttpRequest request) {
        this(new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE), request, HttpConstants.DEFAULT_CHARSET);
    }

    /**
     *
     * @param request
     *            the request to decode
     * @param maxFields
     *            the maximum number of fields the form can have, {@code -1} to disable
     * @param maxBufferedBytes
     *            the maximum number of bytes the decoder can buffer when decoding a field, {@code -1} to disable
     * @throws NullPointerException
     *             for request
     * @throws ErrorDataDecoderException
     *             if the default charset was wrong when decoding or other
     *             errors
     */
    public HttpPostRequestDecoder(HttpRequest request, int maxFields, int maxBufferedBytes) {
        this(new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE), request, HttpConstants.DEFAULT_CHARSET,
             maxFields, maxBufferedBytes);
    }

    /**
     *
     * @param factory
     *            the factory used to create InterfaceHttpData
     * @param request
     *            the request to decode
     * @throws NullPointerException
     *             for request or factory
     * @throws ErrorDataDecoderException
     *             if the default charset was wrong when decoding or other
     *             errors
     */
    public HttpPostRequestDecoder(HttpDataFactory factory, HttpRequest request) {
        this(factory, request, HttpConstants.DEFAULT_CHARSET);
    }

    /**
     *
     * @param factory
     *            the factory used to create InterfaceHttpData
     * @param request
     *            the request to decode
     * @param charset
     *            the charset to use as default
     * @throws NullPointerException
     *             for request or charset or factory
     * @throws ErrorDataDecoderException
     *             if the default charset was wrong when decoding or other
     *             errors
     */
    public HttpPostRequestDecoder(HttpDataFactory factory, HttpRequest request, Charset charset) {
        ObjectUtil.checkNotNull(factory, "factory");
        ObjectUtil.checkNotNull(request, "request");
        ObjectUtil.checkNotNull(charset, "charset");

        // 按 Content-Type 选择具体解码器实现
        if (isMultipart(request)) {
            decoder = new HttpPostMultipartRequestDecoder(factory, request, charset);
        } else {
            decoder = new HttpPostStandardRequestDecoder(factory, request, charset);
        }
    }

    /**
     *
     * @param factory
     *            the factory used to create InterfaceHttpData
     * @param request
     *            the request to decode
     * @param charset
     *            the charset to use as default
     * @param maxFields
     *            the maximum number of fields the form can have, {@code -1} to disable
     * @param maxBufferedBytes
     *            the maximum number of bytes the decoder can buffer when decoding a field, {@code -1} to disable
     * @throws NullPointerException
     *             for request or charset or factory
     * @throws ErrorDataDecoderException
     *             if the default charset was wrong when decoding or other
     *             errors
     */
    public HttpPostRequestDecoder(HttpDataFactory factory, HttpRequest request, Charset charset,
                                  int maxFields, int maxBufferedBytes) {
        ObjectUtil.checkNotNull(factory, "factory");
        ObjectUtil.checkNotNull(request, "request");
        ObjectUtil.checkNotNull(charset, "charset");

        // Fill default values
        if (isMultipart(request)) {
            decoder = new HttpPostMultipartRequestDecoder(factory, request, charset, maxFields, maxBufferedBytes);
        } else {
            decoder = new HttpPostStandardRequestDecoder(factory, request, charset, maxFields, maxBufferedBytes);
        }
    }

    /**
     * states follow NOTSTARTED PREAMBLE ( (HEADERDELIMITER DISPOSITION (FIELD |
     * FILEUPLOAD))* (HEADERDELIMITER DISPOSITION MIXEDPREAMBLE (MIXEDDELIMITER
     * MIXEDDISPOSITION MIXEDFILEUPLOAD)+ MIXEDCLOSEDELIMITER)* CLOSEDELIMITER)+
     * EPILOGUE
     *
     * First getStatus is: NOSTARTED
     *
     * Content-type: multipart/form-data, boundary=AaB03x => PREAMBLE in Header
     *
     * --AaB03x => HEADERDELIMITER content-disposition: form-data; name="field1"
     * => DISPOSITION
     *
     * Joe Blow => FIELD --AaB03x => HEADERDELIMITER content-disposition:
     * form-data; name="pics" => DISPOSITION Content-type: multipart/mixed,
     * boundary=BbC04y
     *
     * --BbC04y => MIXEDDELIMITER Content-disposition: attachment;
     * filename="file1.txt" => MIXEDDISPOSITION Content-Type: text/plain
     *
     * ... contents of file1.txt ... => MIXEDFILEUPLOAD --BbC04y =>
     * MIXEDDELIMITER Content-disposition: file; filename="file2.gif" =>
     * MIXEDDISPOSITION Content-type: image/gif Content-Transfer-Encoding:
     * binary
     *
     * ...contents of file2.gif... => MIXEDFILEUPLOAD --BbC04y-- =>
     * MIXEDCLOSEDELIMITER --AaB03x-- => CLOSEDELIMITER
     *
     * 找到 CLOSEDELIMITER 后进入 EPILOGUE 终态。
     */
    /** multipart 解码状态机各阶段。 */
    protected enum MultiPartStatus {
        NOTSTARTED, PREAMBLE, HEADERDELIMITER, DISPOSITION, FIELD, FILEUPLOAD, MIXEDPREAMBLE, MIXEDDELIMITER,
        MIXEDDISPOSITION, MIXEDFILEUPLOAD, MIXEDCLOSEDELIMITER, CLOSEDELIMITER, PREEPILOGUE, EPILOGUE
    }

    /** 判断请求是否为 {@code multipart/form-data}。 */

    public static boolean isMultipart(HttpRequest request) {
        String mimeType = request.headers().get(HttpHeaderNames.CONTENT_TYPE);
        if (mimeType != null && mimeType.startsWith(HttpHeaderValues.MULTIPART_FORM_DATA.toString())) {
            return getMultipartDataBoundary(mimeType) != null;
        }
        return false;
    }

    /** 从 Content-Type 解析 boundary 与可选 charset，失败返回 {@code null}。 */

    protected static String[] getMultipartDataBoundary(String contentType) {
        // 解析 multipart/form-data; boundary=... [; charset=...]
        String[] headerContentType = splitHeaderContentType(contentType);
        final String multiPartHeader = HttpHeaderValues.MULTIPART_FORM_DATA.toString();
        if (headerContentType[0].regionMatches(true, 0, multiPartHeader, 0 , multiPartHeader.length())) {
            int mrank;
            int crank;
            final String boundaryHeader = HttpHeaderValues.BOUNDARY.toString();
            if (headerContentType[1].regionMatches(true, 0, boundaryHeader, 0, boundaryHeader.length())) {
                mrank = 1;
                crank = 2;
            } else if (headerContentType[2].regionMatches(true, 0, boundaryHeader, 0, boundaryHeader.length())) {
                mrank = 2;
                crank = 1;
            } else {
                return null;
            }
            String boundary = StringUtil.substringAfter(headerContentType[mrank], '=');
            if (boundary == null) {
                throw new ErrorDataDecoderException("Needs a boundary value");
            }
            if (boundary.charAt(0) == '"') {
                String bound = boundary.trim();
                int index = bound.length() - 1;
                if (bound.charAt(index) == '"') {
                    boundary = bound.substring(1, index);
                }
            }
            final String charsetHeader = HttpHeaderValues.CHARSET.toString();
            if (headerContentType[crank].regionMatches(true, 0, charsetHeader, 0, charsetHeader.length())) {
                String charset = StringUtil.substringAfter(headerContentType[crank], '=');
                if (charset != null) {
                    return new String[] {"--" + boundary, charset};
                }
            }
            return new String[] {"--" + boundary};
        }
        return null;
    }

    @Override
    public boolean isMultipart() {
        return decoder.isMultipart();
    }

    @Override
    public void setDiscardThreshold(int discardThreshold) {
        decoder.setDiscardThreshold(discardThreshold);
    }

    @Override
    public int getDiscardThreshold() {
        return decoder.getDiscardThreshold();
    }

    @Override
    public List<InterfaceHttpData> getBodyHttpDatas() {
        return decoder.getBodyHttpDatas();
    }

    @Override
    public List<InterfaceHttpData> getBodyHttpDatas(String name) {
        return decoder.getBodyHttpDatas(name);
    }

    @Override
    public InterfaceHttpData getBodyHttpData(String name) {
        return decoder.getBodyHttpData(name);
    }

    @Override
    public InterfaceHttpPostRequestDecoder offer(HttpContent content) {
        return decoder.offer(content);
    }

    @Override
    public boolean hasNext() {
        return decoder.hasNext();
    }

    @Override
    public InterfaceHttpData next() {
        return decoder.next();
    }

    @Override
    public InterfaceHttpData currentPartialHttpData() {
        return decoder.currentPartialHttpData();
    }

    @Override
    public void destroy() {
        decoder.destroy();
    }

    @Override
    public void cleanFiles() {
        decoder.cleanFiles();
    }

    @Override
    public void removeHttpDataFromClean(InterfaceHttpData data) {
        decoder.removeHttpDataFromClean(data);
    }

    /** 将 Content-Type 首行拆为 [主类型, 参数1, 参数2] 三段。 */

    private static String[] splitHeaderContentType(String sb) {
        int aStart;
        int aEnd;
        int bStart;
        int bEnd;
        int cStart;
        int cEnd;
        aStart = HttpPostBodyUtil.findNonWhitespace(sb, 0);
        aEnd =  sb.indexOf(';');
        if (aEnd == -1) {
            return new String[] { sb, "", "" };
        }
        bStart = HttpPostBodyUtil.findNonWhitespace(sb, aEnd + 1);
        if (sb.charAt(aEnd - 1) == ' ') {
            aEnd--;
        }
        bEnd =  sb.indexOf(';', bStart);
        if (bEnd == -1) {
            bEnd = HttpPostBodyUtil.findEndOfString(sb);
            return new String[] { sb.substring(aStart, aEnd), sb.substring(bStart, bEnd), "" };
        }
        cStart = HttpPostBodyUtil.findNonWhitespace(sb, bEnd + 1);
        if (sb.charAt(bEnd - 1) == ' ') {
            bEnd--;
        }
        cEnd = HttpPostBodyUtil.findEndOfString(sb);
        return new String[] { sb.substring(aStart, aEnd), sb.substring(bStart, bEnd), sb.substring(cStart, cEnd) };
    }

    /** 分块解码数据不足，需继续 {@link #offer} 更多块。 */

    public static class NotEnoughDataDecoderException extends DecoderException {
        private static final long serialVersionUID = -7846841864603865638L;

        public NotEnoughDataDecoderException() {
        }

        public NotEnoughDataDecoderException(String msg) {
            super(msg);
        }

        public NotEnoughDataDecoderException(Throwable cause) {
            super(cause);
        }

        public NotEnoughDataDecoderException(String msg, Throwable cause) {
            super(msg, cause);
        }
    }

    /** 正文已完全解码，无更多 {@link InterfaceHttpData} 可读。 */

    public static class EndOfDataDecoderException extends DecoderException {
        private static final long serialVersionUID = 1336267941020800769L;
    }

    /** 解码过程发生错误（字符集、格式等）。 */

    public static class ErrorDataDecoderException extends DecoderException {
        private static final long serialVersionUID = 5020247425493164465L;

        public ErrorDataDecoderException() {
        }

        public ErrorDataDecoderException(String msg) {
            super(msg);
        }

        public ErrorDataDecoderException(Throwable cause) {
            super(cause);
        }

        public ErrorDataDecoderException(String msg, Throwable cause) {
            super(msg, cause);
        }
    }

    /** 表单字段数超过 {@code maxFields} 上限。 */

    public static final class TooManyFormFieldsException extends DecoderException {
        private static final long serialVersionUID = 1336267941020800769L;
    }

    /** 单字段缓冲超过 {@code maxBufferedBytes} 上限。 */

    public static final class TooLongFormFieldException extends DecoderException {
        private static final long serialVersionUID = 1336267941020800769L;
    }
}
