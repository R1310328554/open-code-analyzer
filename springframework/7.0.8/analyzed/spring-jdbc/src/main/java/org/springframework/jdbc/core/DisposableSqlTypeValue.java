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

package org.springframework.jdbc.core;

/**
 * {@link SqlTypeValue} 的子接口，增加清理回调；
 * 在设值且对应语句执行后调用。
 *
 * @author Juergen Hoeller
 * @since 1.1
 * @see org.springframework.jdbc.core.support.SqlLobValue
 */
public interface DisposableSqlTypeValue extends SqlTypeValue {

	/**
	 * 清理本类型值持有的资源，例如 SqlLobValue 中的 LobCreator。
	 * @see org.springframework.jdbc.core.support.SqlLobValue#cleanup()
	 * @see org.springframework.jdbc.support.SqlValue#cleanup()
	 */
	void cleanup();

}
