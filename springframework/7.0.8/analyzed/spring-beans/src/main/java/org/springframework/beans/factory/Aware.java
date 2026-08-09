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

package org.springframework.beans.factory;

/**
 * 标记型超级接口：表明 bean 有资格通过回调式方法，
 * 由 Spring 容器通知某个特定框架对象。
 * 实际方法签名由各个子接口决定，但通常是一个接受单个参数、返回 void 的方法。
 *
 * <p>注意：仅仅实现 {@link Aware} 并不会带来任何默认功能。
 * 处理必须显式完成，例如在
 * {@link org.springframework.beans.factory.config.BeanPostProcessor} 中。
 * 可参考 {@link org.springframework.context.support.ApplicationContextAwareProcessor}
 * 了解如何处理具体 {@code *Aware} 接口回调。
 *
 * @author Chris Beams
 * @author Juergen Hoeller
 * @since 3.1
 */
public interface Aware {

}
