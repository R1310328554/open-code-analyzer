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
 * 假设 LOB 数据流的抽象 ResultSetExtractor 实现。通常用作内部类，可以访问周围的方法参数。
 * <p>D 委托 {@code streamData} 模板方法将 LOB 内容流式传输到某些 OutputStream（通常使用 LobHandler）。将流式传输期间抛出的
 * IOException 转换为 LobRetrievalFailureException。
 * <p>A 与 JdbcTemplate 的使用示例：
 * <pre class="code">JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource); // 可重用对象
 * Final LobHandler lobHandler = new DefaultLobHandler(); // 可重用对象
 * jdbcTemplate.query( "从 imagedb 中选择内容，其中 image_name=?", new Object[] {name}, new
 * AbstractLobStreamingResultSetExtractor() { public void streamData(ResultSet rs) 抛出
 * SQLException, IOException { FileCopyUtils.copy(lobHandler.getBlobAsBinaryStream(rs, 1),
 * contentStream); } }); OCAJAVA0文档
 * @author Juergen Hoeller
 * @since 1.0.2
 * @param <T> 结果类型
 * @see org.springframework.jdbc.support.lob.LobHandler
 * @see org.springframework.jdbc.LobRetrievalFailureException
 * @deprecated 6.2 与 {@link org.springframework.jdbc.support.lob.LobHandler} 一起使用，支持 {@link ResultSet#getBinaryStream}/{@link ResultSet#getCharacterStream} 使用
 */
@Deprecated(since = "6.2")
public abstract class AbstractLobStreamingResultSetExtractor<T> implements ResultSetExtractor<@Nullable T> {

	/**
	 * 根据 ResultSet 状态委托handleNoRowFound、handleMultipleRowsFound 和streamData。将streamData
	 * 抛出的IOException 转换为LobRetrievalFailureException。
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
	 * 处理 ResultSet 不包含行的情况。
	 * @throws DataAccessException 相应的异常，默认为 EmptyResultDataAccessException
	 * @see org.springframework.dao.EmptyResultDataAccessException
	 */
	protected void handleNoRowFound() throws DataAccessException {
		throw new EmptyResultDataAccessException(
				"LobStreamingResultSetExtractor did not find row in database", 1);
	}

	/**
	 * 处理 ResultSet 包含多行的情况。
	 * @throws DataAccessException 相应的异常，默认情况下为 In CorrectResultSizeDataAccessException
	 * @see org.springframework.dao.IncorrectResultSizeDataAccessException
	 */
	protected void handleMultipleRowsFound() throws DataAccessException {
		throw new IncorrectResultSizeDataAccessException(
				"LobStreamingResultSetExtractor found multiple rows in database", 1);
	}

	/**
	 * 将 LOB 内容从给定的 ResultSet 流式传输到某个 OutputStream。 <p>通常用作内部类，可以访问周围的方法参数和周围类的 LobHandler 实例变量
	 * 。
	 * @param rs 从中获取 LOB 内容的 ResultSet
	 * @throws SQLException 如果由 JDBC 方法抛出
	 * @throws IOException 如果由流访问方法抛出
	 * @throws DataAccessException 如果出现自定义异常
	 * @see org.springframework.jdbc.support.lob.LobHandler#getBlobAsBinaryStream
	 * @see org.springframework.util.FileCopyUtils
	 */
	protected abstract void streamData(ResultSet rs) throws SQLException, IOException, DataAccessException;

}
