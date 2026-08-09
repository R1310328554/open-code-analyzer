package com.alibaba.arthas.spring.endpoints;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

import com.taobao.arthas.agent.attach.ArthasAgent;

/**
 * Spring Boot Actuator 端点 {@code /actuator/arthas}：暴露 Arthas 配置与 Agent 错误信息。
 * <p>
 * 只读查询当前 {@link com.taobao.arthas.agent.attach.ArthasAgent} 状态，便于运维排查 attach 失败原因。
 *
 * @author hengyunabc 2020-06-24
 */
@Endpoint(id = "arthas")
public class ArthasEndPoint {

	/** 已 attach 的 Agent 实例；未启用或未初始化时为 null。 */
	@Autowired(required = false)
	private ArthasAgent arthasAgent;

	@Autowired(required = false)
	private HashMap<String, String> arthasConfigMap;

	/** 返回 {@code arthasConfigMap} 与 {@code errorMessage}（若 Agent 初始化失败）。 */
	@ReadOperation
	public Map<String, Object> invoke() {
		Map<String, Object> result = new HashMap<String, Object>();

		if (arthasConfigMap != null) {
			result.put("arthasConfigMap", arthasConfigMap);
		}

		String errorMessage = arthasAgent.getErrorMessage();
		if (errorMessage != null) {
			result.put("errorMessage", errorMessage);
		}

		return result;
	}

}
