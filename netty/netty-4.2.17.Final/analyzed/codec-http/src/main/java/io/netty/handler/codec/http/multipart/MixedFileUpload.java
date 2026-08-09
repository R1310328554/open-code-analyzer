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

import java.nio.charset.Charset;

/**
 * 混合存储 {@link FileUpload}：声明大小超过 {@code limitSize} 时直接用磁盘，否则先驻内存。
 * <p>写入过程中超出阈值会由 {@link AbstractMixedHttpData} 迁移到 {@link DiskFileUpload}。
 */
public class MixedFileUpload extends AbstractMixedHttpData<FileUpload> implements FileUpload {

    /** 使用默认临时目录创建混合 FileUpload */
    public MixedFileUpload(String name, String filename, String contentType,
                           String contentTransferEncoding, Charset charset, long size,
                           long limitSize) {
        this(name, filename, contentType, contentTransferEncoding,
             charset, size, limitSize, DiskFileUpload.baseDirectory, DiskFileUpload.deleteOnExitTemporaryFile);
    }

    public MixedFileUpload(String name, String filename, String contentType,
                           String contentTransferEncoding, Charset charset, long size,
                           long limitSize, String baseDir, boolean deleteOnExit) {
        super(limitSize, baseDir, deleteOnExit,
              size > limitSize?
                      // 已知大小已超阈值，直接落盘
                      new DiskFileUpload(name, filename, contentType, contentTransferEncoding, charset, size, baseDir,
                                         deleteOnExit) :
                      // 否则先用内存，写入时再按需切换
                      new MemoryFileUpload(name, filename, contentType, contentTransferEncoding, charset, size)
        );
    }

    @Override
    public String getContentTransferEncoding() {
        return wrapped.getContentTransferEncoding();
    }

    @Override
    public String getFilename() {
        return wrapped.getFilename();
    }

    @Override
    public void setContentTransferEncoding(String contentTransferEncoding) {
        wrapped.setContentTransferEncoding(contentTransferEncoding);
    }

    @Override
    public void setFilename(String filename) {
        wrapped.setFilename(filename);
    }

    @Override
    public void setContentType(String contentType) {
        wrapped.setContentType(contentType);
    }

    @Override
    public String getContentType() {
        return wrapped.getContentType();
    }

    /** 超过阈值时创建 {@link DiskFileUpload} 并继承 maxSize 限制 */
    @Override
    FileUpload makeDiskData() {
        DiskFileUpload diskFileUpload = new DiskFileUpload(
                getName(), getFilename(), getContentType(), getContentTransferEncoding(), getCharset(), definedLength(),
                baseDir, deleteOnExit);
        diskFileUpload.setMaxSize(getMaxSize());
        return diskFileUpload;
    }

    @Override
    public FileUpload copy() {
        // 显式委托父类，保持二进制 API 兼容
        return super.copy();
    }

    @Override
    public FileUpload duplicate() {
        // for binary compatibility
        return super.duplicate();
    }

    @Override
    public FileUpload retainedDuplicate() {
        // for binary compatibility
        return super.retainedDuplicate();
    }

    @Override
    public FileUpload replace(ByteBuf content) {
        // for binary compatibility
        return super.replace(content);
    }

    @Override
    public FileUpload touch() {
        // for binary compatibility
        return super.touch();
    }

    @Override
    public FileUpload touch(Object hint) {
        // for binary compatibility
        return super.touch(hint);
    }

    @Override
    public FileUpload retain() {
        // for binary compatibility
        return super.retain();
    }

    @Override
    public FileUpload retain(int increment) {
        // for binary compatibility
        return super.retain(increment);
    }
}
