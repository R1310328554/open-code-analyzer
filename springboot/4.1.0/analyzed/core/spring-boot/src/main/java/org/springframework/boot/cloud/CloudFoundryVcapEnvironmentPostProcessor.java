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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.jspecify.annotations.Nullable;

import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.boot.logging.DeferredLogFactory;
import org.springframework.core.Ordered;
import org.springframework.core.env.CommandLinePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.util.StringUtils;

/**
 * 知道如何在现有环境中查找 VCAP（即 Cloud Foundry）元数据的 {@link EnvironmentPostProcessor}。 它解析 VCAP_APPLICATION 与 VCAP_SERVICES 元数据，
 * 并以 {@link Environment} 使用者易于消费的形式输出。 若应用在 Cloud Foundry 中运行，这两项元数据均为编码在 OS 环境变量中的 JSON 对象。 VCAP_APPLICATION 是包含应用基本信息（名称、实例 ID、实例索引等）的浅层哈希；
 * VCAP_SERVICES 是以服务标签为键、服务实例元数据哈希列表为值的哈希。示例如下：
 *
 * <pre class="code">
 * VCAP_APPLICATION: {"instance_id":"2ce0ac627a6c8e47e936d829a3a47b5b","instance_index":0,
 *   "version":"0138c4a6-2a73-416b-aca0-572c09f7ca53","name":"foo",
 *   "uris":["foo.cfapps.io"], ...}
 * VCAP_SERVICES: {"rds-mysql-1.0":[{"name":"mysql","label":"rds-mysql-1.0","plan":"10mb",
 *   "credentials":{"name":"d04fb13d27d964c62b267bbba1cffb9da","hostname":"mysql-service-public.clqg2e2w3ecf.us-east-1.rds.amazonaws.com",
 *   "host":"mysql-service-public.clqg2e2w3ecf.us-east-1.rds.amazonaws.com","port":3306,"user":"urpRuqTf8Cpe6",
 *   "username":"urpRuqTf8Cpe6","password":"pxLsGVpsC9A5S"}
 * }]}
 * </pre>
 *
 * 这些对象被扁平化为属性。VCAP_APPLICATION 直接映射为 {@code vcap.application.*}；
 * VCAP_SERVICES 先展开为以服务实例名（如上例中的 "mysql"）为键、实例属性为值的哈希，
 * 再同样扁平化。例如：
 *
 * <pre class="code">
 * vcap.application.instance_id: 2ce0ac627a6c8e47e936d829a3a47b5b
 * vcap.application.version: 0138c4a6-2a73-416b-aca0-572c09f7ca53
 * vcap.application.name: foo
 * vcap.application.uris[0]: foo.cfapps.io
 *
 * vcap.services.mysql.name: mysql
 * vcap.services.mysql.label: rds-mysql-1.0
 * vcap.services.mysql.credentials.name: d04fb13d27d964c62b267bbba1cffb9da
 * vcap.services.mysql.credentials.port: 3306
 * vcap.services.mysql.credentials.host: mysql-service-public.clqg2e2w3ecf.us-east-1.rds.amazonaws.com
 * vcap.services.mysql.credentials.username: urpRuqTf8Cpe6
 * vcap.services.mysql.credentials.password: pxLsGVpsC9A5S
 * ...
 * </pre>
 *
 * 注意：此初始化器主要用于信息用途（应用 ID 与实例 ID 尤其有用）。
 * 服务绑定时 Spring Cloud 通常更便捷，且更能适应 Cloud Foundry 的潜在变更。
 *
 * @author Dave Syer
 * @author Andy Wilkinson
 * @since 1.3.0
 */
public class CloudFoundryVcapEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

	private final Log logger;

	// Before ConfigDataEnvironmentPostProcessor so values there can use these
	private int order = ConfigDataEnvironmentPostProcessor.ORDER - 5;

	/**
	 * 创建新的 {@link CloudFoundryVcapEnvironmentPostProcessor} 实例。
	 * @param logFactory 要使用的日志工厂
	 * @since 3.0.0
	 */
	public CloudFoundryVcapEnvironmentPostProcessor(DeferredLogFactory logFactory) {
		this.logger = logFactory.getLog(CloudFoundryVcapEnvironmentPostProcessor.class);
	}

	public void setOrder(int order) {
		this.order = order;
	}

	@Override
	public int getOrder() {
		return this.order;
	}

	@Override
	public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
		if (CloudPlatform.CLOUD_FOUNDRY.isActive(environment)) {
			Properties properties = new Properties();
			JsonParser jsonParser = JsonParserFactory.getJsonParser();
			addWithPrefix(properties, getPropertiesFromApplication(environment, jsonParser), "vcap.application.");
			addWithPrefix(properties, getPropertiesFromServices(environment, jsonParser), "vcap.services.");
			MutablePropertySources propertySources = environment.getPropertySources();
			PropertiesPropertySource vcapSource = new PropertiesPropertySource("vcap", properties);
			if (propertySources.contains(CommandLinePropertySource.COMMAND_LINE_PROPERTY_SOURCE_NAME)) {
				propertySources.addAfter(CommandLinePropertySource.COMMAND_LINE_PROPERTY_SOURCE_NAME, vcapSource);
			}
			else {
				propertySources.addFirst(vcapSource);
			}
		}
	}

	private void addWithPrefix(Properties properties, Properties other, String prefix) {
		for (String key : other.stringPropertyNames()) {
			String prefixed = prefix + key;
			properties.setProperty(prefixed, other.getProperty(key));
		}
	}

	private Properties getPropertiesFromApplication(Environment environment, JsonParser parser) {
		Properties properties = new Properties();
		try {
			String property = environment.getProperty("VCAP_APPLICATION", "{}");
			Map<String, Object> map = parser.parseMap(property);
			extractPropertiesFromApplication(properties, map);
		}
		catch (Exception ex) {
			this.logger.error("Could not parse VCAP_APPLICATION", ex);
		}
		return properties;
	}

	private Properties getPropertiesFromServices(Environment environment, JsonParser parser) {
		Properties properties = new Properties();
		try {
			String property = environment.getProperty("VCAP_SERVICES", "{}");
			Map<String, Object> map = parser.parseMap(property);
			extractPropertiesFromServices(properties, map);
		}
		catch (Exception ex) {
			this.logger.error("Could not parse VCAP_SERVICES", ex);
		}
		return properties;
	}

	private void extractPropertiesFromApplication(Properties properties, @Nullable Map<String, Object> map) {
		if (map != null) {
			flatten(properties, map, "");
		}
	}

	private void extractPropertiesFromServices(Properties properties, @Nullable Map<String, Object> map) {
		if (map != null) {
			for (Object services : map.values()) {
				@SuppressWarnings("unchecked")
				List<Object> list = (List<Object>) services;
				for (Object object : list) {
					@SuppressWarnings("unchecked")
					Map<String, Object> service = (Map<String, Object>) object;
					String key = (String) service.get("name");
					if (key == null) {
						key = (String) service.get("label");
					}
					flatten(properties, service, key);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void flatten(Properties properties, Map<String, Object> input, @Nullable String path) {
		input.forEach((key, value) -> {
			String name = getPropertyName(path, key);
			if (value instanceof Map) {
				// Need a compound key
				flatten(properties, (Map<String, Object>) value, name);
			}
			else if (value instanceof Collection<?> collection) {
				// Need a compound key
				properties.put(name, StringUtils.collectionToCommaDelimitedString(collection));
				int count = 0;
				for (Object item : collection) {
					String itemKey = "[" + (count++) + "]";
					flatten(properties, Collections.singletonMap(itemKey, item), name);
				}
			}
			else if (value instanceof String) {
				properties.put(name, value);
			}
			else if (value instanceof Number || value instanceof Boolean) {
				properties.put(name, value.toString());
			}
			else {
				properties.put(name, (value != null) ? value : "");
			}
		});
	}

	private String getPropertyName(@Nullable String path, String key) {
		if (!StringUtils.hasText(path)) {
			return key;
		}
		if (key.startsWith("[")) {
			return path + key;
		}
		return path + "." + key;
	}

}
