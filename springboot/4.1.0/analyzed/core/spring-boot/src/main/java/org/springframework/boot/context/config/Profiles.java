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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import org.jspecify.annotations.Nullable;

import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;
import org.springframework.core.ResolvableType;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.style.ToStringCreator;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

/**
 * 提供对环境 profile 的访问，这些 profile 可直接设置在 {@link Environment} 上，
 * 或基于配置数据属性值确定。
 *
 * @author Phillip Webb
 * @author Madhura Bhave
 * @since 2.4.0
 */
public class Profiles implements Iterable<String> {

	/**
	 * 用于指定额外包含的活动 profile 的属性名。
	 */
	public static final String INCLUDE_PROFILES_PROPERTY_NAME = "spring.profiles.include";

	static final ConfigurationPropertyName INCLUDE_PROFILES = ConfigurationPropertyName
		.of(Profiles.INCLUDE_PROFILES_PROPERTY_NAME);

	private static final Bindable<MultiValueMap<String, String>> STRING_STRINGS_MAP = Bindable
		.of(ResolvableType.forClassWithGenerics(MultiValueMap.class, String.class, String.class));

	private static final Bindable<Set<String>> STRING_SET = Bindable.setOf(String.class);

	private final MultiValueMap<String, String> groups;

	private final List<String> activeProfiles;

	private final List<String> defaultProfiles;

	/**
	 * 基于 {@link Environment} 与 {@link Binder} 创建新的 {@link Profiles} 实例。
	 *
	 * @param environment 源环境
	 * @param binder 用于 profile 属性的绑定器
	 * @param additionalProfiles 额外的活动 profile
	 */
	Profiles(Environment environment, Binder binder, @Nullable Collection<String> additionalProfiles) {
		ProfilesValidator validator = ProfilesValidator.get(binder);
		if (additionalProfiles != null) {
			validator.validate(additionalProfiles, () -> "Invalid profile property value found in additional profiles");
		}
		this.groups = binder.bind("spring.profiles.group", STRING_STRINGS_MAP, validator)
			.orElseGet(LinkedMultiValueMap::new);
		this.activeProfiles = expandProfiles(getActivatedProfiles(environment, binder, validator, additionalProfiles));
		this.defaultProfiles = expandProfiles(getDefaultProfiles(environment, binder, validator));
	}

	private List<String> getActivatedProfiles(Environment environment, Binder binder, ProfilesValidator validator,
			@Nullable Collection<String> additionalProfiles) {
		return asUniqueItemList(getProfiles(environment, binder, validator, Type.ACTIVE), additionalProfiles);
	}

	private List<String> getDefaultProfiles(Environment environment, Binder binder, ProfilesValidator validator) {
		return asUniqueItemList(getProfiles(environment, binder, validator, Type.DEFAULT));
	}

	private Collection<String> getProfiles(Environment environment, Binder binder, ProfilesValidator validator,
			Type type) {
		String environmentPropertyValue = environment.getProperty(type.getName());
		Set<String> environmentPropertyProfiles = (!StringUtils.hasLength(environmentPropertyValue))
				? Collections.emptySet()
				: StringUtils.commaDelimitedListToSet(StringUtils.trimAllWhitespace(environmentPropertyValue));
		validator.validate(environmentPropertyProfiles,
				() -> "Invalid profile property value found in Environment under '%s'".formatted(type.getName()));
		Set<String> environmentProfiles = new LinkedHashSet<>(Arrays.asList(type.get(environment)));
		BindResult<Set<String>> boundProfiles = binder.bind(type.getName(), STRING_SET, validator);
		if (hasProgrammaticallySetProfiles(type, environmentPropertyValue, environmentPropertyProfiles,
				environmentProfiles)) {
			if (!type.isMergeWithEnvironmentProfiles() || !boundProfiles.isBound()) {
				return environmentProfiles;
			}
			return boundProfiles.map((bound) -> merge(environmentProfiles, bound)).get();
		}
		return boundProfiles.orElse(type.getDefaultValue());
	}

	private boolean hasProgrammaticallySetProfiles(Type type, @Nullable String environmentPropertyValue,
			Set<String> environmentPropertyProfiles, Set<String> environmentProfiles) {
		if (!StringUtils.hasLength(environmentPropertyValue)) {
			return !type.getDefaultValue().equals(environmentProfiles);
		}
		if (type.getDefaultValue().equals(environmentProfiles)) {
			return false;
		}
		return !environmentPropertyProfiles.equals(environmentProfiles);
	}

	private Set<String> merge(Set<String> environmentProfiles, Set<String> bound) {
		Set<String> result = new LinkedHashSet<>(environmentProfiles);
		result.addAll(bound);
		return result;
	}

	private List<String> expandProfiles(@Nullable List<String> profiles) {
		Deque<String> stack = new ArrayDeque<>();
		asReversedList(profiles).forEach(stack::push);
		Set<String> expandedProfiles = new LinkedHashSet<>();
		while (!stack.isEmpty()) {
			String current = stack.pop();
			if (expandedProfiles.add(current)) {
				asReversedList(this.groups.get(current)).forEach(stack::push);
			}
		}
		return asUniqueItemList(expandedProfiles);
	}

	private List<String> asReversedList(@Nullable List<String> list) {
		if (CollectionUtils.isEmpty(list)) {
			return Collections.emptyList();
		}
		List<String> reversed = new ArrayList<>(list);
		Collections.reverse(reversed);
		return reversed;
	}

	private List<String> asUniqueItemList(Collection<String> profiles) {
		return asUniqueItemList(profiles, null);
	}

	private List<String> asUniqueItemList(Collection<String> profiles, @Nullable Collection<String> additional) {
		LinkedHashSet<String> uniqueItems = new LinkedHashSet<>();
		if (!CollectionUtils.isEmpty(additional)) {
			uniqueItems.addAll(additional);
		}
		uniqueItems.addAll(profiles);
		return Collections.unmodifiableList(new ArrayList<>(uniqueItems));
	}

	/**
	 * 返回所有 {@link #getAccepted() 已接受 profile} 的迭代器。
	 */
	@Override
	public Iterator<String> iterator() {
		return getAccepted().iterator();
	}

	/**
	 * 返回活动 profile。
	 *
	 * @return 活动 profile
	 */
	public List<String> getActive() {
		return this.activeProfiles;
	}

	/**
	 * 返回默认 profile。
	 *
	 * @return 默认 profile
	 */
	public List<String> getDefault() {
		return this.defaultProfiles;
	}

	/**
	 * 返回已接受的 profile。
	 *
	 * @return 已接受的 profile
	 */
	public List<String> getAccepted() {
		return (!this.activeProfiles.isEmpty()) ? this.activeProfiles : this.defaultProfiles;
	}

	/**
	 * 判断给定 profile 是否已接受。
	 *
	 * @param profile 要测试的 profile
	 * @return profile 是否已接受
	 */
	public boolean isAccepted(String profile) {
		return getAccepted().contains(profile);
	}

	@Override
	public String toString() {
		ToStringCreator creator = new ToStringCreator(this);
		creator.append("active", getActive().toString());
		creator.append("default", getDefault().toString());
		creator.append("accepted", getAccepted().toString());
		return creator.toString();
	}

	/**
	 * 可获取的 profile 类型。
	 */
	private enum Type {

		ACTIVE(AbstractEnvironment.ACTIVE_PROFILES_PROPERTY_NAME, Environment::getActiveProfiles, true,
				Collections.emptySet()),

		DEFAULT(AbstractEnvironment.DEFAULT_PROFILES_PROPERTY_NAME, Environment::getDefaultProfiles, false,
				Collections.singleton("default"));

		private final Function<Environment, String[]> getter;

		private final boolean mergeWithEnvironmentProfiles;

		private final String name;

		private final Set<String> defaultValue;

		Type(String name, Function<Environment, String[]> getter, boolean mergeWithEnvironmentProfiles,
				Set<String> defaultValue) {
			this.name = name;
			this.getter = getter;
			this.mergeWithEnvironmentProfiles = mergeWithEnvironmentProfiles;
			this.defaultValue = defaultValue;
		}

		String getName() {
			return this.name;
		}

		String[] get(Environment environment) {
			return this.getter.apply(environment);
		}

		Set<String> getDefaultValue() {
			return this.defaultValue;
		}

		boolean isMergeWithEnvironmentProfiles() {
			return this.mergeWithEnvironmentProfiles;
		}

	}

}
