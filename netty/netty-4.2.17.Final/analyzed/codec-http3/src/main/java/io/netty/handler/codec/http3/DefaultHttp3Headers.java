/*
 * Copyright 2020 The Netty Project
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
package io.netty.handler.codec.http3;

import io.netty.handler.codec.CharSequenceValueConverter;
import io.netty.handler.codec.DefaultHeaders;
import io.netty.util.AsciiString;
import io.netty.util.ByteProcessor;
import org.jetbrains.annotations.Nullable;

import static io.netty.handler.codec.http3.Http3Headers.PseudoHeaderName.hasPseudoHeaderFormat;
import static io.netty.util.AsciiString.CASE_INSENSITIVE_HASHER;
import static io.netty.util.AsciiString.CASE_SENSITIVE_HASHER;
import static io.netty.util.AsciiString.isUpperCase;

/**
 * {@link Http3Headers} 的默认实现，基于 {@link DefaultHeaders} 并满足 HTTP/3 头部约束。
 * <p>伪头部（{@code :method}、{@code :path} 等）在迭代顺序上始终位于普通头部之前，与 RFC 9114 一致。
 */
public final class DefaultHttp3Headers
        extends DefaultHeaders<CharSequence, CharSequence, Http3Headers> implements Http3Headers {
    /** 逐字节扫描头部名，拒绝 HTTP/2 风格的大写字母（HTTP/3 要求小写）。 */
    private static final ByteProcessor HTTP3_NAME_VALIDATOR_PROCESSOR = new ByteProcessor() {
        @Override
        public boolean process(byte value) {
            return !isUpperCase(value);
        }
    };
    /** HTTP/3 头部名校验器：非空且不得含大写 ASCII。 */
    static final NameValidator<CharSequence> HTTP3_NAME_VALIDATOR = new NameValidator<CharSequence>() {
        @Override
        public void validateName(@Nullable CharSequence name) {
            if (name == null || name.length() == 0) {
                throw new Http3HeadersValidationException(String.format("empty headers are not allowed [%s]", name));
            }
            if (name instanceof AsciiString) {
                final int index;
                try {
                    index = ((AsciiString) name).forEachByte(HTTP3_NAME_VALIDATOR_PROCESSOR);
                } catch (Http3HeadersValidationException e) {
                    throw e;
                } catch (Throwable t) {
                    throw new Http3HeadersValidationException(
                            String.format("unexpected error. invalid header name [%s]", name), t);
                }

                if (index != -1) {
                    throw new Http3HeadersValidationException(String.format("invalid header name [%s]", name));
                }
            } else {
                for (int i = 0; i < name.length(); ++i) {
                    if (isUpperCase(name.charAt(i))) {
                        throw new Http3HeadersValidationException(String.format("invalid header name [%s]", name));
                    }
                }
            }
        }
    };

    /** 双向链表中第一个非伪头部节点，用于维持伪头部优先的迭代顺序。 */
    private HeaderEntry<CharSequence, CharSequence> firstNonPseudo = head;

    /**
     * Create a new instance.
     * <p>
     * Header names will be validated according to
     * <a href="https://tools.ietf.org/html/rfc7540">rfc7540</a>.
     * <p>默认开启 RFC 7540 风格的头部名校验（HTTP/3 沿用相同小写规则）。
     */
    public DefaultHttp3Headers() {
        this(true);
    }

    /**
     * Create a new instance.
     * @param validate {@code true} to validate header names according to
     * <a href="https://tools.ietf.org/html/rfc7540">rfc7540</a>. {@code false} to not validate header names.
     */
    @SuppressWarnings("unchecked")
    public DefaultHttp3Headers(boolean validate) {
        // 大小写敏感哈希更廉价；非法头部由 HTTP3_NAME_VALIDATOR 在写入时拦截。
        super(CASE_SENSITIVE_HASHER,
              CharSequenceValueConverter.INSTANCE,
              validate ? HTTP3_NAME_VALIDATOR : NameValidator.NOT_NULL);
    }

    /**
     * Create a new instance.
     * @param validate {@code true} to validate header names according to
     * <a href="https://tools.ietf.org/html/rfc7540">rfc7540</a>. {@code false} to not validate header names.
     * @param arraySizeHint A hint as to how large the hash data structure should be.
     * The next positive power of two will be used. An upper bound may be enforced.
     */
    @SuppressWarnings("unchecked")
    public DefaultHttp3Headers(boolean validate, int arraySizeHint) {
        // 大小写敏感哈希更廉价；非法头部由 HTTP3_NAME_VALIDATOR 在写入时拦截。
        super(CASE_SENSITIVE_HASHER,
              CharSequenceValueConverter.INSTANCE,
              validate ? HTTP3_NAME_VALIDATOR : NameValidator.NOT_NULL,
              arraySizeHint);
    }

    @Override
    public Http3Headers clear() {
        this.firstNonPseudo = head;
        return super.clear();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Http3Headers && equals((Http3Headers) o, CASE_SENSITIVE_HASHER);
    }

    @Override
    public int hashCode() {
        return hashCode(CASE_SENSITIVE_HASHER);
    }

    @Override
    public Http3Headers method(CharSequence value) {
        set(PseudoHeaderName.METHOD.value(), value);
        return this;
    }

    @Override
    public Http3Headers scheme(CharSequence value) {
        set(PseudoHeaderName.SCHEME.value(), value);
        return this;
    }

    @Override
    public Http3Headers authority(CharSequence value) {
        set(PseudoHeaderName.AUTHORITY.value(), value);
        return this;
    }

    @Override
    public Http3Headers path(CharSequence value) {
        set(PseudoHeaderName.PATH.value(), value);
        return this;
    }

    @Override
    public Http3Headers status(CharSequence value) {
        set(PseudoHeaderName.STATUS.value(), value);
        return this;
    }

    @Override
    public CharSequence method() {
        return get(PseudoHeaderName.METHOD.value());
    }

    @Override
    public CharSequence scheme() {
        return get(PseudoHeaderName.SCHEME.value());
    }

    @Override
    public CharSequence authority() {
        return get(PseudoHeaderName.AUTHORITY.value());
    }

    @Override
    public CharSequence path() {
        return get(PseudoHeaderName.PATH.value());
    }

    @Override
    public CharSequence status() {
        return get(PseudoHeaderName.STATUS.value());
    }

    @Override
    public boolean contains(CharSequence name, CharSequence value) {
        return contains(name, value, false);
    }

    @Override
    public boolean contains(CharSequence name, CharSequence value, boolean caseInsensitive) {
        return contains(name, value, caseInsensitive ? CASE_INSENSITIVE_HASHER : CASE_SENSITIVE_HASHER);
    }

    @Override
    protected HeaderEntry<CharSequence, CharSequence> newHeaderEntry(int h, CharSequence name, CharSequence value,
                                                           HeaderEntry<CharSequence, CharSequence> next) {
        return new Http3HeaderEntry(h, name, value, next);
    }

    /** 自定义链表节点：插入时将伪头部排在普通头部之前。 */
    private final class Http3HeaderEntry extends HeaderEntry<CharSequence, CharSequence> {
        protected Http3HeaderEntry(int hash, CharSequence key, CharSequence value,
                HeaderEntry<CharSequence, CharSequence> next) {
            super(hash, key);
            this.value = value;
            this.next = next;

            // 伪头部插入 firstNonPseudo 之前，保证迭代时 :method 等始终在前
            if (hasPseudoHeaderFormat(key)) {
                after = firstNonPseudo;
                before = firstNonPseudo.before();
            } else {
                after = head;
                before = head.before();
                if (firstNonPseudo == head) {
                    firstNonPseudo = this;
                }
            }
            pointNeighborsToThis();
        }

        @Override
        protected void remove() {
            if (this == firstNonPseudo) {
                firstNonPseudo = firstNonPseudo.after();
            }
            super.remove();
        }
    }
}
