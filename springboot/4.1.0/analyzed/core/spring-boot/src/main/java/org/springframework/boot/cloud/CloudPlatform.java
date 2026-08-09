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

package org.springframework.boot.cloud;

import java.util.Arrays;
import java.util.List;

import org.jspecify.annotations.Nullable;

import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.StandardEnvironment;

/**
 * 常见云平台检测。可通过 {@code "spring.main.cloud-platform"} 配置属性强制指定。
 *
 * @author Phillip Webb
 * @author Brian Clozel
 * @author Nguyen Sach
 * @since 1.3.0
 */
public enum CloudPlatform {

	/**
	 * 非云平台。在出现误检测时有用。
	 */
	NONE {

		@Override
		public boolean isDetected(Environment environment) {
			return false;
		}

		@Override
		public boolean isUsingForwardHeaders() {
			return false;
		}
	},

	/**
	 * Cloud Foundry 平台。
	 */
	CLOUD_FOUNDRY {

		@Override
		public boolean isDetected(Environment environment) {
			return environment.containsProperty("VCAP_APPLICATION") || environment.containsProperty("VCAP_SERVICES");
		}

	},

	/**
	 * Heroku 平台。
	 */
	HEROKU {

		@Override
		public boolean isDetected(Environment environment) {
			return environment.containsProperty("DYNO");
		}

	},

	/**
	 * SAP Cloud 平台。
	 */
	SAP {

		@Override
		public boolean isDetected(Environment environment) {
			return environment.containsProperty("HC_LANDSCAPE");
		}

	},

	/**
	 * Nomad 平台。
	 * @since 3.1.0
	 */
	NOMAD {

		@Override
		public boolean isDetected(Environment environment) {
			return environment.containsProperty("NOMAD_ALLOC_ID");
		}

	},

	/**
	 * Kubernetes 平台。
	 */
	KUBERNETES {

		private static final String KUBERNETES_SERVICE_HOST = "KUBERNETES_SERVICE_HOST";

		private static final String KUBERNETES_SERVICE_PORT = "KUBERNETES_SERVICE_PORT";

		private static final String SERVICE_HOST_SUFFIX = "_SERVICE_HOST";

		private static final String SERVICE_PORT_SUFFIX = "_SERVICE_PORT";

		@Override
		public boolean isDetected(Environment environment) {
			if (environment instanceof ConfigurableEnvironment configurableEnvironment) {
				return isAutoDetected(configurableEnvironment);
			}
			return false;
		}

		private boolean isAutoDetected(ConfigurableEnvironment environment) {
			PropertySource<?> environmentPropertySource = environment.getPropertySources()
				.get(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME);
			if (environmentPropertySource != null) {
				if (environmentPropertySource.containsProperty(KUBERNETES_SERVICE_HOST)
						&& environmentPropertySource.containsProperty(KUBERNETES_SERVICE_PORT)) {
					return true;
				}
				if (environmentPropertySource instanceof EnumerablePropertySource<?> enumerablePropertySource) {
					return isAutoDetected(enumerablePropertySource);
				}
			}
			return false;
		}

		private boolean isAutoDetected(EnumerablePropertySource<?> environmentPropertySource) {
			for (String propertyName : environmentPropertySource.getPropertyNames()) {
				if (propertyName.endsWith(SERVICE_HOST_SUFFIX)) {
					String serviceName = propertyName.substring(0,
							propertyName.length() - SERVICE_HOST_SUFFIX.length());
					if (environmentPropertySource.getProperty(serviceName + SERVICE_PORT_SUFFIX) != null) {
						return true;
					}
				}
			}
			return false;
		}

	},

	/**
	 * Azure App Service 平台。
	 */
	AZURE_APP_SERVICE {

		private final List<String> azureEnvVariables = Arrays.asList("WEBSITE_SITE_NAME", "WEBSITE_INSTANCE_ID",
				"WEBSITE_RESOURCE_GROUP", "WEBSITE_SKU");

		@Override
		public boolean isDetected(Environment environment) {
			return this.azureEnvVariables.stream().allMatch(environment::containsProperty);
		}

	},

	/**
	 * Amazon Web Services (AWS) Elastic Container Service (ECS) 平台。
	 * @since 4.0.0
	 */
	AWS_ECS {

		@Override
		public boolean isDetected(Environment environment) {
			String awsExecutionEnv = environment.getProperty("AWS_EXECUTION_ENV");
			return (awsExecutionEnv != null) && awsExecutionEnv.startsWith("AWS_ECS");
		}

	};

	private static final String PROPERTY_NAME = "spring.main.cloud-platform";

	/**
	 * 判断平台是否处于激活状态（即应用是否运行在该平台上）。
	 * @param environment 环境
	 * @return 平台是否激活
	 */
	public boolean isActive(Environment environment) {
		String platformProperty = environment.getProperty(PROPERTY_NAME);
		return isEnforced(platformProperty) || (platformProperty == null && isDetected(environment));
	}

	/**
	 * 通过 {@code "spring.main.cloud-platform"} 配置属性判断是否强制指定平台。
	 * @param environment the environment
	 * @return 平台是否被强制指定
	 * @since 2.3.0
	 */
	public boolean isEnforced(Environment environment) {
		return isEnforced(environment.getProperty(PROPERTY_NAME));
	}

	/**
	 * Determines if the platform is enforced by looking at the
	 * {@code "spring.main.cloud-platform"} configuration property.
	 * @param binder 绑定器
	 * @return if the platform is enforced
	 * @since 2.4.0
	 */
	public boolean isEnforced(Binder binder) {
		return isEnforced(binder.bind(PROPERTY_NAME, String.class).orElse(null));
	}

	private boolean isEnforced(@Nullable String platform) {
		return name().equalsIgnoreCase(platform);
	}

	/**
	 * 通过查找平台特定环境变量判断是否检测到该平台。
	 * @param environment the environment
	 * @return 平台是否被自动检测
	 * @since 2.3.0
	 */
	public abstract boolean isDetected(Environment environment);

	/**
	 * 返回平台是否位于负载均衡器之后并使用 {@literal X-Forwarded-For} 头。
	 * @return 是否使用 {@literal X-Forwarded-For} 头
	 */
	public boolean isUsingForwardHeaders() {
		return true;
	}

	/**
	 * 返回激活的 {@link CloudPlatform}，若无激活平台则返回 {@code null}。
	 * @param environment the environment
	 * @return {@link CloudPlatform} 或 {@code null}
	 */
	public static @Nullable CloudPlatform getActive(@Nullable Environment environment) {
		if (environment != null) {
			for (CloudPlatform cloudPlatform : values()) {
				if (cloudPlatform.isActive(environment)) {
					return cloudPlatform;
				}
			}
		}
		return null;
	}

}
