/*
 * Copyright 2013 The Netty Project
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
package io.netty.handler.codec.spdy;

import io.netty.handler.codec.CharSequenceValueConverter;
import io.netty.handler.codec.DefaultHeaders;
import io.netty.handler.codec.HeadersUtils;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import static io.netty.util.AsciiString.CASE_INSENSITIVE_HASHER;
import static io.netty.util.AsciiString.CASE_SENSITIVE_HASHER;

/**
 * {@link SpdyHeaders} 的默认实现：SPDY 流/会话级头部容器。
 * <p>基于 {@link DefaultHeaders}，默认大小写不敏感；可选开启名称/值校验
 * （{@link SpdyCodecUtil#validateHeaderName}/{@link SpdyCodecUtil#validateHeaderValue}）。
 */
public class DefaultSpdyHeaders extends DefaultHeaders<CharSequence, CharSequence, SpdyHeaders> implements SpdyHeaders {
    /** SPDY 头部名称校验器，委托 SpdyCodecUtil */
    private static final NameValidator<CharSequence> SpdyNameValidator = new NameValidator<CharSequence>() {
        @Override
        public void validateName(CharSequence name) {
            SpdyCodecUtil.validateHeaderName(name);
        }
    };

    /** 默认开启名称与值校验 */
    public DefaultSpdyHeaders() {
        this(true);
    }

    @SuppressWarnings("unchecked")
    /**
     * @param validate {@code true} 时校验头部名/值合法性
     */
    public DefaultSpdyHeaders(boolean validate) {
        super(CASE_INSENSITIVE_HASHER,
                validate ? HeaderValueConverterAndValidator.INSTANCE : CharSequenceValueConverter.INSTANCE,
                validate ? SpdyNameValidator : NameValidator.NOT_NULL);
    }

    @Override
    public String getAsString(CharSequence name) {
        return HeadersUtils.getAsString(this, name);
    }

    @Override
    public List<String> getAllAsString(CharSequence name) {
        return HeadersUtils.getAllAsString(this, name);
    }

    @Override
    public Iterator<Entry<String, String>> iteratorAsString() {
        return HeadersUtils.iteratorAsString(this);
    }

    @Override
    public boolean contains(CharSequence name, CharSequence value) {
        return contains(name, value, false);
    }

    @Override
    public boolean contains(CharSequence name, CharSequence value, boolean ignoreCase) {
        return contains(name, value,
                ignoreCase ? CASE_INSENSITIVE_HASHER : CASE_SENSITIVE_HASHER);
    }

    /** 写入值前额外校验 SPDY 头部值格式 */
    private static final class HeaderValueConverterAndValidator extends CharSequenceValueConverter {
        public static final HeaderValueConverterAndValidator INSTANCE = new HeaderValueConverterAndValidator();

        @Override
        public CharSequence convertObject(Object value) {
            final CharSequence seq = super.convertObject(value);
            SpdyCodecUtil.validateHeaderValue(seq);
            return seq;
        }
    }
}
