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
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

/**
 * 将条件评估详情记录用于报告和日志。
 *
 * @author Greg Turnquist
 * @author Dave Syer
 * @author Phillip Webb
 * @author Andy Wilkinson
 * @author Stephane Nicoll
 * @since 1.0.0
 */
public final class ConditionEvaluationReport {

	private static final String BEAN_NAME = "autoConfigurationReport";

	private static final AncestorsMatchedCondition ANCESTOR_CONDITION = new AncestorsMatchedCondition();

	private final SortedMap<String, ConditionAndOutcomes> outcomes = new TreeMap<>();

	private boolean addedAncestorOutcomes;

	private @Nullable ConditionEvaluationReport parent;

	private final List<String> exclusions = new ArrayList<>();

	private final Set<String> unconditionalClasses = new HashSet<>();

	/**
	 * 私有构造器。
	 * @see #get(ConfigurableListableBeanFactory)
	 */
	private ConditionEvaluationReport() {
	}

	/**
	 * 记录条件评估的发生。
	 * @param source 条件来源（类名或方法名）
	 * @param condition 被评估的条件
	 * @param outcome 条件结果
	 */
	public void recordConditionEvaluation(String source, Condition condition, ConditionOutcome outcome) {
		Assert.notNull(source, "'source' must not be null");
		Assert.notNull(condition, "'condition' must not be null");
		Assert.notNull(outcome, "'outcome' must not be null");
		this.unconditionalClasses.remove(source);
		this.outcomes.computeIfAbsent(source, (key) -> new ConditionAndOutcomes()).add(condition, outcome);
		this.addedAncestorOutcomes = false;
	}

	/**
	 * 记录被排除在条件评估之外的类名。
	 * @param exclusions 被排除的类名
	 */
	public void recordExclusions(Collection<String> exclusions) {
		Assert.notNull(exclusions, "'exclusions' must not be null");
		this.exclusions.addAll(exclusions);
	}

	/**
	 * 记录作为条件评估候选的类名。
	 * @param evaluationCandidates 将进行条件评估的类名
	 */
	public void recordEvaluationCandidates(List<String> evaluationCandidates) {
		Assert.notNull(evaluationCandidates, "'evaluationCandidates' must not be null");
		this.unconditionalClasses.addAll(evaluationCandidates);
	}

	/**
	 * 返回本报告中按来源分组的条件结果。
	 * @return 条件结果
	 */
	public Map<String, ConditionAndOutcomes> getConditionAndOutcomesBySource() {
		if (!this.addedAncestorOutcomes) {
			this.outcomes.forEach((source, sourceOutcomes) -> {
				if (!sourceOutcomes.isFullMatch()) {
					addNoMatchOutcomeToAncestors(source);
				}
			});
			this.addedAncestorOutcomes = true;
		}
		return Collections.unmodifiableMap(this.outcomes);
	}

	private void addNoMatchOutcomeToAncestors(String source) {
		String prefix = source + "$";
		this.outcomes.forEach((candidateSource, sourceOutcomes) -> {
			if (candidateSource.startsWith(prefix)) {
				ConditionOutcome outcome = ConditionOutcome
					.noMatch(ConditionMessage.forCondition("Ancestor " + source).because("did not match"));
				sourceOutcomes.add(ANCESTOR_CONDITION, outcome);
			}
		});
	}

	/**
	 * 返回被排除在条件评估之外的类名。
	 * @return 被排除的类名
	 */
	public List<String> getExclusions() {
		return Collections.unmodifiableList(this.exclusions);
	}

	/**
	 * 返回已评估但无条件约束的类名。
	 * @return 无条件类的类名
	 */
	public Set<String> getUnconditionalClasses() {
		Set<String> filtered = new HashSet<>(this.unconditionalClasses);
		this.exclusions.forEach(filtered::remove);
		return Collections.unmodifiableSet(filtered);
	}

	/**
	 * 父级报告（若存在父 BeanFactory 则来自父级）。
	 * @return 父级报告（若无则为 null）
	 */
	public @Nullable ConditionEvaluationReport getParent() {
		return this.parent;
	}

	/**
	 * 尝试查找指定 BeanFactory 的 {@link ConditionEvaluationReport}。
	 * @param beanFactory BeanFactory（可为 {@code null}）
	 * @return {@link ConditionEvaluationReport} 或 {@code null}
	 */
	public static @Nullable ConditionEvaluationReport find(BeanFactory beanFactory) {
		if (beanFactory instanceof ConfigurableListableBeanFactory configurableListableBeanFactory) {
			return ConditionEvaluationReport.get(configurableListableBeanFactory);
		}
		return null;
	}

	/**
	 * 获取指定 BeanFactory 的 {@link ConditionEvaluationReport}。
	 * @param beanFactory BeanFactory
	 * @return 已有的或新建的 {@link ConditionEvaluationReport}
	 */
	public static ConditionEvaluationReport get(ConfigurableListableBeanFactory beanFactory) {
		synchronized (beanFactory) {
			ConditionEvaluationReport report;
			if (beanFactory.containsSingleton(BEAN_NAME)) {
				report = beanFactory.getBean(BEAN_NAME, ConditionEvaluationReport.class);
			}
			else {
				report = new ConditionEvaluationReport();
				beanFactory.registerSingleton(BEAN_NAME, report);
			}
			locateParent(beanFactory.getParentBeanFactory(), report);
			return report;
		}
	}

	private static void locateParent(@Nullable BeanFactory beanFactory, ConditionEvaluationReport report) {
		if (beanFactory != null && report.parent == null && beanFactory.containsBean(BEAN_NAME)) {
			report.parent = beanFactory.getBean(BEAN_NAME, ConditionEvaluationReport.class);
		}
	}

	public ConditionEvaluationReport getDelta(ConditionEvaluationReport previousReport) {
		ConditionEvaluationReport delta = new ConditionEvaluationReport();
		this.outcomes.forEach((source, sourceOutcomes) -> {
			ConditionAndOutcomes previous = previousReport.outcomes.get(source);
			if (previous == null || previous.isFullMatch() != sourceOutcomes.isFullMatch()) {
				sourceOutcomes.forEach((conditionAndOutcome) -> delta.recordConditionEvaluation(source,
						conditionAndOutcome.getCondition(), conditionAndOutcome.getOutcome()));
			}
		});
		List<String> newExclusions = new ArrayList<>(this.exclusions);
		newExclusions.removeAll(previousReport.getExclusions());
		delta.recordExclusions(newExclusions);
		List<String> newUnconditionalClasses = new ArrayList<>(this.unconditionalClasses);
		newUnconditionalClasses.removeAll(previousReport.unconditionalClasses);
		delta.unconditionalClasses.addAll(newUnconditionalClasses);
		return delta;
	}

	/**
	 * 提供对多个 {@link ConditionAndOutcome} 项的访问。
	 */
	public static class ConditionAndOutcomes implements Iterable<ConditionAndOutcome> {

		private final Set<ConditionAndOutcome> outcomes = new LinkedHashSet<>();

		public void add(Condition condition, ConditionOutcome outcome) {
			this.outcomes.add(new ConditionAndOutcome(condition, outcome));
		}

		/**
		 * 若所有结果均匹配则返回 {@code true}。
		 * @return 若完全匹配则为 {@code true}
		 */
		public boolean isFullMatch() {
			for (ConditionAndOutcome conditionAndOutcomes : this) {
				if (!conditionAndOutcomes.getOutcome().isMatch()) {
					return false;
				}
			}
			return true;
		}

		/**
		 * 返回 {@link ConditionAndOutcome} 项的 {@link Stream}。
		 * @return {@link ConditionAndOutcome} 项的流
		 * @since 3.5.0
		 */
		public Stream<ConditionAndOutcome> stream() {
			return StreamSupport.stream(spliterator(), false);
		}

		@Override
		public Iterator<ConditionAndOutcome> iterator() {
			return Collections.unmodifiableSet(this.outcomes).iterator();
		}

	}

	/**
	 * 提供对单个 {@link Condition} 与 {@link ConditionOutcome} 的访问。
	 */
	public static class ConditionAndOutcome {

		private final Condition condition;

		private final ConditionOutcome outcome;

		public ConditionAndOutcome(Condition condition, ConditionOutcome outcome) {
			this.condition = condition;
			this.outcome = outcome;
		}

		public Condition getCondition() {
			return this.condition;
		}

		public ConditionOutcome getOutcome() {
			return this.outcome;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null || getClass() != obj.getClass()) {
				return false;
			}
			ConditionAndOutcome other = (ConditionAndOutcome) obj;
			return (ObjectUtils.nullSafeEquals(this.condition.getClass(), other.condition.getClass())
					&& ObjectUtils.nullSafeEquals(this.outcome, other.outcome));
		}

		@Override
		public int hashCode() {
			return this.condition.getClass().hashCode() * 31 + this.outcome.hashCode();
		}

		@Override
		public String toString() {
			return this.condition.getClass() + " " + this.outcome;
		}

	}

	private static final class AncestorsMatchedCondition implements Condition {

		@Override
		public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
			throw new UnsupportedOperationException();
		}

	}

}
