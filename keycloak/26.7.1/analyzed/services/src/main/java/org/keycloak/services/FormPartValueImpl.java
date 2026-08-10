/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.keycloak.services;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.keycloak.http.FormPartValue;

/**
 * {@link FormPartValue} 实现：以字符串或 {@link InputStream} 持有 multipart 表单字段值。
 */
public class FormPartValueImpl implements FormPartValue {

    private String value;
    private InputStream inputStream;

    /** 文本字段值。 @param value 字符串内容 */
    public FormPartValueImpl(String value) {
        this.value = value;
    }

    /** 二进制/文件上传字段。 @param inputStream 字段输入流 */
    public FormPartValueImpl(InputStream inputStream) {
        this.inputStream = inputStream;
        this.value = null;
    }

    /** {@inheritDoc} 流式字段时抛出异常 */
    @Override
    public String asString() {
        if (inputStream != null) {
            throw new RuntimeException("Value is a input stream");
        }
        return value;
    }

    /** {@inheritDoc} 字符串字段时转为 {@link ByteArrayInputStream} */
    @Override
    public InputStream asInputStream() {
        if (inputStream == null) {
            return new ByteArrayInputStream(value.getBytes());
        }
        return inputStream;
    }
}
