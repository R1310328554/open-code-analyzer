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

package org.springframework.boot.context.config;

import java.util.EventListener;

import org.jspecify.annotations.Nullable;

import org.springframework.boot.context.config.ConfigData.Options;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;

/**
 * 监听 {@link ConfigDataEnvironmentPostProcessor} 触发的 {@link Environment} 更新的 {@link EventListener}。
 *
 * @author Phillip Webb
 * @since 2.4.2
 */
public interface ConfigDataEnvironmentUpdateListener extends EventListener {

	/**
	 * 空操作的 {@link ConfigDataEnvironmentUpdateListener}。
	 */
	ConfigDataEnvironmentUpdateListener NONE = new ConfigDataEnvironmentUpdateListener() {
	};

	/**
	 * 向 {@link Environment} 添加新 {@link PropertySource} 时调用。
	 *
	 * @param propertySource 已添加的 {@link PropertySource}
	 * @param location 源的原始 {@link ConfigDataLocation}
	 * @param resource 源的 {@link ConfigDataResource}
	 */
	default void onPropertySourceAdded(PropertySource<?> propertySource, @Nullable ConfigDataLocation location,
			@Nullable ConfigDataResource resource) {
	}

	/**
	 * 设置 {@link Environment} profile 时调用。
	 *
	 * @param profiles 正在设置的 profile
	 */
	default void onSetProfiles(Profiles profiles) {
	}

	/**
	 * 获取特定属性源的配置数据选项时调用。
	 *
	 * @param configData 配置数据
	 * @param propertySource 属性源
	 * @param options {@link ConfigData#getOptions(PropertySource)} 提供的选项
	 * @return 实际应使用的选项
	 * @since 3.5.1
	 */
	default ConfigData.Options onConfigDataOptions(ConfigData configData, PropertySource<?> propertySource,
			Options options) {
		return options;
	}

}
