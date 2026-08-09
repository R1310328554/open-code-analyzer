/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.datasource;

import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.util.AssertUtil;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * <p>
 * 基于 JAR 内文件的只读数据源，仅在初始化时加载一次，不支持热更新。
 * </p>
 * <p>
 * 限制：默认读缓冲 1 MB，最大 4 MB；文件不得超过缓冲大小，否则抛异常。默认字符集 UTF-8。
 * </p>
 *
 * @author dingq
 * @author Eric Zhao
 * @since 1.6.0
 */
public class FileInJarReadableDataSource<T> extends AbstractDataSource<String, T> {

    private static final int MAX_SIZE = 1024 * 1024 * 4;
    private static final int DEFAULT_BUF_SIZE = 1024 * 1024;
    private static final Charset DEFAULT_CHARSET = Charset.forName("utf-8");

    private final Charset charset;
    private final String jarName;
    private final String fileInJarName;

    private byte[] buf;
    private JarEntry jarEntry;
    private JarFile jarFile;

    /**
     * @param jarName       待读取的 JAR 路径
     * @param fileInJarName JAR 内配置文件路径
     * @param configParser  配置解析器
     * @throws IOException IO 失败时抛出
     */
    public FileInJarReadableDataSource(String jarName, String fileInJarName, Converter<String, T> configParser)
        throws IOException {
        this(jarName, fileInJarName, configParser, DEFAULT_BUF_SIZE, DEFAULT_CHARSET);
    }

    public FileInJarReadableDataSource(String jarName, String fileInJarName, Converter<String, T> configParser,
                                       int bufSize) throws IOException {
        this(jarName, fileInJarName, configParser, bufSize, DEFAULT_CHARSET);
    }

    public FileInJarReadableDataSource(String jarName, String fileInJarName, Converter<String, T> configParser,
                                       Charset charset) throws IOException {
        this(jarName, fileInJarName, configParser, DEFAULT_BUF_SIZE, charset);
    }

    public FileInJarReadableDataSource(String jarName, String fileInJarName, Converter<String, T> configParser,
                                       int bufSize, Charset charset) throws IOException {
        super(configParser);
        AssertUtil.assertNotBlank(jarName, "jarName cannot be blank");
        AssertUtil.assertNotBlank(fileInJarName, "fileInJarName cannot be blank");
        if (bufSize <= 0 || bufSize > MAX_SIZE) {
            throw new IllegalArgumentException("bufSize must between (0, " + MAX_SIZE + "], but " + bufSize + " get");
        }
        AssertUtil.notNull(charset, "charset can't be null");
        this.buf = new byte[bufSize];
        this.charset = charset;
        this.jarName = jarName;
        this.fileInJarName = fileInJarName;
        initializeJar();
        firstLoad();
    }

    @Override
    public String readSource() throws Exception {
        if (null == jarEntry) {
            // 条目不存在，后续 read 将失败
            RecordLog.warn(String.format("[FileInJarReadableDataSource] File does not exist: %s", jarFile.getName()));
        }
        try (InputStream inputStream = jarFile.getInputStream(jarEntry)) {
            if (inputStream.available() > buf.length) {
                throw new IllegalStateException(String.format("Size of file <%s> exceeds the bufSize (%d): %d",
                    jarFile.getName(), buf.length, inputStream.available()));
            }
            int len = inputStream.read(buf);
            return new String(buf, 0, len, charset);
        }
    }

    /** 构造完成后立即加载一次配置并更新 property。 */
    private void firstLoad() {
        try {
            T newValue = loadConfig();
            getProperty().updateValue(newValue);
        } catch (Throwable e) {
            RecordLog.warn("[FileInJarReadableDataSource] Error when loading config", e);
        }
    }

    @Override
    public void close() throws Exception {
        buf = null;
    }

    private void initializeJar() throws IOException {
        this.jarFile = new JarFile(jarName);
        this.jarEntry = jarFile.getJarEntry(fileInJarName);
    }
}
