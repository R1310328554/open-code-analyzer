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

package org.springframework.boot.autoconfigure.condition;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Condition;

/**
 * 当所有嵌套类条件均匹配时才匹配的 {@link Condition}。可用于创建组合条件，例如：
 *
 * <pre class="code">
 * static class OnJndiAndProperty extends AllNestedConditions {
 *
 *    OnJndiAndProperty() {
 *        super(ConfigurationPhase.PARSE_CONFIGURATION);
 *    }
 *
 *    &#064;ConditionalOnJndi()
 *    static class OnJndi {
 *    }
 *
 *    &#064;ConditionalOnProperty("something")
 *    static class OnProperty {
 *    }
 *
 * }
 * </pre>
 * <p>
 * 应根据所定义的条件指定
 * {@link org.springframework.context.annotation.ConfigurationCondition.ConfigurationPhase
 * ConfigurationPhase}。上例中所有条件均为静态且可提前评估，因此
 * {@code PARSE_CONFIGURATION} 是合适的选择。
 *
 * @author Phillip Webb
 * @since 1.3.0
 */
public abstract class AllNestedConditions extends AbstractNestedCondition {

	public AllNestedConditions(ConfigurationPhase configurationPhase) {
		super(configurationPhase);
	}

	@Override
	protected ConditionOutcome getFinalMatchOutcome(MemberMatchOutcomes memberOutcomes) {
		boolean match = hasSameSize(memberOutcomes.getMatches(), memberOutcomes.getAll());
		List<ConditionMessage> messages = new ArrayList<>();
		messages.add(ConditionMessage.forCondition("AllNestedConditions")
			.because(memberOutcomes.getMatches().size() + " matched " + memberOutcomes.getNonMatches().size()
					+ " did not"));
		for (ConditionOutcome outcome : memberOutcomes.getAll()) {
			messages.add(outcome.getConditionMessage());
		}
		return new ConditionOutcome(match, ConditionMessage.of(messages));
	}

	private boolean hasSameSize(List<?> list1, List<?> list2) {
		return list1.size() == list2.size();
	}

}
