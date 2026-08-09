/*
 * Copyright 2002-present the original author or authors.
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

package org.springframework.scheduling.support;

import io.micrometer.common.KeyValue;
import io.micrometer.common.docs.KeyName;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationConvention;
import io.micrometer.observation.docs.ObservationDocumentation;

/**
 * 针对 {@link org.springframework.scheduling.annotation.Scheduled 调度任务}
 * 执行观测的已文档化 {@link io.micrometer.common.KeyValue KeyValue}。
 *
 * <p>本类供自动化工具记录附加于 {@code @Scheduled} 观测的 KeyValue。
 *
 * @author Brian Clozel
 * @since 6.1
 */
public enum ScheduledTaskObservationDocumentation implements ObservationDocumentation {

	/**
	 * 对 {@link org.springframework.scheduling.annotation.Scheduled} 任务执行的观测。
	 */
	TASKS_SCHEDULED_EXECUTION {
		@Override
		public Class<? extends ObservationConvention<? extends Observation.Context>> getDefaultConvention() {
			return DefaultScheduledTaskObservationConvention.class;
		}
		@Override
		public KeyName[] getLowCardinalityKeyNames() {
			return LowCardinalityKeyNames.values();
		}
		@Override
		public KeyName[] getHighCardinalityKeyNames() {
			return new KeyName[] {};
		}
	};


	public enum LowCardinalityKeyNames implements KeyName {

		/**
		 * 调度任务所执行方法的名称。
		 */
		CODE_FUNCTION {
			@Override
			public String asString() {
				return "code.function";
			}
		},

		/**
		 * 拥有调度方法的目标类型的 {@link Class#getCanonicalName() 规范名称}。
		 */
		CODE_NAMESPACE {
			@Override
			public String asString() {
				return "code.namespace";
			}
		},

		/**
		 * 任务执行期间抛出的异常名称；若未抛出异常则为 {@value KeyValue#NONE_VALUE}。
		 */
		EXCEPTION {
			@Override
			public String asString() {
				return "exception";
			}
		},

		/**
		 * 调度任务执行的结果。
		 */
		OUTCOME {
			@Override
			public String asString() {
				return "outcome";
			}
		}

	}

}
