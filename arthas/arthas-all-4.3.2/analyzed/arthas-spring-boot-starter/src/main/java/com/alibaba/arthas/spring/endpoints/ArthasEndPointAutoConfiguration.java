package com.alibaba.arthas.spring.endpoints;

import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/**
 * 在 Actuator 可用且未自定义 Bean 时注册 {@link ArthasEndPoint}。
 * <p>
 * 与 {@link com.alibaba.arthas.spring.ArthasConfiguration} 共用 {@code spring.arthas.enabled} 开关。
 *
 * @author hengyunabc 2020-06-24
 */
@ConditionalOnProperty(name = "spring.arthas.enabled", matchIfMissing = true)
public class ArthasEndPointAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnAvailableEndpoint
	/** 注册 Actuator 只读端点 Bean。 */
	public ArthasEndPoint arthasEndPoint() {
		return new ArthasEndPoint();
	}
}
