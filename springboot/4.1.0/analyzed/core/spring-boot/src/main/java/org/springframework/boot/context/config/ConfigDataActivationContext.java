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

import org.jspecify.annotations.Nullable;

import org.springframework.boot.cloud.CloudPlatform;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.Environment;
import org.springframework.core.style.ToStringCreator;

/**
 * 判定何时激活 {@link ConfigDataEnvironmentContributor 贡献的} {@link ConfigData} 时使用的上下文信息。
 *
 * @author Phillip Webb
 */
class ConfigDataActivationContext {

	private final @Nullable CloudPlatform cloudPlatform;

	private final @Nullable Profiles profiles;

	/**
	 * 在尚未激活任何 profile 时创建新的 {@link ConfigDataActivationContext} 实例。
	 *
	 * @param environment 源环境
	 * @param binder 提供相关配置数据贡献访问的绑定器
	 */
	ConfigDataActivationContext(Environment environment, Binder binder) {
		this.cloudPlatform = deduceCloudPlatform(environment, binder);
		this.profiles = null;
	}

	/**
	 * 使用给定 {@link CloudPlatform} 与 {@link Profiles} 创建新的 {@link ConfigDataActivationContext} 实例。
	 *
	 * @param cloudPlatform 云平台
	 * @param profiles profile 信息
	 */
	ConfigDataActivationContext(@Nullable CloudPlatform cloudPlatform, @Nullable Profiles profiles) {
		this.cloudPlatform = cloudPlatform;
		this.profiles = profiles;
	}

	private @Nullable CloudPlatform deduceCloudPlatform(Environment environment, Binder binder) {
		for (CloudPlatform candidate : CloudPlatform.values()) {
			if (candidate.isEnforced(binder)) {
				return candidate;
			}
		}
		return CloudPlatform.getActive(environment);
	}

	/**
	 * 返回包含指定 profile 的新 {@link ConfigDataActivationContext}。
	 *
	 * @param profiles profile 信息
	 * @return 包含指定 profile 的新实例
	 */
	ConfigDataActivationContext withProfiles(Profiles profiles) {
		return new ConfigDataActivationContext(this.cloudPlatform, profiles);
	}

	/**
	 * 返回活动的 {@link CloudPlatform}，或 {@code null}。
	 *
	 * @return 活动云平台
	 */
	@Nullable CloudPlatform getCloudPlatform() {
		return this.cloudPlatform;
	}

	/**
	 * 若可用则返回 profile 信息。
	 *
	 * @return profile 信息，或 {@code null}
	 */
	@Nullable Profiles getProfiles() {
		return this.profiles;
	}

	@Override
	public String toString() {
		ToStringCreator creator = new ToStringCreator(this);
		creator.append("cloudPlatform", this.cloudPlatform);
		creator.append("profiles", this.profiles);
		return creator.toString();
	}

}
