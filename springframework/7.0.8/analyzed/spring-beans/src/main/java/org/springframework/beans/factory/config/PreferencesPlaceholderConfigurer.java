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

package org.springframework.beans.factory.config;

import java.util.Properties;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.InitializingBean;

/**
 * 支持 JDK 1.4 {@link Preferences} API 的 {@link PropertyPlaceholderConfigurer} 子类。
 *
 * <p>占位符解析顺序：先在用户偏好设置中查找键，再在系统偏好设置中查找，
 * 最后在本配置器的 properties 中查找。若未定义对应偏好设置，行为与
 * PropertyPlaceholderConfigurer 相同。
 *
 * <p>支持为系统与用户偏好设置树指定自定义路径；也支持在占位符中指定路径
 *（如 "myPath/myPlaceholderKey"）。未指定时使用各自的根节点。
 *
 * @author Juergen Hoeller
 * @since 16.02.2004
 * @see #setSystemTreePath
 * @see #setUserTreePath
 * @see java.util.prefs.Preferences
 * @deprecated as of 5.2, along with {@link PropertyPlaceholderConfigurer}; to be removed in 8.0
 */
@Deprecated(since = "5.2", forRemoval = true)
@SuppressWarnings({"deprecation", "removal"})
public class PreferencesPlaceholderConfigurer extends PropertyPlaceholderConfigurer implements InitializingBean {

	/** 系统偏好设置树中的路径。 */
	private @Nullable String systemTreePath;

	/** 用户偏好设置树中的路径。 */
	private @Nullable String userTreePath;

	/** 系统偏好设置根节点（或指定路径下的子节点）。 */
	private Preferences systemPrefs = Preferences.systemRoot();

	/** 用户偏好设置根节点（或指定路径下的子节点）。 */
	private Preferences userPrefs = Preferences.userRoot();


	/**
	 * 设置用于解析占位符的系统偏好设置树路径。默认为根节点。
	 */
	public void setSystemTreePath(String systemTreePath) {
		this.systemTreePath = systemTreePath;
	}

	/**
	 * 设置用于解析占位符的用户偏好设置树路径。默认为根节点。
	 */
	public void setUserTreePath(String userTreePath) {
		this.userTreePath = userTreePath;
	}


	/**
	 * 提前获取系统与用户偏好设置树所需节点对应的 {@link Preferences} 实例。
	 */
	@Override
	public void afterPropertiesSet() {
		if (this.systemTreePath != null) {
			this.systemPrefs = this.systemPrefs.node(this.systemTreePath);
		}
		if (this.userTreePath != null) {
			this.userPrefs = this.userPrefs.node(this.userTreePath);
		}
	}

	/**
	 * 占位符解析顺序：用户偏好设置 → 系统偏好设置 → 传入的 properties。
	 */
	@Override
	protected String resolvePlaceholder(String placeholder, Properties props) {
		String path = null;
		String key = placeholder;
		// 若占位符含 '/'，则 '/' 前为路径，后为键名
		int endOfPath = placeholder.lastIndexOf('/');
		if (endOfPath != -1) {
			path = placeholder.substring(0, endOfPath);
			key = placeholder.substring(endOfPath + 1);
		}
		String value = resolvePlaceholder(path, key, this.userPrefs);
		if (value == null) {
			value = resolvePlaceholder(path, key, this.systemPrefs);
			if (value == null) {
				value = props.getProperty(placeholder);
			}
		}
		return value;
	}

	/**
	 * 在指定 {@link Preferences} 中根据路径与键解析占位符值。
	 * @param path 偏好设置路径（占位符中 '/' 之前的部分）
	 * @param key 偏好设置键（占位符中 '/' 之后的部分）
	 * @param preferences 用于解析的 Preferences
	 * @return 占位符对应的值，未找到时返回 {@code null}
	 */
	protected @Nullable String resolvePlaceholder(@Nullable String path, String key, Preferences preferences) {
		if (path != null) {
			// 节点不存在时不创建节点
			try {
				if (preferences.nodeExists(path)) {
					return preferences.node(path).get(key, null);
				}
				else {
					return null;
				}
			}
			catch (BackingStoreException ex) {
				throw new BeanDefinitionStoreException("Cannot access specified node path [" + path + "]", ex);
			}
		}
		else {
			return preferences.get(key, null);
		}
	}

}
