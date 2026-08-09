package com.taobao.arthas.compiler;

/*-
 * #%L
 * compiler
 * %%
 * Copyright (C) 2017 - 2018 SkaLogs
 * %%
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
 * #L%
 */

import javax.tools.SimpleJavaFileObject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * 将 javac 编译产物写入内存的 {@link SimpleJavaFileObject} 实现，不落磁盘。
 */
public class MemoryByteCode extends SimpleJavaFileObject {
    private static final char PKG_SEPARATOR = '.';
    private static final char DIR_SEPARATOR = '/';
    private static final String CLASS_FILE_SUFFIX = ".class";

    /** 累积编译器写入的 .class 字节流 */
    private ByteArrayOutputStream byteArrayOutputStream;

    public MemoryByteCode(String className) {
        super(URI.create("byte:///" + className.replace(PKG_SEPARATOR, DIR_SEPARATOR)
                + Kind.CLASS.extension), Kind.CLASS);
    }

    public MemoryByteCode(String className, ByteArrayOutputStream byteArrayOutputStream)
            throws URISyntaxException {
        this(className);
        this.byteArrayOutputStream = byteArrayOutputStream;
    }

    /** 编译器通过此输出流写入字节码；首次调用时懒创建缓冲区 */
    @Override
    public OutputStream openOutputStream() throws IOException {
        if (byteArrayOutputStream == null) {
            byteArrayOutputStream = new ByteArrayOutputStream();
        }
        return byteArrayOutputStream;
    }

    /** 返回已编译的完整 .class 字节数组 */
    public byte[] getByteCode() {
        return byteArrayOutputStream.toByteArray();
    }

    /** 从虚拟 URI 路径反推全限定类名 */
    public String getClassName() {
        String className = getName();
        className = className.replace(DIR_SEPARATOR, PKG_SEPARATOR);
        className = className.substring(1, className.indexOf(CLASS_FILE_SUFFIX));
        return className;
    }

}
