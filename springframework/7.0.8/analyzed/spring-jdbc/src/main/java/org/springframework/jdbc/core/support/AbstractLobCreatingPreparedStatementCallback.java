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
 * 管理 {@link LobCreator} 的抽象 {@link PreparedStatementCallback} 实现。通常用作内部类，可以访问周围的方法参数。
 * <p>D 委托给 {@code setValues} 模板方法，用于设置PreparedStatement 上的值，使用给定的 LobCreator 作为 BLOB/CLOB
 * 参数。
 * <p>A 与 {@link org.springframework.jdbc.core.JdbcTemplate} 的使用示例：
 * <pre class="code">JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource); // 可重用对象
 * LobHandler lobHandler = new DefaultLobHandler(); // 可重用对象
 * jdbcTemplate.execute( "INSERT INTO imagedb (image_name, content, description) VALUES (?,
 *  ?, ?)", new AbstractLobCreatingPreparedStatementCallback(lobHandler) { protected void s
 * etValues(PreparedStatement ps, LobCreator lobCreator) throws SQLException { ps.setString
 * (1, name); } lobCreator.setBlobAsBinaryStream(ps, 2, contentStream, contentLength); lobC
 * reator.setClobAsString(ps, 3, 描述); OCAJAVA0文档
 * @author Juergen Hoeller
 * @since 1.0.2
 * @see org.springframework.jdbc.support.lob.LobCreator
 * @deprecated 6.2，支持 {@link SqlBinaryValue} 和 {@link SqlCharacterValue}
 */
@Deprecated(since = "6.2")
public abstract class AbstractLobCreatingPreparedStatementCallback implements PreparedStatementCallback<Integer> {

	/** 处理器相关状态（`lobHandler`）。 */
	private final LobHandler lobHandler;


	/**
	 * 为给定的 LobHandler 创建一个新的 AbstractLobCreatingPreparedStatementCallback。
	 * @param lobHandler 用于创建 LobCreators 的 LobHandler
	 */
	public AbstractLobCreatingPreparedStatementCallback(LobHandler lobHandler) {
		Assert.notNull(lobHandler, "LobHandler must not be null");
		this.lobHandler = lobHandler;
	}


	/**
	 * 执行核心逻辑：In Prepared Statement（方法 `doInPreparedStatement`）。
	 */
	@Override
	public final Integer doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException {
		try (LobCreator lobCreator = this.lobHandler.getLobCreator()) {
			setValues(ps, lobCreator);
			return ps.executeUpdate();
		}
	}

	/**
	 * 使用给定的 BLOB/CLOB 参数的 LobCreator 在给定的PreparedStatement 上设置值。
	 * @param ps 要使用的PreparedStatement
	 * @param lobCreator 要使用的 LobCreator
	 * @throws SQLException 如果由 JDBC 方法抛出
	 * @throws DataAccessException 如果出现自定义异常
	 */
	protected abstract void setValues(PreparedStatement ps, LobCreator lobCreator)
			throws SQLException, DataAccessException;

}
