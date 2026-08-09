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

package org.springframework.boot.autoconfigure.info;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * 项目信息的配置属性。
 *
 * @author Stephane Nicoll
 * @since 1.4.0
 */
@ConfigurationProperties("spring.info")
public class ProjectInfoProperties {

	private final Build build = new Build();

	private final Git git = new Git();

	public Build getBuild() {
		return this.build;
	}

	public Git getGit() {
		return this.git;
	}

	/**
	 * 构建信息相关属性。
	 */
	public static class Build {

		/**
		 * 生成的 build-info.properties 文件位置。
		 */
		private Resource location = new ClassPathResource("META-INF/build-info.properties");

		/**
		 * 文件编码。
		 */
		private Charset encoding = StandardCharsets.UTF_8;

		public Resource getLocation() {
			return this.location;
		}

		public void setLocation(Resource location) {
			this.location = location;
		}

		public Charset getEncoding() {
			return this.encoding;
		}

		public void setEncoding(Charset encoding) {
			this.encoding = encoding;
		}

	}

	/**
	 * Git 信息相关属性。
	 */
	public static class Git {

		/**
		 * 生成的 git.properties 文件位置。
		 */
		private Resource location = new ClassPathResource("git.properties");

		/**
		 * 文件编码。
		 */
		private Charset encoding = StandardCharsets.UTF_8;

		public Resource getLocation() {
			return this.location;
		}

		public void setLocation(Resource location) {
			this.location = location;
		}

		public Charset getEncoding() {
			return this.encoding;
		}

		public void setEncoding(Charset encoding) {
			this.encoding = encoding;
		}

	}

}
