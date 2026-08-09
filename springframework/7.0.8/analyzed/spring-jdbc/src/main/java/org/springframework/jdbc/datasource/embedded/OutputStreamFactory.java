/*
 * Copyright 2002-present the original author or authors.
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

package org.springframework.jdbc.datasource.embedded;

import java.io.IOException;
import java.io.OutputStream;

/**
 * 用于向嵌入式数据库（例如 Derby）公开虚拟 OutputStreams 的内部帮助程序，以防止创建日志文件。
 * @author Juergen Hoeller
 * @since 3.0
 */
public final class OutputStreamFactory {

	/**
	 * 创建 `OutputStreamFactory` 的新实例。
	 */
	private OutputStreamFactory() {
	}


	/**
	 * 返回一个 {@link java.io.OutputStream}，忽略提供给它的所有数据。
	 */
	public static OutputStream getNoopOutputStream() {
		return new OutputStream() {
			@Override
			public void write(int b) throws IOException {
				// 忽略输出
			}
		};
	}

}
