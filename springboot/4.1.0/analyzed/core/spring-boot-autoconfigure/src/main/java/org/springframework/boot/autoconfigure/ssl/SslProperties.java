/*
 * Copyright 2012-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.autoconfigure.ssl;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 集中式 SSL 信任材料配置属性。
 * <p>
 * 绑定 {@code spring.ssl.bundle.*} 前缀，支持 PEM 与 JKS 两种 bundle 定义及文件监视配置。
 *
 * @author Scott Frederick
 * @author Moritz Halbritter
 * @since 3.1.0
 */
@ConfigurationProperties("spring.ssl")
public class SslProperties {

	/**
	 * SSL bundle 集合。
	 */
	private final Bundles bundle = new Bundles();

	public Bundles getBundle() {
		return this.bundle;
	}

	/**
	 * 定义 SSL Bundle 的属性。
	 */
	public static class Bundles {

		/**
		 * PEM 编码的 SSL 信任材料。
		 */
		private final Map<String, PemSslBundleProperties> pem = new LinkedHashMap<>();

		/**
		 * Java 密钥库格式的 SSL 信任材料。
		 */
		private final Map<String, JksSslBundleProperties> jks = new LinkedHashMap<>();

		/**
		 * 信任材料文件监视配置。
		 */
		private final Watch watch = new Watch();

		public Map<String, PemSslBundleProperties> getPem() {
			return this.pem;
		}

		public Map<String, JksSslBundleProperties> getJks() {
			return this.jks;
		}

		public Watch getWatch() {
			return this.watch;
		}

		public static class Watch {

			/**
			 * 文件监视配置。
			 */
			private final File file = new File();

			public File getFile() {
				return this.file;
			}

			public static class File {

				/**
				 * 静默期，在此时间内无变更后才视为检测到变化。
				 */
				private Duration quietPeriod = Duration.ofSeconds(10);

				public Duration getQuietPeriod() {
					return this.quietPeriod;
				}

				public void setQuietPeriod(Duration quietPeriod) {
					this.quietPeriod = quietPeriod;
				}

			}

		}

	}

}
