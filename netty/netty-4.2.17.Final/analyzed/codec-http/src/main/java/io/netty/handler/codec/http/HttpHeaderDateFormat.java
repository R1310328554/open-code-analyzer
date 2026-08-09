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
package io.netty.handler.codec.http;

import io.netty.util.concurrent.FastThreadLocal;
import io.netty.handler.codec.DateFormatter;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * HTTP 头 Date 字段的解析/格式化工具（已废弃，请用 {@link DateFormatter}）。
 * <p>
 * 解析兼容三种格式，编码仅输出 RFC 标准格式（Sun, 06 Nov 1994 08:49:37 GMT）。
 * @deprecated Use {@link DateFormatter} instead
 */
@Deprecated
public final class HttpHeaderDateFormat extends SimpleDateFormat {
    private static final long serialVersionUID = -925286159755905325L;

    private final SimpleDateFormat format1 = new HttpHeaderDateFormatObsolete1();
    private final SimpleDateFormat format2 = new HttpHeaderDateFormatObsolete2();

    private static final FastThreadLocal<HttpHeaderDateFormat> dateFormatThreadLocal =
            new FastThreadLocal<HttpHeaderDateFormat>() {
                @Override
                protected HttpHeaderDateFormat initialValue() {
                    return new HttpHeaderDateFormat();
                }
            };

    public static HttpHeaderDateFormat get() {
        return dateFormatThreadLocal.get();
    }

    /**
     * 标准日期格式（编码输出格式）。
     * Sun, 06 Nov 1994 08:49:37 GMT -> E, dd MMM yyyy HH:mm:ss z
     */
    private HttpHeaderDateFormat() {
        super("E, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH);
        setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    @Override
    public Date parse(String text, ParsePosition pos) {
        Date date = super.parse(text, pos);
        if (date == null) {
            date = format1.parse(text, pos);
        }
        if (date == null) {
            date = format2.parse(text, pos);
        }
        return date;
    }

    /**
     * 第一种过时格式，解析时作为回退。
     * Sunday, 06-Nov-94 08:49:37 GMT -> E, dd-MMM-yy HH:mm:ss z
     */
    private static final class HttpHeaderDateFormatObsolete1 extends SimpleDateFormat {
        private static final long serialVersionUID = -3178072504225114298L;

        HttpHeaderDateFormatObsolete1() {
            super("E, dd-MMM-yy HH:mm:ss z", Locale.ENGLISH);
            setTimeZone(TimeZone.getTimeZone("GMT"));
        }
    }

    /**
     * 第二种过时格式（asctime 风格）。
     * <p>
     * Sun Nov 6 08:49:37 1994 -> E MMM d HH:mm:ss yyyy
     */
    private static final class HttpHeaderDateFormatObsolete2 extends SimpleDateFormat {
        private static final long serialVersionUID = 3010674519968303714L;

        HttpHeaderDateFormatObsolete2() {
            super("E MMM d HH:mm:ss yyyy", Locale.ENGLISH);
            setTimeZone(TimeZone.getTimeZone("GMT"));
        }
    }
}
