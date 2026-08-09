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

package org.springframework.jdbc.support.lob;

import java.io.InputStream;
import java.io.Reader;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.jspecify.annotations.Nullable;

/**
 * {@link LobHandler} 实现的抽象基类。
 *
 * <p>通过列名查找并委托给接受列索引的对应访问器，实现所有按列名访问的方法。
 *
 * @author Juergen Hoeller
 * @since 1.2
 * @see java.sql.ResultSet#findColumn
 * @deprecated 自 6.2 起弃用，推荐使用 {@link org.springframework.jdbc.core.support.SqlBinaryValue}
 * 和 {@link org.springframework.jdbc.core.support.SqlCharacterValue}
 */
@Deprecated(since = "6.2")
public abstract class AbstractLobHandler implements LobHandler {

	@Override
	public byte @Nullable [] getBlobAsBytes(ResultSet rs, String columnName) throws SQLException {
		return getBlobAsBytes(rs, rs.findColumn(columnName));
	}

	@Override
	public @Nullable InputStream getBlobAsBinaryStream(ResultSet rs, String columnName) throws SQLException {
		return getBlobAsBinaryStream(rs, rs.findColumn(columnName));
	}

	@Override
	public @Nullable String getClobAsString(ResultSet rs, String columnName) throws SQLException {
		return getClobAsString(rs, rs.findColumn(columnName));
	}

	@Override
	public @Nullable InputStream getClobAsAsciiStream(ResultSet rs, String columnName) throws SQLException {
		return getClobAsAsciiStream(rs, rs.findColumn(columnName));
	}

	@Override
	public Reader getClobAsCharacterStream(ResultSet rs, String columnName) throws SQLException {
		return getClobAsCharacterStream(rs, rs.findColumn(columnName));
	}

}
