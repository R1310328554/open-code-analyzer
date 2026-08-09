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
import io.netty.channel.ChannelException;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.util.internal.ObjectUtil;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * 内存型 {@link FileUpload} 实现，上传内容全部保存在 {@link ByteBuf} 中。
 * <p>注意内存占用：大文件应选用 {@link DiskFileUpload} 或 {@link MixedFileUpload}。
 */
public class MemoryFileUpload extends AbstractMemoryHttpData implements FileUpload {

    private String filename;

    private String contentType;

    private String contentTransferEncoding;

    /** 构造内存文件上传项，并设置文件名、Content-Type 与传输编码 */
    public MemoryFileUpload(String name, String filename, String contentType,
            String contentTransferEncoding, Charset charset, long size) {
        super(name, charset, size);
        setFilename(filename);
        setContentType(contentType);
        setContentTransferEncoding(contentTransferEncoding);
    }

    @Override
    public HttpDataType getHttpDataType() {
        return HttpDataType.FileUpload;
    }

    @Override
    public String getFilename() {
        return filename;
    }

    @Override
    public void setFilename(String filename) {
        this.filename = FileUploadUtil.validateFileNameForMultiPart(filename);
    }

    @Override
    public int hashCode() {
        return FileUploadUtil.hashCode(this);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof FileUpload && FileUploadUtil.equals(this, (FileUpload) o);
    }

    @Override
    public int compareTo(InterfaceHttpData o) {
        if (!(o instanceof FileUpload)) {
            throw new ClassCastException("Cannot compare " + getHttpDataType() +
                    " with " + o.getHttpDataType());
        }
        return compareTo((FileUpload) o);
    }

    public int compareTo(FileUpload o) {
        return FileUploadUtil.compareTo(this, o);
    }

    @Override
    public void setContentType(String contentType) {
        this.contentType = ObjectUtil.checkNotNull(contentType, "contentType");
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public String getContentTransferEncoding() {
        return contentTransferEncoding;
    }

    @Override
    public void setContentTransferEncoding(String contentTransferEncoding) {
        this.contentTransferEncoding = contentTransferEncoding;
    }

    @Override
    public String toString() {
        return HttpHeaderNames.CONTENT_DISPOSITION + ": " +
               HttpHeaderValues.FORM_DATA + "; " + HttpHeaderValues.NAME + "=\"" + getName() +
            "\"; " + HttpHeaderValues.FILENAME + "=\"" + filename + "\"\r\n" +
            HttpHeaderNames.CONTENT_TYPE + ": " + contentType +
            (getCharset() != null? "; " + HttpHeaderValues.CHARSET + '=' + getCharset().name() + "\r\n" : "\r\n") +
            HttpHeaderNames.CONTENT_LENGTH + ": " + length() + "\r\n" +
            "Completed: " + isCompleted() +
            "\r\nIsInMemory: " + isInMemory();
    }

    /** 深拷贝内容 {@link ByteBuf}，返回新的 {@link MemoryFileUpload} */
    @Override
    public FileUpload copy() {
        final ByteBuf content = content();
        return replace(content != null ? content.copy() : content);
    }

    @Override
    public FileUpload duplicate() {
        final ByteBuf content = content();
        return replace(content != null ? content.duplicate() : content);
    }

    @Override
    public FileUpload retainedDuplicate() {
        ByteBuf content = content();
        if (content != null) {
            content = content.retainedDuplicate();
            boolean success = false;
            try {
                FileUpload duplicate = replace(content);
                success = true;
                return duplicate;
            } finally {
                if (!success) {
                    content.release();
                }
            }
        } else {
            return replace(null);
        }
    }

    /** 以新 {@link ByteBuf} 内容替换，保留元数据字段 */
    @Override
    public FileUpload replace(ByteBuf content) {
        MemoryFileUpload upload = new MemoryFileUpload(
                getName(), getFilename(), getContentType(), getContentTransferEncoding(), getCharset(), size);
        if (content != null) {
            try {
                upload.setContent(content);
            } catch (IOException e) {
                throw new ChannelException(e);
            }
        }
        upload.setCompleted(isCompleted());
        return upload;
    }

    @Override
    public FileUpload retain() {
        super.retain();
        return this;
    }

    @Override
    public FileUpload retain(int increment) {
        super.retain(increment);
        return this;
    }

    @Override
    public FileUpload touch() {
        super.touch();
        return this;
    }

    @Override
    public FileUpload touch(Object hint) {
        super.touch(hint);
        return this;
    }
}
