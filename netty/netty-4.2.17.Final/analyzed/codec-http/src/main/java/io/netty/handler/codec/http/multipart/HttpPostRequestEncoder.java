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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.EmptyHttpHeaders;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpConstants;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.stream.ChunkedInput;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.StringUtil;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

import static io.netty.buffer.Unpooled.wrappedBuffer;
import static io.netty.util.internal.ObjectUtil.checkNotNull;

/**
 * 将表单数据编码为 POST（或 PUT/OPTIONS 等）请求体的 {@link ChunkedInput}。
 * <p>
 * 支持 {@code application/x-www-form-urlencoded} 与 {@code multipart/form-data}；
 * RFC 7231 允许 POST/PUT/OPTIONS 带 body，TRACE 禁止带 body。
 */
public class HttpPostRequestEncoder implements ChunkedInput<HttpContent> {

    /** URL 编码模式（影响空格与特殊字符转义规则）。 */

    public enum EncoderMode {
        /** 传统 RFC1738 模式；OAuth 场景请用 {@link EncoderMode#RFC3986}。 */

        RFC1738,

        /** RFC3986 模式，OAuth 等场景使用。 */

        RFC3986,

        /** HTML5 模式：同名多文件不使用 mixed，各自独立 part。 */

        HTML5
    }

    private static final String ASTERISK = "*";
    private static final String PLUS = "+";
    private static final String TILDE = "~";
    private static final String ASTERISK_REPLACEMENT = "%2A";
    private static final String PLUS_REPLACEMENT = "%20";
    private static final String TILDE_REPLACEMENT = "%7E";

    /** 创建 {@link InterfaceHttpData} 的工厂。 */

    private final HttpDataFactory factory;

    /** 待编码的 HTTP 请求（头会被改写 Content-Type 等）。 */

    private final HttpRequest request;

    /** 默认字符集。 */

    private final Charset charset;

    /** 是否以分块方式输出，默认 {@code false}。 */

    private boolean isChunked;

    /** 用户添加的原始正文数据项（未编码）。 */

    private final List<InterfaceHttpData> bodyListDatas;
    /** 含 boundary、头字段等编码后的完整 multipart 序列。 */

    final List<InterfaceHttpData> multipartHttpDatas;

    /** 是否 multipart 编码模式。 */

    private final boolean isMultipart;

    /** 外层 multipart boundary 字符串。 */

    String multipartDataBoundary;

    /** 内层 multipart/mixed boundary（仅一层）。 */

    String multipartMixedBoundary;
    /** 请求头是否已最终确定（Content-Length / chunked 等）。 */

    private boolean headerFinalized;

    private final EncoderMode encoderMode;

    /**
     *
     * @param request
     *            the request to encode
     * @param multipart
     *            True if the FORM is a ENCTYPE="multipart/form-data"
     * @throws NullPointerException
     *             for request
     * @throws ErrorDataEncoderException
     *             if the request is a TRACE
     */
    public HttpPostRequestEncoder(HttpRequest request, boolean multipart) throws ErrorDataEncoderException {
        this(new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE), request, multipart,
                HttpConstants.DEFAULT_CHARSET, EncoderMode.RFC1738);
    }

    /**
     *
     * @param factory
     *            the factory used to create InterfaceHttpData
     * @param request
     *            the request to encode
     * @param multipart
     *            True if the FORM is a ENCTYPE="multipart/form-data"
     * @throws NullPointerException
     *             for request and factory
     * @throws ErrorDataEncoderException
     *             if the request is a TRACE
     */
    public HttpPostRequestEncoder(HttpDataFactory factory, HttpRequest request, boolean multipart)
            throws ErrorDataEncoderException {
        this(factory, request, multipart, HttpConstants.DEFAULT_CHARSET, EncoderMode.RFC1738);
    }

    /**
     *
     * @param factory
     *            the factory used to create InterfaceHttpData
     * @param request
     *            the request to encode
     * @param multipart
     *            True if the FORM is a ENCTYPE="multipart/form-data"
     * @param charset
     *            the charset to use as default
     * @param encoderMode
     *            the mode for the encoder to use. See {@link EncoderMode} for the details.
     * @throws NullPointerException
     *             for request or charset or factory
     * @throws ErrorDataEncoderException
     *             if the request is a TRACE
     */
    public HttpPostRequestEncoder(
            HttpDataFactory factory, HttpRequest request, boolean multipart, Charset charset,
            EncoderMode encoderMode)
            throws ErrorDataEncoderException {
        this.request = checkNotNull(request, "request");
        this.charset = checkNotNull(charset, "charset");
        this.factory = checkNotNull(factory, "factory");
        if (HttpMethod.TRACE.equals(request.method())) {
            throw new ErrorDataEncoderException("Cannot create a Encoder if request is a TRACE");
        }
        // 初始化 multipart boundary 与 body 列表
        bodyListDatas = new ArrayList<InterfaceHttpData>();
        // 默认 RFC1738 编码模式
        isLastChunk = false;
        isLastChunkSent = false;
        isMultipart = multipart;
        multipartHttpDatas = new ArrayList<InterfaceHttpData>();
        this.encoderMode = encoderMode;
        if (isMultipart) {
            initDataMultipart();
        }
    }

    /** 清理当前请求关联的磁盘临时文件。 */

    public void cleanFiles() {
        factory.cleanRequestHttpData(request);
    }

    /**
     * Does the last non empty chunk already encoded so that next chunk will be empty (last chunk)
     */
    private boolean isLastChunk;
    /**
     * Last chunk already sent
     */
    private boolean isLastChunkSent;
    /**
     * The current FileUpload that is currently in encode process
     */
    private FileUpload currentFileUpload;
    /**
     * While adding a FileUpload, is the multipart currently in Mixed Mode
     */
    private boolean duringMixedMode;
    /**
     * Global Body size
     */
    private long globalBodySize;
    /**
     * Global Transfer progress
     */
    private long globalProgress;

    /** 是否为 multipart 编码。 */

    public boolean isMultipart() {
        return isMultipart;
    }

    /** 初始化外层 multipart boundary。 */

    private void initDataMultipart() {
        multipartDataBoundary = getNewMultipartDelimiter();
    }

    /** 初始化内层 multipart/mixed boundary。 */

    private void initMixedMultipart() {
        multipartMixedBoundary = getNewMultipartDelimiter();
    }

    /** 生成随机 hex boundary 字符串（DATA 或 MIXED）。 */

    private static String getNewMultipartDelimiter() {
        // 生成随机 boundary 分隔符
        return Long.toHexString(ThreadLocalRandom.current().nextLong());
    }

    /** 返回已添加的正文 {@link InterfaceHttpData} 列表。 */

    public List<InterfaceHttpData> getBodyListAttributes() {
        return bodyListDatas;
    }

    /** 批量设置正文数据项（会清空原有列表）。 */

    public void setBodyHttpDatas(List<InterfaceHttpData> datas) throws ErrorDataEncoderException {
        ObjectUtil.checkNotNull(datas, "datas");
        globalBodySize = 0;
        bodyListDatas.clear();
        currentFileUpload = null;
        duringMixedMode = false;
        multipartHttpDatas.clear();
        for (InterfaceHttpData data : datas) {
            addBodyHttpData(data);
        }
    }

    /** 添加 name=value 表单属性。 */

    public void addBodyAttribute(String name, String value) throws ErrorDataEncoderException {
        String svalue = value != null? value : StringUtil.EMPTY_STRING;
        Attribute data = factory.createAttribute(request, checkNotNull(name, "name"), svalue);
        addBodyHttpData(data);
    }

    /** 添加磁盘 {@link File} 作为文件上传 part。 */

    public void addBodyFileUpload(String name, File file, String contentType, boolean isText)
            throws ErrorDataEncoderException {
        addBodyFileUpload(name, file.getName(), file, contentType, isText);
    }

    /** 指定客户端文件名添加文件上传 part。 */

    public void addBodyFileUpload(String name, String filename, File file, String contentType, boolean isText)
            throws ErrorDataEncoderException {
        checkNotNull(name, "name");
        checkNotNull(file, "file");
        if (filename == null) {
            filename = StringUtil.EMPTY_STRING;
        }
        String scontentType = contentType;
        String contentTransferEncoding = null;
        if (contentType == null) {
            if (isText) {
                scontentType = HttpPostBodyUtil.DEFAULT_TEXT_CONTENT_TYPE;
            } else {
                scontentType = HttpPostBodyUtil.DEFAULT_BINARY_CONTENT_TYPE;
            }
        }
        if (!isText) {
            contentTransferEncoding = HttpPostBodyUtil.TransferEncodingMechanism.BINARY.value();
        }
        FileUpload fileUpload = factory.createFileUpload(request, name, filename, scontentType,
                contentTransferEncoding, null, file.length());
        try {
            fileUpload.setContent(file);
        } catch (IOException e) {
            throw new ErrorDataEncoderException(e);
        }
        addBodyHttpData(fileUpload);
    }

    /** 批量添加同名多文件上传。 */

    public void addBodyFileUploads(String name, File[] file, String[] contentType, boolean[] isText)
            throws ErrorDataEncoderException {
        if (file.length != contentType.length && file.length != isText.length) {
            throw new IllegalArgumentException("Different array length");
        }
        for (int i = 0; i < file.length; i++) {
            addBodyFileUpload(name, file[i], contentType[i], isText[i]);
        }
    }

    /** 添加任意 {@link InterfaceHttpData} 并更新 Content-Length 估算。 */

    public void addBodyHttpData(InterfaceHttpData data) throws ErrorDataEncoderException {
        if (headerFinalized) {
            throw new ErrorDataEncoderException("Cannot add value once finalized");
        }
        bodyListDatas.add(checkNotNull(data, "data"));
        if (!isMultipart) {
            if (data instanceof Attribute) {
                Attribute attribute = (Attribute) data;
                try {
                    // urlencoded: 编码后的 name=value&
                    String key = encodeAttribute(attribute.getName(), charset);
                    String value = encodeAttribute(attribute.getValue(), charset);
                    Attribute newattribute = factory.createAttribute(request, key, value);
                    multipartHttpDatas.add(newattribute);
                    globalBodySize += newattribute.getName().length() + 1 + newattribute.length() + 1;
                } catch (IOException e) {
                    throw new ErrorDataEncoderException(e);
                }
            } else if (data instanceof FileUpload) {
                // 非 multipart 时文件名作为 Attribute 值
                FileUpload fileUpload = (FileUpload) data;
                // urlencoded 形式 name=filename&
                String key = encodeAttribute(fileUpload.getName(), charset);
                String value = encodeAttribute(fileUpload.getFilename(), charset);
                Attribute newattribute = factory.createAttribute(request, key, value);
                multipartHttpDatas.add(newattribute);
                globalBodySize += newattribute.getName().length() + 1 + newattribute.length() + 1;
            }
            return;
        }
        /*
         * Logic:
         * if not Attribute:
         *      add Data to body list
         *      if (duringMixedMode)
         *          add endmixedmultipart delimiter
         *          currentFileUpload = null
         *          duringMixedMode = false;
         *      add multipart delimiter, multipart body header and Data to multipart list
         *      reset currentFileUpload, duringMixedMode
         * if FileUpload: take care of multiple file for one field => mixed mode
         *      if (duringMixedMode)
         *          if (currentFileUpload.name == data.name)
         *              add mixedmultipart delimiter, mixedmultipart body header and Data to multipart list
         *          else
         *              add endmixedmultipart delimiter, multipart body header and Data to multipart list
         *              currentFileUpload = data
         *              duringMixedMode = false;
         *      else
         *          if (currentFileUpload.name == data.name)
         *              change multipart body header of previous file into multipart list to
         *                      mixedmultipart start, mixedmultipart body header
         *              add mixedmultipart delimiter, mixedmultipart body header and Data to multipart list
         *              duringMixedMode = true
         *          else
         *              add multipart delimiter, multipart body header and Data to multipart list
         *              currentFileUpload = data
         *              duringMixedMode = false;
         * Do not add last delimiter! Could be:
         * if duringmixedmode: endmixedmultipart + endmultipart
         * else only endmultipart
         */
        if (data instanceof Attribute) {
            if (duringMixedMode) {
                InternalAttribute internal = new InternalAttribute(charset);
                internal.addValue("\r\n--" + multipartMixedBoundary + "--");
                multipartHttpDatas.add(internal);
                multipartMixedBoundary = null;
                currentFileUpload = null;
                duringMixedMode = false;
            }
            InternalAttribute internal = new InternalAttribute(charset);
            if (!multipartHttpDatas.isEmpty()) {
                // 上一 part 为字段，先输出 CRLF
                internal.addValue("\r\n");
            }
            internal.addValue("--" + multipartDataBoundary + "\r\n");
            // content-disposition: form-data; name="field1"
            Attribute attribute = (Attribute) data;
            internal.addValue(HttpHeaderNames.CONTENT_DISPOSITION + ": " + HttpHeaderValues.FORM_DATA + "; "
                    + HttpHeaderValues.NAME + "=\"" + attribute.getName() + "\"\r\n");
            // Add Content-Length: xxx
            internal.addValue(HttpHeaderNames.CONTENT_LENGTH + ": " +
                    attribute.length() + "\r\n");
            Charset localcharset = attribute.getCharset();
            if (localcharset != null) {
                // Content-Type: text/plain; charset=charset
                internal.addValue(HttpHeaderNames.CONTENT_TYPE + ": " +
                        HttpPostBodyUtil.DEFAULT_TEXT_CONTENT_TYPE + "; " +
                        HttpHeaderValues.CHARSET + '='
                        + localcharset.name() + "\r\n");
            }
            // CRLF between body header and data
            internal.addValue("\r\n");
            multipartHttpDatas.add(internal);
            multipartHttpDatas.add(data);
            globalBodySize += attribute.length() + internal.size();
        } else if (data instanceof FileUpload) {
            FileUpload fileUpload = (FileUpload) data;
            InternalAttribute internal = new InternalAttribute(charset);
            if (!multipartHttpDatas.isEmpty()) {
                // previously a data field so CRLF
                internal.addValue("\r\n");
            }
            boolean localMixed;
            if (duringMixedMode) {
                if (currentFileUpload != null && currentFileUpload.getName().equals(fileUpload.getName())) {
                    // continue a mixed mode

                    localMixed = true;
                } else {
                    // end a mixed mode

                    // add endmixedmultipart delimiter, multipart body header
                    // and
                    // Data to multipart list
                    internal.addValue("--" + multipartMixedBoundary + "--");
                    multipartHttpDatas.add(internal);
                    multipartMixedBoundary = null;
                    // start a new one (could be replaced if mixed start again
                    // from here
                    internal = new InternalAttribute(charset);
                    internal.addValue("\r\n");
                    localMixed = false;
                    // new currentFileUpload and no more in Mixed mode
                    currentFileUpload = fileUpload;
                    duringMixedMode = false;
                }
            } else {
                if (encoderMode != EncoderMode.HTML5 && currentFileUpload != null
                        && currentFileUpload.getName().equals(fileUpload.getName())) {
                    // create a new mixed mode (from previous file)

                    // change multipart body header of previous file into
                    // multipart list to
                    // mixedmultipart start, mixedmultipart body header

                    // change Internal (size()-2 position in multipartHttpDatas)
                    // from (line starting with *)
                    // --AaB03x
                    // * Content-Disposition: form-data; name="files";
                    // filename="file1.txt"
                    // Content-Type: text/plain
                    // to (lines starting with *)
                    // --AaB03x
                    // * Content-Disposition: form-data; name="files"
                    // * Content-Type: multipart/mixed; boundary=BbC04y
                    // *
                    // * --BbC04y
                    // * Content-Disposition: attachment; filename="file1.txt"
                    // Content-Type: text/plain
                    initMixedMultipart();
                    InternalAttribute pastAttribute = (InternalAttribute) multipartHttpDatas.get(multipartHttpDatas
                            .size() - 2);
                    // remove past size
                    globalBodySize -= pastAttribute.size();
                    StringBuilder replacement = new StringBuilder(
                            139 + multipartDataBoundary.length() + multipartMixedBoundary.length() * 2 +
                                    fileUpload.getFilename().length() + fileUpload.getName().length())

                        .append("--")
                        .append(multipartDataBoundary)
                        .append("\r\n")

                        .append(HttpHeaderNames.CONTENT_DISPOSITION)
                        .append(": ")
                        .append(HttpHeaderValues.FORM_DATA)
                        .append("; ")
                        .append(HttpHeaderValues.NAME)
                        .append("=\"")
                        .append(fileUpload.getName())
                        .append("\"\r\n")

                        .append(HttpHeaderNames.CONTENT_TYPE)
                        .append(": ")
                        .append(HttpHeaderValues.MULTIPART_MIXED)
                        .append("; ")
                        .append(HttpHeaderValues.BOUNDARY)
                        .append('=')
                        .append(multipartMixedBoundary)
                        .append("\r\n\r\n")

                        .append("--")
                        .append(multipartMixedBoundary)
                        .append("\r\n")

                        .append(HttpHeaderNames.CONTENT_DISPOSITION)
                        .append(": ")
                        .append(HttpHeaderValues.ATTACHMENT);

                    if (!fileUpload.getFilename().isEmpty()) {
                        replacement.append("; ")
                                   .append(HttpHeaderValues.FILENAME)
                                   .append("=\"")
                                   .append(currentFileUpload.getFilename())
                                   .append('"');
                    }

                    replacement.append("\r\n");

                    pastAttribute.setValue(replacement.toString(), 1);
                    pastAttribute.setValue("", 2);

                    // update past size
                    globalBodySize += pastAttribute.size();

                    // now continue
                    // add mixedmultipart delimiter, mixedmultipart body header
                    // and
                    // Data to multipart list
                    localMixed = true;
                    duringMixedMode = true;
                } else {
                    // a simple new multipart
                    // add multipart delimiter, multipart body header and Data
                    // to multipart list
                    localMixed = false;
                    currentFileUpload = fileUpload;
                    duringMixedMode = false;
                }
            }

            if (localMixed) {
                // add mixedmultipart delimiter, mixedmultipart body header and
                // Data to multipart list
                internal.addValue("--" + multipartMixedBoundary + "\r\n");

                if (fileUpload.getFilename().isEmpty()) {
                    // Content-Disposition: attachment
                    internal.addValue(HttpHeaderNames.CONTENT_DISPOSITION + ": "
                            + HttpHeaderValues.ATTACHMENT + "\r\n");
                } else {
                    // Content-Disposition: attachment; filename="file1.txt"
                    internal.addValue(HttpHeaderNames.CONTENT_DISPOSITION + ": "
                            + HttpHeaderValues.ATTACHMENT + "; "
                            + HttpHeaderValues.FILENAME + "=\"" + fileUpload.getFilename() + "\"\r\n");
                }
            } else {
                internal.addValue("--" + multipartDataBoundary + "\r\n");

                if (fileUpload.getFilename().isEmpty()) {
                    // Content-Disposition: form-data; name="files";
                    internal.addValue(HttpHeaderNames.CONTENT_DISPOSITION + ": " + HttpHeaderValues.FORM_DATA + "; "
                            + HttpHeaderValues.NAME + "=\"" + fileUpload.getName() + "\"\r\n");
                } else {
                    // Content-Disposition: form-data; name="files";
                    // filename="file1.txt"
                    internal.addValue(HttpHeaderNames.CONTENT_DISPOSITION + ": " + HttpHeaderValues.FORM_DATA + "; "
                            + HttpHeaderValues.NAME + "=\"" + fileUpload.getName() + "\"; "
                            + HttpHeaderValues.FILENAME + "=\"" + fileUpload.getFilename() + "\"\r\n");
                }
            }
            // Add Content-Length: xxx
            internal.addValue(HttpHeaderNames.CONTENT_LENGTH + ": " +
                    fileUpload.length() + "\r\n");
            // Content-Type: image/gif
            // Content-Type: text/plain; charset=ISO-8859-1
            // Content-Transfer-Encoding: binary
            internal.addValue(HttpHeaderNames.CONTENT_TYPE + ": " + fileUpload.getContentType());
            String contentTransferEncoding = fileUpload.getContentTransferEncoding();
            if (contentTransferEncoding != null
                    && contentTransferEncoding.equals(HttpPostBodyUtil.TransferEncodingMechanism.BINARY.value())) {
                internal.addValue("\r\n" + HttpHeaderNames.CONTENT_TRANSFER_ENCODING + ": "
                        + HttpPostBodyUtil.TransferEncodingMechanism.BINARY.value() + "\r\n\r\n");
            } else if (fileUpload.getCharset() != null) {
                internal.addValue("; " + HttpHeaderValues.CHARSET + '=' + fileUpload.getCharset().name() + "\r\n\r\n");
            } else {
                internal.addValue("\r\n\r\n");
            }
            multipartHttpDatas.add(internal);
            multipartHttpDatas.add(data);
            globalBodySize += fileUpload.length() + internal.size();
        }
    }

    /**
     * Iterator to be used when encoding will be called chunk after chunk
     */
    private ListIterator<InterfaceHttpData> iterator;

    /**
     * Finalize the request by preparing the Header in the request and returns the request ready to be sent.<br>
     * Once finalized, no data must be added.<br>
     * If the request does not need chunk (isChunked() == false), this request is the only object to send to the remote
     * server.
     *
     * @return the request object (chunked or not according to size of body)
     * @throws ErrorDataEncoderException
     *             if the encoding is in error or if the finalize were already done
     */
    public HttpRequest finalizeRequest() throws ErrorDataEncoderException {
        // Finalize the multipartHttpDatas
        if (!headerFinalized) {
            if (isMultipart) {
                InternalAttribute internal = new InternalAttribute(charset);
                if (duringMixedMode) {
                    internal.addValue("\r\n--" + multipartMixedBoundary + "--");
                }
                internal.addValue("\r\n--" + multipartDataBoundary + "--\r\n");
                multipartHttpDatas.add(internal);
                multipartMixedBoundary = null;
                currentFileUpload = null;
                duringMixedMode = false;
                globalBodySize += internal.size();
            }
            headerFinalized = true;
        } else {
            throw new ErrorDataEncoderException("Header already encoded");
        }

        HttpHeaders headers = request.headers();
        List<String> contentTypes = headers.getAll(HttpHeaderNames.CONTENT_TYPE);
        List<String> transferEncoding = headers.getAll(HttpHeaderNames.TRANSFER_ENCODING);
        if (contentTypes != null) {
            headers.remove(HttpHeaderNames.CONTENT_TYPE);
            for (String contentType : contentTypes) {
                // "multipart/form-data; boundary=--89421926422648"
                // toLowerCase() without a Locale would corrupt the comparison under Turkish
                // (tr_TR) locale, where 'I' -> 'ı' (U+0131): a request that sets
                // Content-Type: MULTIPART/form-data would lowercase to "multıpart/form-data" and
                // miss the prefix check, leaving the original header in place alongside the
                // multipart one this encoder is about to add.
                String lowercased = contentType.toLowerCase(Locale.US);
                if (lowercased.startsWith(HttpHeaderValues.MULTIPART_FORM_DATA.toString()) ||
                        lowercased.startsWith(HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED.toString())) {
                    // ignore
                } else {
                    headers.add(HttpHeaderNames.CONTENT_TYPE, contentType);
                }
            }
        }
        if (isMultipart) {
            String value = HttpHeaderValues.MULTIPART_FORM_DATA + "; " + HttpHeaderValues.BOUNDARY + '='
                    + multipartDataBoundary;
            headers.add(HttpHeaderNames.CONTENT_TYPE, value);
        } else {
            // Not multipart
            headers.add(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED);
        }
        // Now consider size for chunk or not
        long realSize = globalBodySize;
        if (!isMultipart) {
            realSize -= 1; // last '&' removed
        }
        iterator = multipartHttpDatas.listIterator();

        headers.set(HttpHeaderNames.CONTENT_LENGTH, String.valueOf(realSize));
        if (realSize > HttpPostBodyUtil.chunkSize || isMultipart) {
            isChunked = true;
            if (transferEncoding != null) {
                headers.remove(HttpHeaderNames.TRANSFER_ENCODING);
                for (CharSequence v : transferEncoding) {
                    if (HttpHeaderValues.CHUNKED.contentEqualsIgnoreCase(v)) {
                        // ignore
                    } else {
                        headers.add(HttpHeaderNames.TRANSFER_ENCODING, v);
                    }
                }
            }
            HttpUtil.setTransferEncodingChunked(request, true);

            // wrap to hide the possible content
            return new WrappedHttpRequest(request);
        } else {
            // get the only one body and set it to the request
            HttpContent chunk = nextChunk();
            if (request instanceof FullHttpRequest) {
                FullHttpRequest fullRequest = (FullHttpRequest) request;
                ByteBuf chunkContent = chunk.content();
                if (fullRequest.content() != chunkContent) {
                    fullRequest.content().clear().writeBytes(chunkContent);
                    chunkContent.release();
                }
                return fullRequest;
            } else {
                return new WrappedFullHttpRequest(request, chunk);
            }
        }
    }

    /**
     * @return True if the request is by Chunk
     */
    public boolean isChunked() {
        return isChunked;
    }

    /**
     * Encode one attribute
     *
     * @return the encoded attribute
     * @throws ErrorDataEncoderException
     *             if the encoding is in error
     */
    private String encodeAttribute(String s, Charset charset) throws ErrorDataEncoderException {
        if (s == null) {
            return "";
        }
        try {
            String encoded = URLEncoder.encode(s, charset.name());
            if (encoderMode == EncoderMode.RFC3986) {
                encoded = encoded.replace(ASTERISK, ASTERISK_REPLACEMENT)
                                 .replace(PLUS, PLUS_REPLACEMENT)
                                 .replace(TILDE, TILDE_REPLACEMENT);
            }
            return encoded;
        } catch (UnsupportedEncodingException e) {
            throw new ErrorDataEncoderException(charset.name(), e);
        }
    }

    /**
     * The ByteBuf currently used by the encoder
     */
    private ByteBuf currentBuffer;
    /**
     * The current InterfaceHttpData to encode (used if more chunks are available)
     */
    private InterfaceHttpData currentData;
    /**
     * If not multipart, does the currentBuffer stands for the Key or for the Value
     */
    private boolean isKey = true;

    /**
     *
     * @return the next ByteBuf to send as an HttpChunk and modifying currentBuffer accordingly
     */
    private ByteBuf fillByteBuf() {
        int length = currentBuffer.readableBytes();
        if (length > HttpPostBodyUtil.chunkSize) {
            return currentBuffer.readRetainedSlice(HttpPostBodyUtil.chunkSize);
        } else {
            // to continue
            ByteBuf slice = currentBuffer;
            currentBuffer = null;
            return slice;
        }
    }

    /**
     * From the current context (currentBuffer and currentData), returns the next HttpChunk (if possible) trying to get
     * sizeleft bytes more into the currentBuffer. This is the Multipart version.
     *
     * @param sizeleft
     *            the number of bytes to try to get from currentData
     * @return the next HttpChunk or null if not enough bytes were found
     * @throws ErrorDataEncoderException
     *             if the encoding is in error
     */
    private HttpContent encodeNextChunkMultipart(int sizeleft) throws ErrorDataEncoderException {
        if (currentData == null) {
            return null;
        }
        ByteBuf buffer;
        if (currentData instanceof InternalAttribute) {
            buffer = ((InternalAttribute) currentData).toByteBuf();
            currentData = null;
        } else {
            try {
                buffer = ((HttpData) currentData).getChunk(sizeleft);
            } catch (IOException e) {
                throw new ErrorDataEncoderException(e);
            }
            if (buffer.capacity() == 0) {
                // end for current InterfaceHttpData, need more data
                currentData = null;
                return null;
            }
        }
        if (currentBuffer == null) {
            currentBuffer = buffer;
        } else {
            currentBuffer = wrappedBuffer(currentBuffer, buffer);
        }
        if (currentBuffer.readableBytes() < HttpPostBodyUtil.chunkSize) {
            currentData = null;
            return null;
        }
        buffer = fillByteBuf();
        return new DefaultHttpContent(buffer);
    }

    /**
     * From the current context (currentBuffer and currentData), returns the next HttpChunk (if possible) trying to get
     * sizeleft bytes more into the currentBuffer. This is the UrlEncoded version.
     *
     * @param sizeleft
     *            the number of bytes to try to get from currentData
     * @return the next HttpChunk or null if not enough bytes were found
     * @throws ErrorDataEncoderException
     *             if the encoding is in error
     */
    private HttpContent encodeNextChunkUrlEncoded(int sizeleft) throws ErrorDataEncoderException {
        if (currentData == null) {
            return null;
        }
        int size = sizeleft;
        ByteBuf buffer;

        // Set name=
        if (isKey) {
            String key = currentData.getName();
            buffer = wrappedBuffer(key.getBytes(charset));
            isKey = false;
            if (currentBuffer == null) {
                currentBuffer = wrappedBuffer(buffer, wrappedBuffer("=".getBytes(charset)));
            } else {
                currentBuffer = wrappedBuffer(currentBuffer, buffer, wrappedBuffer("=".getBytes(charset)));
            }
            // continue
            size -= buffer.readableBytes() + 1;
            if (currentBuffer.readableBytes() >= HttpPostBodyUtil.chunkSize) {
                buffer = fillByteBuf();
                return new DefaultHttpContent(buffer);
            }
        }

        // Put value into buffer
        try {
            buffer = ((HttpData) currentData).getChunk(size);
        } catch (IOException e) {
            throw new ErrorDataEncoderException(e);
        }

        // Figure out delimiter
        ByteBuf delimiter = null;
        if (buffer.readableBytes() < size) {
            isKey = true;
            currentData = null;
            delimiter = iterator.hasNext() ? wrappedBuffer("&".getBytes(charset)) : null;
        }

        // End for current InterfaceHttpData, need potentially more data
        if (buffer.capacity() == 0) {
            isKey = true;
            currentData = null;
            if (currentBuffer == null) {
                if (delimiter == null) {
                    return null;
                } else {
                    currentBuffer = delimiter;
                }
            } else {
                if (delimiter != null) {
                    currentBuffer = wrappedBuffer(currentBuffer, delimiter);
                }
            }
            if (currentBuffer.readableBytes() >= HttpPostBodyUtil.chunkSize) {
                buffer = fillByteBuf();
                return new DefaultHttpContent(buffer);
            }
            return null;
        }

        // Put it all together: name=value&
        if (currentBuffer == null) {
            if (delimiter != null) {
                currentBuffer = wrappedBuffer(buffer, delimiter);
            } else {
                currentBuffer = buffer;
            }
        } else {
            if (delimiter != null) {
                currentBuffer = wrappedBuffer(currentBuffer, buffer, delimiter);
            } else {
                currentBuffer = wrappedBuffer(currentBuffer, buffer);
            }
        }

        if (currentBuffer.readableBytes() >= HttpPostBodyUtil.chunkSize) {
            return new DefaultHttpContent(fillByteBuf());
        }
        return null;
    }

    @Override
    public void close() throws Exception {
        // NO since the user can want to reuse (broadcast for instance)
        // cleanFiles();
    }

    @Deprecated
    @Override
    public HttpContent readChunk(ChannelHandlerContext ctx) throws Exception {
        return readChunk(ctx.alloc());
    }

    /**
     * Returns the next available HttpChunk. The caller is responsible to test if this chunk is the last one (isLast()),
     * in order to stop calling this getMethod.
     *
     * @return the next available HttpChunk
     * @throws ErrorDataEncoderException
     *             if the encoding is in error
     */
    @Override
    public HttpContent readChunk(ByteBufAllocator allocator) throws Exception {
        if (isLastChunkSent) {
            return null;
        } else {
            HttpContent nextChunk = nextChunk();
            globalProgress += nextChunk.content().readableBytes();
            return nextChunk;
        }
    }

    /**
     * Returns the next available HttpChunk. The caller is responsible to test if this chunk is the last one (isLast()),
     * in order to stop calling this getMethod.
     *
     * @return the next available HttpChunk
     * @throws ErrorDataEncoderException
     *             if the encoding is in error
     */
    private HttpContent nextChunk() throws ErrorDataEncoderException {
        if (isLastChunk) {
            isLastChunkSent = true;
            return LastHttpContent.EMPTY_LAST_CONTENT;
        }
        // first test if previous buffer is not empty
        int size = calculateRemainingSize();
        if (size <= 0) {
            // NextChunk from buffer
            ByteBuf buffer = fillByteBuf();
            return new DefaultHttpContent(buffer);
        }
        // size > 0
        if (currentData != null) {
            // continue to read data
            HttpContent chunk;
            if (isMultipart) {
                chunk = encodeNextChunkMultipart(size);
            } else {
                chunk = encodeNextChunkUrlEncoded(size);
            }
            if (chunk != null) {
                // NextChunk from data
                return chunk;
            }
            size = calculateRemainingSize();
        }
        if (!iterator.hasNext()) {
            return lastChunk();
        }
        while (size > 0 && iterator.hasNext()) {
            currentData = iterator.next();
            HttpContent chunk;
            if (isMultipart) {
                chunk = encodeNextChunkMultipart(size);
            } else {
                chunk = encodeNextChunkUrlEncoded(size);
            }
            if (chunk == null) {
                // not enough
                size = calculateRemainingSize();
                continue;
            }
            // NextChunk from data
            return chunk;
        }
        // end since no more data
        return lastChunk();
    }

    private int calculateRemainingSize() {
        int size = HttpPostBodyUtil.chunkSize;
        if (currentBuffer != null) {
            size -= currentBuffer.readableBytes();
        }
        return size;
    }

    private HttpContent lastChunk() {
        isLastChunk = true;
        if (currentBuffer == null) {
            isLastChunkSent = true;
            // LastChunk with no more data
            return LastHttpContent.EMPTY_LAST_CONTENT;
        }
        // NextChunk as last non empty from buffer
        ByteBuf buffer = currentBuffer;
        currentBuffer = null;
        return new DefaultHttpContent(buffer);
    }

    @Override
    public boolean isEndOfInput() throws Exception {
        return isLastChunkSent;
    }

    @Override
    public long length() {
        return isMultipart? globalBodySize : globalBodySize - 1;
    }

    @Override
    public long progress() {
        return globalProgress;
    }

    /**
     * Exception when an error occurs while encoding
     */
    public static class ErrorDataEncoderException extends Exception {
        private static final long serialVersionUID = 5020247425493164465L;

        public ErrorDataEncoderException() {
        }

        public ErrorDataEncoderException(String msg) {
            super(msg);
        }

        public ErrorDataEncoderException(Throwable cause) {
            super(cause);
        }

        public ErrorDataEncoderException(String msg, Throwable cause) {
            super(msg, cause);
        }
    }

    private static class WrappedHttpRequest implements HttpRequest {
        private final HttpRequest request;
        WrappedHttpRequest(HttpRequest request) {
            this.request = request;
        }

        @Override
        public HttpRequest setProtocolVersion(HttpVersion version) {
            request.setProtocolVersion(version);
            return this;
        }

        @Override
        public HttpRequest setMethod(HttpMethod method) {
            request.setMethod(method);
            return this;
        }

        @Override
        public HttpRequest setUri(String uri) {
            request.setUri(uri);
            return this;
        }

        @Override
        public HttpMethod getMethod() {
            return request.method();
        }

        @Override
        public HttpMethod method() {
            return request.method();
        }

        @Override
        public String getUri() {
            return request.uri();
        }

        @Override
        public String uri() {
            return request.uri();
        }

        @Override
        public HttpVersion getProtocolVersion() {
            return request.protocolVersion();
        }

        @Override
        public HttpVersion protocolVersion() {
            return request.protocolVersion();
        }

        @Override
        public HttpHeaders headers() {
            return request.headers();
        }

        @Override
        public DecoderResult decoderResult() {
            return request.decoderResult();
        }

        @Override
        @Deprecated
        public DecoderResult getDecoderResult() {
            return request.getDecoderResult();
        }

        @Override
        public void setDecoderResult(DecoderResult result) {
            request.setDecoderResult(result);
        }
    }

    private static final class WrappedFullHttpRequest extends WrappedHttpRequest implements FullHttpRequest {
        private final HttpContent content;

        private WrappedFullHttpRequest(HttpRequest request, HttpContent content) {
            super(request);
            this.content = content;
        }

        @Override
        public FullHttpRequest setProtocolVersion(HttpVersion version) {
            super.setProtocolVersion(version);
            return this;
        }

        @Override
        public FullHttpRequest setMethod(HttpMethod method) {
            super.setMethod(method);
            return this;
        }

        @Override
        public FullHttpRequest setUri(String uri) {
            super.setUri(uri);
            return this;
        }

        @Override
        public FullHttpRequest copy() {
            return replace(content().copy());
        }

        @Override
        public FullHttpRequest duplicate() {
            return replace(content().duplicate());
        }

        @Override
        public FullHttpRequest retainedDuplicate() {
            return replace(content().retainedDuplicate());
        }

        @Override
        public FullHttpRequest replace(ByteBuf content) {
            DefaultFullHttpRequest duplicate = new DefaultFullHttpRequest(protocolVersion(), method(), uri(), content);
            duplicate.headers().set(headers());
            duplicate.trailingHeaders().set(trailingHeaders());
            return duplicate;
        }

        @Override
        public FullHttpRequest retain(int increment) {
            content.retain(increment);
            return this;
        }

        @Override
        public FullHttpRequest retain() {
            content.retain();
            return this;
        }

        @Override
        public FullHttpRequest touch() {
            content.touch();
            return this;
        }

        @Override
        public FullHttpRequest touch(Object hint) {
            content.touch(hint);
            return this;
        }

        @Override
        public ByteBuf content() {
            return content.content();
        }

        @Override
        public HttpHeaders trailingHeaders() {
            if (content instanceof LastHttpContent) {
                return ((LastHttpContent) content).trailingHeaders();
            } else {
                return EmptyHttpHeaders.INSTANCE;
            }
        }

        @Override
        public int refCnt() {
            return content.refCnt();
        }

        @Override
        public boolean release() {
            return content.release();
        }

        @Override
        public boolean release(int decrement) {
            return content.release(decrement);
        }
    }
}
