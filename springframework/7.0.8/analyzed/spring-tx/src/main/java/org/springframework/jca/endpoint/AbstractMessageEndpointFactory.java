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

package org.springframework.jca.endpoint;

import java.lang.reflect.Method;

import javax.transaction.xa.XAResource;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.ApplicationServerInternalException;
import jakarta.resource.spi.UnavailableException;
import jakarta.resource.spi.endpoint.MessageEndpoint;
import jakarta.resource.spi.endpoint.MessageEndpointFactory;
import jakarta.transaction.Transaction;
import jakarta.transaction.TransactionManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.transaction.jta.SimpleTransactionFactory;
import org.springframework.transaction.jta.TransactionFactory;
import org.springframework.util.Assert;

/**
 * JCA 1.7 {@link jakarta.resource.spi.endpoint.MessageEndpointFactory} 接口的
 * 抽象基类实现，提供事务管理能力以及端点调用时的 ClassLoader 暴露。
 *
 * @author Juergen Hoeller
 * @since 2.5
 * @see #setTransactionManager
 */
public abstract class AbstractMessageEndpointFactory implements MessageEndpointFactory, BeanNameAware {

	/** 子类可用的 Logger。 */
	protected final Log logger = LogFactory.getLog(getClass());

	private @Nullable TransactionFactory transactionFactory;

	private @Nullable String transactionName;

	private int transactionTimeout = -1;

	private @Nullable String beanName;


	/**
	 * 设置用于包装端点调用的 XA 事务管理器，
	 * 在每次此类事务中登记端点资源。
	 * <p>传入对象可以是实现 Spring
	 * {@link org.springframework.transaction.jta.TransactionFactory} 接口的
	 * 事务管理器，或普通 {@link jakarta.transaction.TransactionManager}。
	 * <p>若未指定事务管理器，端点调用将不会包装在 XA 事务中。
	 * 请查阅资源提供者 ActivationSpec 文档了解特定提供者的本地事务选项。
	 * @see #setTransactionName
	 * @see #setTransactionTimeout
	 */
	public void setTransactionManager(Object transactionManager) {
		if (transactionManager instanceof TransactionFactory factory) {
			this.transactionFactory = factory;
		}
		else if (transactionManager instanceof TransactionManager manager) {
			this.transactionFactory = new SimpleTransactionFactory(manager);
		}
		else {
			throw new IllegalArgumentException("Transaction manager [" + transactionManager +
					"] is neither a [org.springframework.transaction.jta.TransactionFactory} nor a " +
					"[jakarta.transaction.TransactionManager]");
		}
	}

	/**
	 * 设置用于包装端点调用的 Spring TransactionFactory，
	 * 在每次此类事务中登记端点资源。
	 * <p>也可通过 {@link #setTransactionManager "transactionManager"} 属性
	 * 指定合适的事务管理器。
	 * <p>若未指定事务工厂，端点调用将不会包装在 XA 事务中。 Check out your
	 * resource provider's ActivationSpec documentation for local
	 * transaction options of your particular provider.
	 * @see #setTransactionName
	 * @see #setTransactionTimeout
	 */
	public void setTransactionFactory(TransactionFactory transactionFactory) {
		this.transactionFactory = transactionFactory;
	}

	/**
	 * 指定事务名称（若有）。
	 * <p>默认为无。指定名称将传递给事务管理器，
	 * 以便在事务监视器中识别该事务。
	 */
	public void setTransactionName(String transactionName) {
		this.transactionName = transactionName;
	}

	/**
	 * 指定事务超时（若有）。
	 * <p>默认为 -1：依赖事务管理器的默认超时。
	 * 指定具体超时以限制每次端点调用的最大持续时间。
	 */
	public void setTransactionTimeout(int transactionTimeout) {
		this.transactionTimeout = transactionTimeout;
	}

	/**
	 * 设置本消息端点的名称。在 Spring Bean 工厂中定义时自动填充 Bean 名称。
	 */
	@Override
	public void setBeanName(String beanName) {
		this.beanName = beanName;
	}


	/**
	 * JCA 1.7 {@code #getActivationName()} 方法的实现，
	 * 返回本 MessageEndpointFactory 上设置的 Bean 名称。
	 * @see #setBeanName
	 */
	@Override
	public @Nullable String getActivationName() {
		return this.beanName;
	}

	/**
	 * JCA 1.7 {@code #getEndpointClass()} 方法的实现，
	 * 返回 {@code null} 以表示合成端点类型。
	 */
	@Override
	public @Nullable Class<?> getEndpointClass() {
		return null;
	}

	/**
	 * 若已指定事务管理器则返回 {@code true}；否则返回 {@code false}。
	 * @see #setTransactionManager
	 * @see #setTransactionFactory
	 */
	@Override
	public boolean isDeliveryTransacted(Method method) throws NoSuchMethodException {
		return (this.transactionFactory != null);
	}

	/**
	 * 标准 JCA 1.5 版本的 {@code createEndpoint}。
	 * <p>本实现委托 {@link #createEndpointInternal()}，
	 * 在端点被调用前初始化端点的 XAResource。
	 */
	@Override
	public MessageEndpoint createEndpoint(XAResource xaResource) throws UnavailableException {
		AbstractMessageEndpoint endpoint = createEndpointInternal();
		endpoint.initXAResource(xaResource);
		return endpoint;
	}

	/**
	 * JCA 1.6 替代版本的 {@code createEndpoint}。
	 * <p>本实现委托 {@link #createEndpointInternal()}，
	 * 忽略指定超时。仅用于 JCA 1.6 合规。
	 */
	@Override
	public MessageEndpoint createEndpoint(XAResource xaResource, long timeout) throws UnavailableException {
		AbstractMessageEndpoint endpoint = createEndpointInternal();
		endpoint.initXAResource(xaResource);
		return endpoint;
	}

	/**
	 * 创建实际端点实例，作为本工厂 {@link AbstractMessageEndpoint} 内部类的子类。
	 * @return 实际端点实例（永不为 {@code null}）
	 * @throws UnavailableException 当前无可用端点时
	 */
	protected abstract AbstractMessageEndpoint createEndpointInternal() throws UnavailableException;


	/**
	 * 实际端点实现的内部类，基于模板方法以支持任意具体端点实现。
	 */
	protected abstract class AbstractMessageEndpoint implements MessageEndpoint {

		private @Nullable TransactionDelegate transactionDelegate;

		private boolean beforeDeliveryCalled = false;

		private @Nullable ClassLoader previousContextClassLoader;

		/**
		 * 初始化本端点的 TransactionDelegate。
		 * @param xaResource 本端点的 XAResource
		 */
		void initXAResource(XAResource xaResource) {
			this.transactionDelegate = new TransactionDelegate(xaResource);
		}

		/**
		 * 本 {@code beforeDelivery} 实现必要时启动事务，
		 * 并将端点 ClassLoader 暴露为当前线程上下文 ClassLoader。
		 * <p>注意，JCA 1.7 规范不要求 ResourceAdapter
		 * 在调用具体端点前调用此方法。若未调用（检查 {@link #hasBeforeDeliveryBeenCalled()}），
		 * 具体端点方法应在其自身处理中显式调用 {@code beforeDelivery}
		 * 及其对应方法 {@link #afterDelivery()}。
		 */
		@Override
		public void beforeDelivery(@Nullable Method method) throws ResourceException {
			this.beforeDeliveryCalled = true;
			Assert.state(this.transactionDelegate != null, "Not initialized");
			try {
				this.transactionDelegate.beginTransaction();
			}
			catch (Throwable ex) {
				throw new ApplicationServerInternalException("Failed to begin transaction", ex);
			}
			Thread currentThread = Thread.currentThread();
			this.previousContextClassLoader = currentThread.getContextClassLoader();
			currentThread.setContextClassLoader(getEndpointClassLoader());
		}

		/**
		 * 暴露端点 ClassLoader 的模板方法
		 *（通常为消息监听器类加载时使用的 ClassLoader）。
		 * @return 端点 ClassLoader（永不为 {@code null}）
		 */
		protected abstract ClassLoader getEndpointClassLoader();

		/**
		 * 返回本端点的 {@link #beforeDelivery} 方法是否已被调用。
		 */
		protected final boolean hasBeforeDeliveryBeenCalled() {
			return this.beforeDeliveryCalled;
		}

		/**
		 * 通知端点基类具体端点调用导致异常的回调方法。
		 * <p>在具体端点抛出异常时由子类调用。
		 * @param ex 具体端点抛出的异常
		 */
		protected void onEndpointException(Throwable ex) {
			Assert.state(this.transactionDelegate != null, "Not initialized");
			this.transactionDelegate.setRollbackOnly();
			logger.debug("Transaction marked as rollback-only after endpoint exception", ex);
		}

		/**
		 * 本 {@code afterDelivery} 实现重置线程上下文 ClassLoader
		 * 并完成事务（若有）。
		 * <p>Note that the JCA 1.7 specification does not require a ResourceAdapter
		 * to call this method after invoking the concrete endpoint. See the
		 * explanation in {@link #beforeDelivery}'s javadoc.
		 */
		@Override
		public void afterDelivery() throws ResourceException {
			Assert.state(this.transactionDelegate != null, "Not initialized");
			this.beforeDeliveryCalled = false;
			Thread.currentThread().setContextClassLoader(this.previousContextClassLoader);
			this.previousContextClassLoader = null;
			try {
				this.transactionDelegate.endTransaction();
			}
			catch (Throwable ex) {
				logger.warn("Failed to complete transaction after endpoint delivery", ex);
				throw new ApplicationServerInternalException("Failed to complete transaction", ex);
			}
		}

		@Override
		public void release() {
			if (this.transactionDelegate != null) {
				try {
					this.transactionDelegate.setRollbackOnly();
					this.transactionDelegate.endTransaction();
				}
				catch (Throwable ex) {
					logger.warn("Could not complete unfinished transaction on endpoint release", ex);
				}
			}
		}
	}


	/**
	 * 执行实际事务处理（包括登记端点 XAResource）的私有内部类。
	 */
	private class TransactionDelegate {

		private final @Nullable XAResource xaResource;

		private @Nullable Transaction transaction;

		private boolean rollbackOnly;

		public TransactionDelegate(@Nullable XAResource xaResource) {
			if (xaResource == null && transactionFactory != null &&
					!transactionFactory.supportsResourceAdapterManagedTransactions()) {
				throw new IllegalStateException("ResourceAdapter-provided XAResource is required for " +
						"transaction management. Check your ResourceAdapter's configuration.");
			}
			this.xaResource = xaResource;
		}

		public void beginTransaction() throws Exception {
			if (transactionFactory != null && this.xaResource != null) {
				this.transaction = transactionFactory.createTransaction(transactionName, transactionTimeout);
				this.transaction.enlistResource(this.xaResource);
			}
		}

		public void setRollbackOnly() {
			if (this.transaction != null) {
				this.rollbackOnly = true;
			}
		}

		public void endTransaction() throws Exception {
			if (this.transaction != null) {
				try {
					if (this.rollbackOnly) {
						this.transaction.rollback();
					}
					else {
						this.transaction.commit();
					}
				}
				finally {
					this.transaction = null;
					this.rollbackOnly = false;
				}
			}
		}
	}

}
