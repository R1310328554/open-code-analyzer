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

package org.springframework.jdbc.core.support;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.support.lob.LobCreator;
import org.springframework.jdbc.support.lob.LobHandler;
import org.springframework.util.Assert;

/**
 * 管理 {@link LobCreator} 的抽象 {@link PreparedStatementCallback} 实现。
 * 通常作为内部类使用，可访问外围方法参数。
 *
 * <p>委托 {@code setValues} 模板方法在 PreparedStatement 上设置值，
 * 使用给定 LobCreator 处理 BLOB/CLOB 参数。
 *
 * <p>与 {@link org.springframework.jdbc.core.JdbcTemplate} 配合使用的示例：
 *
 * <pre class="code">JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);  // 可复用对象
 * LobHandler lobHandler = new DefaultLobHandler();  // 可复用对象
 *
 * jdbcTemplate.execute(
 *     "INSERT INTO imagedb (image_name, content, description) VALUES (?, ?, ?)",
 *     new AbstractLobCreatingPreparedStatementCallback(lobHandler) {
 *       protected void setValues(PreparedStatement ps, LobCreator lobCreator) throws SQLException {
 *         ps.setString(1, name);
 *         lobCreator.setBlobAsBinaryStream(ps, 2, contentStream, contentLength);
 *         lobCreator.setClobAsString(ps, 3, description);
 *       }
 *     });
 * </pre>
 *
 * @author Juergen Hoeller
 * @since 1.0.2
 * @see org.springframework.jdbc.support.lob.LobCreator
 * @deprecated 自 6.2 起弃用，建议使用 {@link SqlBinaryValue} 和 {@link SqlCharacterValue}
 */
@Deprecated(since = "6.2")
public abstract class AbstractLobCreatingPreparedStatementCallback implements PreparedStatementCallback<Integer> {

	private final LobHandler lobHandler;


	/**
	 * 为给定 LobHandler 创建新的 AbstractLobCreatingPreparedStatementCallback。
	 * @param lobHandler 用于创建 LobCreator 的 LobHandler
	 */
	public AbstractLobCreatingPreparedStatementCallback(LobHandler lobHandler) {
		Assert.notNull(lobHandler, "LobHandler must not be null");
		this.lobHandler = lobHandler;
	}


	@Override
	public final Integer doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException {
		try (LobCreator lobCreator = this.lobHandler.getLobCreator()) {
			setValues(ps, lobCreator);
			return ps.executeUpdate();
		}
	}

	/**
	 * 在给定 PreparedStatement 上设置值，
	 * 使用给定 LobCreator 处理 BLOB/CLOB 参数。
	 * @param ps 要使用的 PreparedStatement
	 * @param lobCreator 要使用的 LobCreator
	 * @throws SQLException JDBC 方法抛出时
	 * @throws DataAccessException 自定义异常时
	 */
	protected abstract void setValues(PreparedStatement ps, LobCreator lobCreator)
			throws SQLException, DataAccessException;

}
