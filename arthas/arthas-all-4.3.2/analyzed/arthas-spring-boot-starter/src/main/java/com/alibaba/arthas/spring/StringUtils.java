package com.alibaba.arthas.spring;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Spring Boot 配置键名工具：将 kebab-case（{@code telnet-port}）转为 camelCase（{@code telnetPort}）。
 * <p>
 * {@link ConfigurationProperties} 绑定后的 Map 键可能含连字符，需与 Arthas 内部配置键对齐。
 *
 * @author hengyunabc 2020-06-24
 */
public class StringUtils {

	/**
	 * 遍历配置 Map，将键名中的 {@code -x} 转为驼峰 {@code X}，值保持不变。
	 *
	 * @param map 原始配置键值对
	 * @return 键名已规范化的新 Map
	 */
	public static Map<String, String> removeDashKey(Map<String, String> map) {
		Map<String, String> result = new HashMap<String, String>(map.size());

		for (Entry<String, String> entry : map.entrySet()) {
			String key = entry.getKey();

			// 含连字符时逐字符扫描，连字符后首字母大写并跳过连字符
			if (key.contains("-")) {

				StringBuilder sb = new StringBuilder(key.length());
				for (int i = 0; i < key.length(); i++) {
					if (key.charAt(i) == '-' && (i + 1 < key.length()) && Character.isAlphabetic(key.charAt(i + 1))) {
						++i;
						char upperChar = Character.toUpperCase(key.charAt(i));
						sb.append(upperChar);
					} else {
						sb.append(key.charAt(i));
					}
				}
				key = sb.toString();
			}

			result.put(key, entry.getValue());
		}

		return result;
	}

}
