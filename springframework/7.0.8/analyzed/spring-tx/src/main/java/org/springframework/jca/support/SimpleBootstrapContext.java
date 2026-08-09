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

package org.springframework.jca.support;

import java.util.Timer;

import jakarta.resource.spi.BootstrapContext;
import jakarta.resource.spi.UnavailableException;
import jakarta.resource.spi.XATerminator;
import jakarta.resource.spi.work.WorkContext;
import jakarta.resource.spi.work.WorkManager;
import jakarta.transaction.TransactionSynchronizationRegistry;
import org.jspecify.annotations.Nullable;

import org.springframework.util.Assert;

/**
 * JCA 1.7 {@link jakarta.resource.spi.BootstrapContext} 接口的简单实现，
 * 用于在本地环境中引导 JCA ResourceAdapter。
 *
 * <p>委托给给定的 WorkManager 和 XATerminator（若有）。
 * 创建 {@code java.util.Timer} 的简单本地实例。
 *
 * @author Juergen Hoeller
 * @since 2.0.3
 * @see jakarta.resource.spi.ResourceAdapter#start(jakarta.resource.spi.BootstrapContext)
 * @see ResourceAdapterFactoryBean
 */
public class SimpleBootstrapContext implements BootstrapContext {

	private final @Nullable WorkManager workManager;

	private @Nullable XATerminator xaTerminator;

	private @Nullable TransactionSynchronizationRegistry transactionSynchronizationRegistry;


	/**
	 * 为给定 WorkManager 创建 SimpleBootstrapContext，
	 * 不提供 XATerminator。
	 * @param workManager 要使用的 JCA WorkManager（可为 {@code null}）
	 */
	public SimpleBootstrapContext(@Nullable WorkManager workManager) {
		this.workManager = workManager;
	}

	/**
	 * 为给定 WorkManager 和 XATerminator 创建 SimpleBootstrapContext。
	 * @param workManager 要使用的 JCA WorkManager（可为 {@code null}）
	 * @param xaTerminator 要使用的 JCA XATerminator（可为 {@code null}）
	 */
	public SimpleBootstrapContext(@Nullable WorkManager workManager, @Nullable XATerminator xaTerminator) {
		this.workManager = workManager;
		this.xaTerminator = xaTerminator;
	}

	/**
	 * 为给定 WorkManager、XATerminator 和 TransactionSynchronizationRegistry
	 * 创建 SimpleBootstrapContext。
	 * @param workManager 要使用的 JCA WorkManager（可为 {@code null}）
	 * @param xaTerminator 要使用的 JCA XATerminator（可为 {@code null}）
	 * @param transactionSynchronizationRegistry 要使用的 TransactionSynchronizationRegistry
	 * （可为 {@code null}）
	 * @since 5.0
	 */
	public SimpleBootstrapContext(@Nullable WorkManager workManager, @Nullable XATerminator xaTerminator,
			@Nullable TransactionSynchronizationRegistry transactionSynchronizationRegistry) {

		this.workManager = workManager;
		this.xaTerminator = xaTerminator;
		this.transactionSynchronizationRegistry = transactionSynchronizationRegistry;
	}


	@Override
	public WorkManager getWorkManager() {
		Assert.state(this.workManager != null, "No WorkManager available");
		return this.workManager;
	}

	@Override
	public @Nullable XATerminator getXATerminator() {
		return this.xaTerminator;
	}

	@Override
	public Timer createTimer() throws UnavailableException {
		return new Timer();
	}

	@Override
	public boolean isContextSupported(Class<? extends WorkContext> workContextClass) {
		return false;
	}

	@Override
	public @Nullable TransactionSynchronizationRegistry getTransactionSynchronizationRegistry() {
		return this.transactionSynchronizationRegistry;
	}

}
