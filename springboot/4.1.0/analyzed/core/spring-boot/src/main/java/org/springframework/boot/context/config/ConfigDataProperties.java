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

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import org.jspecify.annotations.Nullable;

import org.springframework.boot.cloud.CloudPlatform;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.bind.Name;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

/**
 * 处理 {@link ConfigData} 时使用的绑定属性。
 *
 * @author Phillip Webb
 * @author Madhura Bhave
 * @author Yanming Zhou
 */
class ConfigDataProperties {

	private static final ConfigurationPropertyName NAME = ConfigurationPropertyName.of("spring.config");

	private static final Bindable<ConfigDataProperties> BINDABLE_PROPERTIES = Bindable.of(ConfigDataProperties.class);

	private final List<ConfigDataLocation> imports;

	private final @Nullable Activate activate;

	/**
	 * 创建新的 {@link ConfigDataProperties} 实例。
	 *
	 * @param imports 请求的导入项
	 * @param activate 激活属性
	 */
	ConfigDataProperties(@Nullable @Name("import") List<ConfigDataLocation> imports, @Nullable Activate activate) {
		this.imports = (imports != null) ? imports.stream().filter(ConfigDataLocation::isNotEmpty).toList()
				: Collections.emptyList();
		this.activate = activate;
	}

	/**
	 * 返回请求的额外导入项。
	 *
	 * @return 请求的导入项
	 */
	List<ConfigDataLocation> getImports() {
		return this.imports;
	}

	/**
	 * 若属性表明配置数据属性源在给定激活上下文中处于活动状态则返回 {@code true}。
	 *
	 * @param activationContext 激活上下文
	 * @return 配置数据属性源是否活动
	 */
	boolean isActive(@Nullable ConfigDataActivationContext activationContext) {
		return this.activate == null || this.activate.isActive(activationContext);
	}

	/**
	 * 返回不含任何导入项的此属性新副本。
	 *
	 * @return 新的 {@link ConfigDataProperties} 实例
	 */
	ConfigDataProperties withoutImports() {
		return new ConfigDataProperties(null, this.activate);
	}

	/**
	 * 从给定 {@link Binder} 创建 {@link ConfigDataProperties} 的工厂方法。
	 *
	 * @param binder 用于绑定属性的绑定器
	 * @return {@link ConfigDataProperties} 实例，或 {@code null}
	 */
	static @Nullable ConfigDataProperties get(Binder binder) {
		return binder.bind(NAME, BINDABLE_PROPERTIES, new ConfigDataLocationBindHandler()).orElse(null);
	}

	/**
	 * 用于判定配置数据属性源何时激活的激活属性。
	 */
	static class Activate {

		private final @Nullable CloudPlatform onCloudPlatform;

		private final String @Nullable [] onProfile;

		/**
		 * 创建新的 {@link Activate} 实例。
		 *
		 * @param onCloudPlatform 激活所需的云平台
		 * @param onProfile 激活所需的 profile 表达式
		 */
		Activate(@Nullable CloudPlatform onCloudPlatform, String @Nullable [] onProfile) {
			this.onProfile = onProfile;
			this.onCloudPlatform = onCloudPlatform;
		}

		/**
		 * 若属性表明配置数据属性源在给定激活上下文中处于活动状态则返回 {@code true}。
		 *
		 * @param activationContext 激活上下文
		 * @return 配置数据属性源是否活动
		 */
		boolean isActive(@Nullable ConfigDataActivationContext activationContext) {
			if (activationContext == null) {
				return false;
			}
			CloudPlatform cloudPlatform = activationContext.getCloudPlatform();
			boolean activate = isActive((cloudPlatform != null) ? cloudPlatform : CloudPlatform.NONE);
			activate = activate && isActive(activationContext.getProfiles());
			return activate;
		}

		private boolean isActive(CloudPlatform cloudPlatform) {
			return this.onCloudPlatform == null || this.onCloudPlatform == cloudPlatform;
		}

		private boolean isActive(@Nullable Profiles profiles) {
			return ObjectUtils.isEmpty(this.onProfile)
					|| (profiles != null && matchesActiveProfiles(profiles::isAccepted));
		}

		private boolean matchesActiveProfiles(Predicate<String> activeProfiles) {
			Assert.state(this.onProfile != null, "'this.onProfile' must not be null");
			return org.springframework.core.env.Profiles.of(this.onProfile).matches(activeProfiles);
		}

	}

}
