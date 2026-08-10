/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.keycloak.quarkus.runtime.storage.database.liquibase;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.SAXParserFactory;

import org.keycloak.connections.jpa.updater.liquibase.conn.DefaultLiquibaseConnectionProvider;

import liquibase.Scope;
import liquibase.parser.ChangeLogParser;
import liquibase.parser.ChangeLogParserFactory;
import liquibase.parser.core.xml.XMLChangeLogSAXParser;
import liquibase.ui.LoggerUIService;
import org.jboss.logging.Logger;

/**
 * Quarkus Liquibase 连接提供方：自定义 Scope/UI 服务，并禁用 changelog XML 校验以加快启动。
 */
public class QuarkusLiquibaseConnectionProvider extends DefaultLiquibaseConnectionProvider {

    private static final Logger logger = Logger.getLogger(QuarkusLiquibaseConnectionProvider.class);


    @Override
    protected void baseLiquibaseInitialization() {

        // 使用自定义 Scope 初始化 Liquibase（Logger UI 服务）
        final Map<String, Object> scopeValues = new HashMap<>();
        scopeValues.put(Scope.Attr.ui.name(), new LoggerUIService());
        try {
            Scope.enter(scopeValues);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize Liquibase: " + e.getMessage(), e);
        }

        // 关闭 XML schema 校验，避免 Quarkus 原生镜像下 DTD 解析问题
        for (ChangeLogParser parser : ChangeLogParserFactory.getInstance().getParsers()) {
            if (parser instanceof XMLChangeLogSAXParser) {
                Method getSaxParserFactory = null;
                try {
                    getSaxParserFactory = XMLChangeLogSAXParser.class.getDeclaredMethod("getSaxParserFactory");
                    getSaxParserFactory.setAccessible(true);
                    SAXParserFactory saxParserFactory = (SAXParserFactory) getSaxParserFactory.invoke(parser);
                    saxParserFactory.setValidating(false);
                    saxParserFactory.setSchema(null);
                } catch (Exception e) {
                    logger.warnf("Failed to disable liquibase XML validations");
                } finally {
                    if (getSaxParserFactory != null) {
                        getSaxParserFactory.setAccessible(false);
                    }
                }
            }
        }
    }

    @Override
    public String getId() {
        return "quarkus";
    }

    @Override
    public int order() {
        return 100;
    }
}
