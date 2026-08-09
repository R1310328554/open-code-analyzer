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

package org.springframework.boot.autoconfigure;

import java.util.Collections;
import java.util.EventObject;
import java.util.List;
import java.util.Set;

/**
 * 导入自动配置类时触发的事件。
 *
 * @author Phillip Webb
 * @since 1.5.0
 */
public class AutoConfigurationImportEvent extends EventObject {

	/** 即将导入的自动配置候选类名列表。 */
	private final List<String> candidateConfigurations;

	/** 已应用的排除项。 */
	private final Set<String> exclusions;

	public AutoConfigurationImportEvent(Object source, List<String> candidateConfigurations, Set<String> exclusions) {
		super(source);
		this.candidateConfigurations = Collections.unmodifiableList(candidateConfigurations);
		this.exclusions = Collections.unmodifiableSet(exclusions);
	}

	/**
	 * 返回即将导入的自动配置候选类。
	 * @return 自动配置候选类
	 */
	public List<String> getCandidateConfigurations() {
		return this.candidateConfigurations;
	}

	/**
	 * 返回已应用的排除项。
	 * @return 已应用的排除项
	 */
	public Set<String> getExclusions() {
		return this.exclusions;
	}

}
