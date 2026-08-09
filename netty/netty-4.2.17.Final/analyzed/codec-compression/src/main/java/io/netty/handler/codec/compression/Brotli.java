/*
 * Copyright 2021 The Netty Project
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

package io.netty.handler.codec.compression;

import com.aayushatharva.brotli4j.Brotli4jLoader;
import io.netty.util.internal.PlatformDependent;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

/**
 * Brotli 压缩运行时可用性探测：检测 brotli4j 是否在 classpath 中且原生库可加载。
 */
public final class Brotli {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(Brotli.class);
    private static final ClassNotFoundException CNFE;
    private static Throwable cause;

    static {
        ClassNotFoundException cnfe = null;

        try {
            Class.forName("com.aayushatharva.brotli4j.Brotli4jLoader", false,
                PlatformDependent.getClassLoader(Brotli.class));
        } catch (ClassNotFoundException t) {
            cnfe = t;
            logger.debug(
                "brotli4j not in the classpath; Brotli support will be unavailable.");
        }

        CNFE = cnfe;

        // classpath 中存在 brotli4j 时尝试加载原生库
        if (cnfe == null) {
            cause = Brotli4jLoader.getUnavailabilityCause();
            if (cause != null) {
                logger.debug("Failed to load brotli4j; Brotli support will be unavailable.", cause);
            }
        }
    }

    /**
     *
     * @return 当 brotli4j 在 classpath 中且当前平台原生库已成功加载时为 {@code true}
     */
    public static boolean isAvailable() {
        return CNFE == null && Brotli4jLoader.isAvailable();
    }

    /**
     * 当 brotli4j 缺失或原生库不可用时抛出异常。
     * @throws Throwable a ClassNotFoundException if brotli4j is missing
     * or a UnsatisfiedLinkError if brotli4j native lib can't be loaded
     */
    public static void ensureAvailability() throws Throwable {
        if (CNFE != null) {
            throw CNFE;
        }
        Brotli4jLoader.ensureAvailability();
    }

    /**
     * 返回原生库加载失败原因；可用时为 {@code null}。
     */
    public static Throwable cause() {
        return cause;
    }

    private Brotli() {
    }
}
