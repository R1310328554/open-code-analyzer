/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.models.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * 基于时间的一次性密码（TOTP）实现。
 * <p>算法参见 http://tools.ietf.org/html/draft-mraihi-totp-timebased-06，继承 {@link HmacOTP} 生成与校验令牌。</p>
 *
 * @author anil saldhana
 * @since Sep 20, 2010
 */
public class TimeBasedOTP extends HmacOTP {

    /** 默认时间步长（秒）。 */
    public static final int DEFAULT_INTERVAL_SECONDS = 30;
    /** 默认时钟偏移窗口（前后各若干步）。 */
    public static final int DEFAULT_DELAY_WINDOW = 1;

    private Clock clock;

    public TimeBasedOTP() {
        this(DEFAULT_ALGORITHM, DEFAULT_NUMBER_DIGITS, DEFAULT_INTERVAL_SECONDS, DEFAULT_DELAY_WINDOW);
    }

    /**
     * 指定算法、位数、时间步长与校验窗口。
     * @param algorithm the encryption algorithm
     * @param numberDigits the number of digits for tokens
     * @param timeIntervalInSeconds the number of seconds a token is valid
     * @param lookAroundWindow the number of previous and following intervals that should be used to validate tokens.
     */
    public TimeBasedOTP(String algorithm, int numberDigits, int timeIntervalInSeconds, int lookAroundWindow) {
        super(numberDigits, algorithm, lookAroundWindow);
        this.clock = new Clock(timeIntervalInSeconds);
    }

    /**
     * <p>根据共享密钥生成当前时间步的 TOTP。</p>
     *
     * @param secretKey the secret key to derive the token from.
     */
    public String generateTOTP(byte[] secretKey) {
        long T = this.clock.getCurrentInterval();

        String steps = Long.toHexString(T).toUpperCase();

        // 补齐为 16 位十六进制时间步字符串
        while (steps.length() < 16) {
            steps = "0" + steps;
        }

        return generateOTP(secretKey, steps, this.numberDigits, this.algorithm);
    }

    public String generateTOTP(String secretKey) {
        return generateTOTP(secretKey.getBytes());
    }

    /**
     * <p>在时钟偏移窗口内校验 TOTP 是否与共享密钥匹配。</p>
     *
     * @param token  OTP string to validate
     * @param secret Shared secret
     * @return true of the token is valid
     */
    public boolean validateTOTP(String token, byte[] secret) {
        long currentInterval = this.clock.getCurrentInterval();

        for (int i = 0; i <= (lookAroundWindow * 2); i++) {
            long delta = clockSkewIndexToDelta(i);
            long adjustedInterval = currentInterval + delta;

            String steps = Long.toHexString(adjustedInterval).toUpperCase();

            // 补齐为 16 位十六进制时间步字符串
            while (steps.length() < 16) {
                steps = "0" + steps;
            }

            String candidate = generateOTP(secret, steps, this.numberDigits, this.algorithm);

            if (token != null && MessageDigest.isEqual(candidate.getBytes(StandardCharsets.UTF_8), token.getBytes(StandardCharsets.UTF_8))) {
                return true;
            }
        }

        return false;
    }

    /** 将窗口索引映射为时间步偏移：0,1,2,… → 0,-1,1,-2,2,… */
    /**
     * maps 0, 1, 2, 3, 4, 5, 6, 7, ... to 0, -1, 1, -2, 2, -3, 3, ...
     */
    private long clockSkewIndexToDelta(int idx) {
        return (idx + 1) / 2 * (1 - (idx % 2) * 2);
    }

    /** 设置内部时钟（主要用于测试）。 */
    public void setCalendar(Calendar calendar) {
        this.clock.setCalendar(calendar);
    }

    private static class Clock {

        private final int interval;
        private Calendar calendar;

        public Clock(int interval) {
            this.interval = interval;
        }

        public long getCurrentInterval() {
            Calendar currentCalendar = this.calendar;

            if (currentCalendar == null) {
                currentCalendar = GregorianCalendar.getInstance(TimeZone.getTimeZone("UTC"));
            }

            return (currentCalendar.getTimeInMillis() / 1000) / this.interval;
        }

        public void setCalendar(Calendar calendar) {
            this.calendar = calendar;
        }
    }
}