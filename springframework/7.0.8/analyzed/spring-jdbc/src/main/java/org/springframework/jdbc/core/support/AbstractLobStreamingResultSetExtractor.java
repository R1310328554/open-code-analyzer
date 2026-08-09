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

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.jspecify.annotations.Nullable;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.LobRetrievalFailureException;
import org.springframework.jdbc.core.ResultSetExtractor;

/**
 * 假定以流式方式读取 LOB 数据的抽象 {@link ResultSetExtractor} 实现。
 * 通常作为内部类使用，可访问外围方法参数。
 *
 * <p>委托 {@code streamData} 模板方法将 LOB 内容流式写入 OutputStream，
 * 通常借助 LobHandler。流式读取期间抛出的 IOException 会转换为 LobRetrievalFailureException。
 *
 * <p>与 JdbcTemplate 配合使用的示例：
 *
 * <pre class="code">JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);  // 可复用对象
 * final LobHandler lobHandler = new DefaultLobHandler();  // 可复用对象
 *
 * jdbcTemplate.query(
 *	   "SELECT content FROM imagedb WHERE image_name=?", new Object[] {name},
 *	   new AbstractLobStreamingResultSetExtractor() {
 *	     public void streamData(ResultSet rs) throws SQLException, IOException {
 *         FileCopyUtils.copy(lobHandler.getBlobAsBinaryStream(rs, 1), contentStream);
 *       }
 *     });
 * </pre>
 *
 * @author Juergen Hoeller
 * @since 1.0.2
 * @param <T> 结果类型
 * @see org.springframework.jdbc.support.lob.LobHandler
 * @see org.springframework.jdbc.LobRetrievalFailureException
 * @deprecated 自 6.2 起与 {@link org.springframework.jdbc.support.lob.LobHandler} 一并弃用，
 * 建议使用 {@link ResultSet#getBinaryStream}/{@link ResultSet#getCharacterStream}
 */
@Deprecated(since = "6.2")
public abstract class AbstractLobStreamingResultSetExtractor<T> implements ResultSetExtractor<@Nullable T> {

	/**
	 * 根据 ResultSet 状态分别委托 handleNoRowFound、handleMultipleRowsFound 和 streamData。
	 * 将 streamData 抛出的 IOException 转换为 LobRetrievalFailureException。
	 * @see #handleNoRowFound
	 * @see #handleMultipleRowsFound
	 * @see #streamData
	 * @see org.springframework.jdbc.LobRetrievalFailureException
	 */
	@Override
	public final @Nullable T extractData(ResultSet rs) throws SQLException, DataAccessException {
		if (!rs.next()) {
			handleNoRowFound();
		}
		else {
			try {
				streamData(rs);
				if (rs.next()) {
					handleMultipleRowsFound();
				}
			}
			catch (IOException ex) {
				throw new LobRetrievalFailureException("Could not stream LOB content", ex);
			}
		}
		return null;
	}

	/**
	 * 处理 ResultSet 不包含任何行的情况。
	 * @throws DataAccessException 对应异常，默认抛出 EmptyResultDataAccessException
	 * @see org.springframework.dao.EmptyResultDataAccessException
	 */
	protected void handleNoRowFound() throws DataAccessException {
		throw new EmptyResultDataAccessException(
				"LobStreamingResultSetExtractor did not find row in database", 1);
	}

	/**
	 * 处理 ResultSet 包含多行的情况。
	 * @throws DataAccessException 对应异常，默认抛出 IncorrectResultSizeDataAccessException
	 * @see org.springframework.dao.IncorrectResultSizeDataAccessException
	 */
	protected void handleMultipleRowsFound() throws DataAccessException {
		throw new IncorrectResultSizeDataAccessException(
				"LobStreamingResultSetExtractor found multiple rows in database", 1);
	}

	/**
	 * 从给定 ResultSet 将 LOB 内容流式写入 OutputStream。
	 * <p>通常作为内部类使用，可访问外围方法参数及外围类的 LobHandler 实例变量。
	 * @param rs 读取 LOB 内容的 ResultSet
	 * @throws SQLException JDBC 方法抛出时
	 * @throws IOException 流访问方法抛出时
	 * @throws DataAccessException 自定义异常时
	 * @see org.springframework.jdbc.support.lob.LobHandler#getBlobAsBinaryStream
	 * @see org.springframework.util.FileCopyUtils
	 */
	protected abstract void streamData(ResultSet rs) throws SQLException, IOException, DataAccessException;

}
