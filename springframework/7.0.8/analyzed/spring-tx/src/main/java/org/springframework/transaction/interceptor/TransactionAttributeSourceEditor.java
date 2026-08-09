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

package org.springframework.transaction.interceptor;

import java.beans.PropertyEditorSupport;
import java.util.Enumeration;
import java.util.Properties;

import org.springframework.beans.propertyeditors.PropertiesEditor;
import org.springframework.util.StringUtils;

/**
 * 将 String 转换为 {@link TransactionAttributeSource} 的属性编辑器。
 * 事务属性字符串须可由本包中的 {@link TransactionAttributeEditor} 解析。
 *
 * <p>字符串为属性语法，形式为：<br>
 * {@code <全限定类名>.<方法名>=<事务属性字符串>}
 *
 * <p>例如：<br>
 * {@code com.mycompany.mycode.MyClass.myMethod=PROPAGATION_MANDATORY,ISOLATION_DEFAULT}
 *
 * <p><b>注意：</b>指定类必须是定义方法的类；
 * 若实现接口，则为接口类名。
 *
 * <p>注意：将为给定名称注册所有重载方法。
 * 不支持显式注册特定重载方法。
 * 支持 "xxx*" 映射 &mdash; 例如 "notify*" 将匹配 "notify" 和 "notifyAll"。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 26.04.2003
 * @see TransactionAttributeEditor
 */
public class TransactionAttributeSourceEditor extends PropertyEditorSupport {

	@Override
	public void setAsText(String text) throws IllegalArgumentException {
		MethodMapTransactionAttributeSource source = new MethodMapTransactionAttributeSource();
		if (StringUtils.hasLength(text)) {
			// 使用 Properties 编辑器对整串进行分词。
			PropertiesEditor propertiesEditor = new PropertiesEditor();
			propertiesEditor.setAsText(text);
			Properties props = (Properties) propertiesEditor.getValue();

			// 得到 Properties 后逐个处理。
			TransactionAttributeEditor tae = new TransactionAttributeEditor();
			Enumeration<?> propNames = props.propertyNames();
			while (propNames.hasMoreElements()) {
				String name = (String) propNames.nextElement();
				String value = props.getProperty(name);
				// 将值转换为事务属性。
				tae.setAsText(value);
				TransactionAttribute attr = (TransactionAttribute) tae.getValue();
				// 注册名称与属性。
				source.addTransactionalMethod(name, attr);
			}
		}
		setValue(source);
	}

}
