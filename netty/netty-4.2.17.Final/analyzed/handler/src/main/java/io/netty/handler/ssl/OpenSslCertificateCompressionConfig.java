/*
 * Copyright 2022 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License, version
 * 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at:
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package io.netty.handler.ssl;

import io.netty.util.internal.ObjectUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Configuration for TLS1.3 certificate compression extension.
 *
 * <p>不可变的 TLS 1.3 证书压缩算法列表及方向（压缩/解压/双向）配置；
 * 服务端算法优先级由 {@link Builder#addAlgorithm} 注册顺序决定。</p>
 */
public final class OpenSslCertificateCompressionConfig implements
        Iterable<OpenSslCertificateCompressionConfig.AlgorithmConfig> {
    /** 已注册的算法配置列表（不可变）。 */
    private final List<AlgorithmConfig> pairList;

    private OpenSslCertificateCompressionConfig(AlgorithmConfig... pairs) {
        pairList = Collections.unmodifiableList(Arrays.asList(pairs));
    }

    @Override
    public Iterator<AlgorithmConfig> iterator() {
        return pairList.iterator();
    }

    /**
     * Creates a new {@link Builder} for a config.
     * <p>创建流式构建器，按优先级顺序 {@link Builder#addAlgorithm} 注册算法。</p>
     *
     * @return a bulder
     */
    public static Builder newBuilder() {
        return new Builder();
    }

    /**
     * Builder for an {@link OpenSslCertificateCompressionAlgorithm}.
     * <p>构建 {@link OpenSslCertificateCompressionConfig}；内部列表在 {@link #build()} 前可变。</p>
     */
    public static final class Builder {
        /** 待固化为配置的算法条目。 */
        private final List<AlgorithmConfig> algorithmList = new ArrayList<AlgorithmConfig>();

        private Builder() { }

        /**
         * Adds a certificate compression algorithm.
         * For servers, algorithm preference order is dictated by the order of algorithm registration.
         * Most preferred algorithm should be registered first.
         *
         * @param algorithm implementation of the compression and or decompression algorithm as a
         * {@link OpenSslCertificateCompressionAlgorithm}
         * @param mode indicates whether decompression support should be advertized, compression should be applied
         *                  for peers which support it, or both. This allows the caller to support one way compression
         *                  only.
         *                  <p>{@link AlgorithmMode} 控制本端是否对外宣告压缩能力、解压能力或两者兼有。</p>
         * @return self.
         */
        public Builder addAlgorithm(OpenSslCertificateCompressionAlgorithm algorithm, AlgorithmMode mode) {
            algorithmList.add(new AlgorithmConfig(algorithm, mode));
            return this;
        }

        /**
         * Build a new {@link OpenSslCertificateCompressionConfig} based on the previous
         * added {@link OpenSslCertificateCompressionAlgorithm}s.
         *
         * @return a new config.
         */
        public OpenSslCertificateCompressionConfig build() {
            return new OpenSslCertificateCompressionConfig(algorithmList.toArray(new AlgorithmConfig[0]));
        }
    }

    /**
     * The configuration for algorithm.
     * <p>单个压缩算法及其使用方向的不可变配对。</p>
     */
    public static final class AlgorithmConfig {
        private final OpenSslCertificateCompressionAlgorithm algorithm;
        private final AlgorithmMode mode;

        private AlgorithmConfig(OpenSslCertificateCompressionAlgorithm algorithm, AlgorithmMode mode) {
            this.algorithm = ObjectUtil.checkNotNull(algorithm, "algorithm");
            this.mode = ObjectUtil.checkNotNull(mode, "mode");
        }

        /**
         * The {@link AlgorithmMode}
         *
         * @return the usage mode.
         */
        public AlgorithmMode mode() {
            return mode;
        }

        /**
         * The configured {@link OpenSslCertificateCompressionAlgorithm}.
         *
         * @return the algorithm
         */
        public OpenSslCertificateCompressionAlgorithm algorithm() {
            return algorithm;
        }
    }

    /**
     * The usage mode of the {@link OpenSslCertificateCompressionAlgorithm}.
     * <p>声明算法在本连接上用于压缩 outgoing 证书、解压 incoming 证书，或双向支持。</p>
     */
    public enum AlgorithmMode {
        /**
         * Compression supported and should be advertized.
         * <p>本端发送证书时可压缩。</p>
         */
        Compress,

        /**
         * Decompression supported and should be advertized.
         * <p>本端可解压对端压缩证书。</p>
         */
        Decompress,

        /**
         * Compression and Decompression are supported and both should be advertized.
         * <p>压缩与解压能力均对外宣告。</p>
         */
        Both
    }
}
