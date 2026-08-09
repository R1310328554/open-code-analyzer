package com.alibaba.arthas.spring;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.ConfigurableEnvironment;

import com.taobao.arthas.agent.attach.ArthasAgent;

/**
 * Spring Boot 自动配置：在应用启动时 attach Arthas Agent。
 * <p>
 * 当 {@code spring.arthas.enabled=true}（默认开启）时注册配置 Map 与 {@link ArthasAgent} Bean，
 * 将 {@link ArthasProperties} 与环境变量合并后交给 Agent 初始化。
 *
 * @author hengyunabc 2020-06-22
 */
@ConditionalOnProperty(name = "spring.arthas.enabled", matchIfMissing = true)
@EnableConfigurationProperties({ ArthasProperties.class })
public class ArthasConfiguration {
	private static final Logger logger = LoggerFactory.getLogger(ArthasConfiguration.class);

	/** Spring 环境，用于读取 {@code spring.application.name} 等属性。 */
	@Autowired
	ConfigurableEnvironment environment;

	/**
	 * <pre>
	 * 1. 提取所有以 arthas.* 开头的配置项，再统一转换为Arthas配置
	 * 2. 避免某些配置在新版本里支持，但在ArthasProperties里没有配置的情况。
	 * </pre>
	 */
	@ConfigurationProperties(prefix = "arthas")
	@ConditionalOnMissingBean(name="arthasConfigMap")
	@Bean
	/** 收集 {@code arthas.*} 前缀的配置项，供 Agent 与 Actuator 端点读取。 */
	public HashMap<String, String> arthasConfigMap() {
		return new HashMap<String, String>();
	}

	@ConditionalOnMissingBean
	@Bean
	/**
	 * 创建并初始化 Arthas Agent：规范化键名、补默认值、注入 appName，再 attach 到当前 JVM。
	 */
	public ArthasAgent arthasAgent(@Autowired @Qualifier("arthasConfigMap") Map<String, String> arthasConfigMap,
			@Autowired ArthasProperties arthasProperties) throws Throwable {
        arthasConfigMap = StringUtils.removeDashKey(arthasConfigMap);
        ArthasProperties.updateArthasConfigMapDefaultValue(arthasConfigMap);
        /**
         * @see org.springframework.boot.context.ContextIdApplicationContextInitializer#getApplicationId(ConfigurableEnvironment)
         */
        String appName = environment.getProperty("spring.application.name");
        if (arthasConfigMap.get("appName") == null && appName != null) {
            arthasConfigMap.put("appName", appName);
        }

		// 给配置全加上前缀
		Map<String, String> mapWithPrefix = new HashMap<String, String>(arthasConfigMap.size());
		for (Entry<String, String> entry : arthasConfigMap.entrySet()) {
			mapWithPrefix.put("arthas." + entry.getKey(), entry.getValue());
		}

		final ArthasAgent arthasAgent = new ArthasAgent(mapWithPrefix, arthasProperties.getHome(),
				arthasProperties.isSlientInit(), null);

		arthasAgent.init();
		logger.info("Arthas agent start success.");
		return arthasAgent;

	}
}
