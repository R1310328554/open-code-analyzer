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

package org.springframework.boot.info;

/**
 * 应用运行所在 Java 环境的信息。
 *
 * @author Jonatan Ivanov
 * @author Stephane Nicoll
 * @since 2.6.0
 */
public class JavaInfo {

	private final String version;

	private final JavaVendorInfo vendor;

	private final JavaRuntimeEnvironmentInfo runtime;

	private final JavaVirtualMachineInfo jvm;

	public JavaInfo() {
		this.version = System.getProperty("java.version");
		this.vendor = new JavaVendorInfo();
		this.runtime = new JavaRuntimeEnvironmentInfo();
		this.jvm = new JavaVirtualMachineInfo();
	}

	public String getVersion() {
		return this.version;
	}

	public JavaVendorInfo getVendor() {
		return this.vendor;
	}

	public JavaRuntimeEnvironmentInfo getRuntime() {
		return this.runtime;
	}

	public JavaVirtualMachineInfo getJvm() {
		return this.jvm;
	}

	/**
	 * 应用运行所在 Java Runtime 的 Java Vendor 信息。
	 *
	 * @since 2.7.0
	 */
	public static class JavaVendorInfo {

		private final String name;

		private final String version;

		public JavaVendorInfo() {
			this.name = System.getProperty("java.vendor");
			this.version = System.getProperty("java.vendor.version");
		}

		public String getName() {
			return this.name;
		}

		public String getVersion() {
			return this.version;
		}

	}

	/**
	 * 应用运行所在 Java Runtime Environment 的信息。
	 */
	public static class JavaRuntimeEnvironmentInfo {

		private final String name;

		private final String version;

		public JavaRuntimeEnvironmentInfo() {
			this.name = System.getProperty("java.runtime.name");
			this.version = System.getProperty("java.runtime.version");
		}

		public String getName() {
			return this.name;
		}

		public String getVersion() {
			return this.version;
		}

	}

	/**
	 * 应用运行所在 Java Virtual Machine 的信息。
	 */
	public static class JavaVirtualMachineInfo {

		private final String name;

		private final String vendor;

		private final String version;

		public JavaVirtualMachineInfo() {
			this.name = System.getProperty("java.vm.name");
			this.vendor = System.getProperty("java.vm.vendor");
			this.version = System.getProperty("java.vm.version");
		}

		public String getName() {
			return this.name;
		}

		public String getVendor() {
			return this.vendor;
		}

		public String getVersion() {
			return this.version;
		}

	}

}
