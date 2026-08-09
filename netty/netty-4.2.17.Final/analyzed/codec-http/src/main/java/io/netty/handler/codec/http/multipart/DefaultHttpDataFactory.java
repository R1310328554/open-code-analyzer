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

import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpConstants;
import io.netty.handler.codec.http.HttpRequest;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 默认 {@link HttpDataFactory}：按构造参数创建 Memory/Disk/Mixed 类型的 Attribute 与 FileUpload。
 * <p>处理完成后应 release 并调用 clean 方法，示例：</p>
 * <ul>
 * <li>MemoryAttribute, DiskAttribute or MixedAttribute</li>
 * <li>MemoryFileUpload, DiskFileUpload or MixedFileUpload</li>
 * </ul>
 * A good example of releasing HttpData once all work is done is as follow:<br>
 * <pre>{@code
 *   for (InterfaceHttpData httpData: decoder.getBodyHttpDatas()) {
 *     httpData.release();
 *     factory.removeHttpDataFromClean(request, httpData);
 *   }
 *   factory.cleanAllHttpData();
 *   decoder.destroy();
 *  }</pre>
 */
public class DefaultHttpDataFactory implements HttpDataFactory {

    /**
     * 默认内存阈值：16 KB，超出则 Mixed 模式落盘。
     */
    public static final long MINSIZE = 0x4000;
    /**
     * 默认最大尺寸 {@code -1} 表示不限制（仍受 {@link #setMaxLimit} 约束）。
     */
    public static final long MAXSIZE = -1;

    private final boolean useDisk;

    private final boolean checkSize;

    private long minSize;

    private long maxSize = MAXSIZE;

    private Charset charset = HttpConstants.DEFAULT_CHARSET;

    private String baseDir;

    private boolean deleteOnExit; // false is a good default cause true leaks

    /**
     * 按请求跟踪需清理的 {@link HttpData}；用 {@link IdentityHashMap} 因请求对象可能 equals 相同但非同一实例。
     */
    private final Map<HttpRequest, List<HttpData>> requestFileDeleteMap =
            Collections.synchronizedMap(new IdentityHashMap<HttpRequest, List<HttpData>>());

    /**
     * 默认 Mixed 模式：小于 16KB 驻内存，否则写磁盘。
     */
    public DefaultHttpDataFactory() {
        useDisk = false;
        checkSize = true;
        minSize = MINSIZE;
    }

    public DefaultHttpDataFactory(Charset charset) {
        this();
        this.charset = charset;
    }

    /**
     * {@code useDisk=true} 始终磁盘；{@code false} 始终内存。
     */
    public DefaultHttpDataFactory(boolean useDisk) {
        this.useDisk = useDisk;
        checkSize = false;
    }

    public DefaultHttpDataFactory(boolean useDisk, Charset charset) {
        this(useDisk);
        this.charset = charset;
    }
    /**
     * 指定 {@code minSize} 的 Mixed 模式阈值。
     */
    public DefaultHttpDataFactory(long minSize) {
        useDisk = false;
        checkSize = true;
        this.minSize = minSize;
    }

    public DefaultHttpDataFactory(long minSize, Charset charset) {
        this(minSize);
        this.charset = charset;
    }

    /**
     * 设置磁盘 Attribute/FileUpload 的临时文件基目录。
     *
     * @param baseDir directory path where to store disk attributes and file uploads.
     */
    public void setBaseDir(String baseDir) {
        this.baseDir = baseDir;
    }

    /**
     * Override global {@link DiskAttribute#deleteOnExitTemporaryFile} and
     * {@link DiskFileUpload#deleteOnExitTemporaryFile} values.
     *
     * @param deleteOnExit true if temporary files should be deleted with the JVM, false otherwise.
     */
    public void setDeleteOnExit(boolean deleteOnExit) {
        this.deleteOnExit = deleteOnExit;
    }

    @Override
    public void setMaxLimit(long maxSize) {
        this.maxSize = maxSize;
    }

    /**
     * @return the associated list of {@link HttpData} for the request
     */
    private List<HttpData> getList(HttpRequest request) {
        List<HttpData> list = requestFileDeleteMap.get(request);
        if (list == null) {
            list = new ArrayList<HttpData>();
            requestFileDeleteMap.put(request, list);
        }
        return list;
    }

    @Override
    public Attribute createAttribute(HttpRequest request, String name) {
        if (useDisk) {
            Attribute attribute = new DiskAttribute(name, charset, baseDir, deleteOnExit);
            attribute.setMaxSize(maxSize);
            List<HttpData> list = getList(request);
            list.add(attribute);
            return attribute;
        }
        if (checkSize) {
            Attribute attribute = new MixedAttribute(name, minSize, charset, baseDir, deleteOnExit);
            attribute.setMaxSize(maxSize);
            List<HttpData> list = getList(request);
            list.add(attribute);
            return attribute;
        }
        MemoryAttribute attribute = new MemoryAttribute(name);
        attribute.setMaxSize(maxSize);
        return attribute;
    }

    @Override
    public Attribute createAttribute(HttpRequest request, String name, long definedSize) {
        if (useDisk) {
            Attribute attribute = new DiskAttribute(name, definedSize, charset, baseDir, deleteOnExit);
            attribute.setMaxSize(maxSize);
            List<HttpData> list = getList(request);
            list.add(attribute);
            return attribute;
        }
        if (checkSize) {
            Attribute attribute = new MixedAttribute(name, definedSize, minSize, charset, baseDir, deleteOnExit);
            attribute.setMaxSize(maxSize);
            List<HttpData> list = getList(request);
            list.add(attribute);
            return attribute;
        }
        MemoryAttribute attribute = new MemoryAttribute(name, definedSize);
        attribute.setMaxSize(maxSize);
        return attribute;
    }

    /**
     * 校验已创建 HttpData 未超过 maxSize。
     */
    private static void checkHttpDataSize(HttpData data) {
        try {
            data.checkSize(data.length());
        } catch (IOException ignored) {
            throw new IllegalArgumentException("Attribute bigger than maxSize allowed");
        }
    }

    @Override
    public Attribute createAttribute(HttpRequest request, String name, String value) {
        if (useDisk) {
            Attribute attribute;
            try {
                attribute = new DiskAttribute(name, value, charset, baseDir, deleteOnExit);
                attribute.setMaxSize(maxSize);
            } catch (IOException e) {
                // revert to Mixed mode
                attribute = new MixedAttribute(name, value, minSize, charset, baseDir, deleteOnExit);
                attribute.setMaxSize(maxSize);
            }
            checkHttpDataSize(attribute);
            List<HttpData> list = getList(request);
            list.add(attribute);
            return attribute;
        }
        if (checkSize) {
            Attribute attribute = new MixedAttribute(name, value, minSize, charset, baseDir, deleteOnExit);
            attribute.setMaxSize(maxSize);
            checkHttpDataSize(attribute);
            List<HttpData> list = getList(request);
            list.add(attribute);
            return attribute;
        }
        try {
            MemoryAttribute attribute = new MemoryAttribute(name, value, charset);
            attribute.setMaxSize(maxSize);
            checkHttpDataSize(attribute);
            return attribute;
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public FileUpload createFileUpload(HttpRequest request, String name, String filename,
            String contentType, String contentTransferEncoding, Charset charset,
            long size) {
        if (useDisk) {
            FileUpload fileUpload = new DiskFileUpload(name, filename, contentType,
                    contentTransferEncoding, charset, size, baseDir, deleteOnExit);
            fileUpload.setMaxSize(maxSize);
            checkHttpDataSize(fileUpload);
            List<HttpData> list = getList(request);
            list.add(fileUpload);
            return fileUpload;
        }
        if (checkSize) {
            FileUpload fileUpload = new MixedFileUpload(name, filename, contentType,
                    contentTransferEncoding, charset, size, minSize, baseDir, deleteOnExit);
            fileUpload.setMaxSize(maxSize);
            checkHttpDataSize(fileUpload);
            List<HttpData> list = getList(request);
            list.add(fileUpload);
            return fileUpload;
        }
        MemoryFileUpload fileUpload = new MemoryFileUpload(name, filename, contentType,
                contentTransferEncoding, charset, size);
        fileUpload.setMaxSize(maxSize);
        checkHttpDataSize(fileUpload);
        return fileUpload;
    }

    @Override
    public void removeHttpDataFromClean(HttpRequest request, InterfaceHttpData data) {
        if (!(data instanceof HttpData)) {
            return;
        }

        // Do not use getList because it adds empty list to requestFileDeleteMap
        // if request is not found
        List<HttpData> list = requestFileDeleteMap.get(request);
        if (list == null) {
            return;
        }

        // Can't simply call list.remove(data), because different data items may be equal.
        // Need to check identity.
        Iterator<HttpData> i = list.iterator();
        while (i.hasNext()) {
            HttpData n = i.next();
            if (n == data) {
                i.remove();

                // Remove empty list to avoid memory leak
                if (list.isEmpty()) {
                    requestFileDeleteMap.remove(request);
                }

                return;
            }
        }
    }

    @Override
    public void cleanRequestHttpData(HttpRequest request) {
        List<HttpData> list = requestFileDeleteMap.remove(request);
        if (list != null) {
            for (HttpData data : list) {
                data.release();
            }
        }
    }

    @Override
    public void cleanAllHttpData() {
        Iterator<Entry<HttpRequest, List<HttpData>>> i = requestFileDeleteMap.entrySet().iterator();
        while (i.hasNext()) {
            Entry<HttpRequest, List<HttpData>> e = i.next();

            // Calling i.remove() here will cause "java.lang.IllegalStateException: Entry was removed"
            // at e.getValue() below

            List<HttpData> list = e.getValue();
            for (HttpData data : list) {
                data.release();
            }

            i.remove();
        }
    }

    @Override
    public void cleanRequestHttpDatas(HttpRequest request) {
        cleanRequestHttpData(request);
    }

    @Override
    public void cleanAllHttpDatas() {
        cleanAllHttpData();
    }
}
