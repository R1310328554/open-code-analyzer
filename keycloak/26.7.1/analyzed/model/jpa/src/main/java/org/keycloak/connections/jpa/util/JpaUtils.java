/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.connections.jpa.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnitTransactionType;
import jakarta.persistence.ValidationMode;

import org.keycloak.connections.jpa.entityprovider.JpaEntityProvider;
import org.keycloak.models.KeycloakSession;

import org.hibernate.dialect.Dialect;
import org.hibernate.engine.jdbc.env.spi.IdentifierHelper;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.internal.SessionFactoryImpl;
import org.hibernate.jpa.boot.internal.ParsedPersistenceXmlDescriptor;
import org.hibernate.jpa.boot.spi.Bootstrap;
import org.hibernate.jpa.boot.spi.PersistenceUnitDescriptor;
import org.hibernate.jpa.boot.spi.PersistenceXmlParser;
import org.jboss.logging.Logger;

/**
 * JPA/Hibernate 工具类：持久化单元构建、命名查询加载、Schema 解析等。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class JpaUtils {

    /** Hibernate 默认 schema 配置键。 */
    public static final String HIBERNATE_DEFAULT_SCHEMA = "hibernate.default_schema";
    /** 原生 SQL 命名查询后缀标记。 */
    public static final String QUERY_NATIVE_SUFFIX = "[native]";
    /** JPQL 命名查询后缀标记。 */
    public static final String QUERY_JPQL_SUFFIX = "[jpql]";
    private static final Logger logger = Logger.getLogger(JpaUtils.class);

    /** 为原生查询拼接 schema 前缀（若 EMF 配置了 default schema）。 */
    public static String getTableNameForNativeQuery(String tableName, EntityManager em) {
        final Dialect dialect = em.getEntityManagerFactory().unwrap(SessionFactoryImpl.class).getJdbcServices().getDialect();
        IdentifierHelper identifierHelper = em.getEntityManagerFactory().unwrap(SessionFactoryImpl.class).getJdbcServices().getJdbcEnvironment().getIdentifierHelper();
        String schema = em.getEntityManagerFactory().unwrap(SessionFactoryImpl.class).getSessionFactoryOptions().getDefaultSchema();
        return (schema==null) ? tableName : identifierHelper.toIdentifier(schema).render(dialect) + "." + tableName;
    }

    private static List<ParsedPersistenceXmlDescriptor> transformPersistenceUnits(Collection<PersistenceUnitDescriptor> descriptors) {
        return descriptors.stream().map(descriptor -> (ParsedPersistenceXmlDescriptor) descriptor).collect(Collectors.toList());
    }

    /** 合并 persistence.xml 与 default-persistence.xml，注入扩展实体后构建 EMF。 */
    public static EntityManagerFactory createEntityManagerFactory(KeycloakSession session, String unitName, Map<String, Object> properties, boolean jta) {
        PersistenceUnitTransactionType txType = jta ? PersistenceUnitTransactionType.JTA : PersistenceUnitTransactionType.RESOURCE_LOCAL;
        PersistenceXmlParser parser = PersistenceXmlParser.create(properties);
        List<URL> urls = parser.getClassLoaderService().locateResources("META-INF/persistence.xml");

        List<ParsedPersistenceXmlDescriptor> persistenceUnits = urls.isEmpty() ? new ArrayList<>() : transformPersistenceUnits(parser.parse(urls).values());
        ParsedPersistenceXmlDescriptor defaultPersistenceUnit = transformPersistenceUnits(parser.parse(Collections.singletonList(JpaUtils.class.getClassLoader().getResource("default-persistence.xml")), txType)
                .values())
                .stream()
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Cannot find the file 'default-persistence.xml'"));
        persistenceUnits.add(defaultPersistenceUnit);

        for (ParsedPersistenceXmlDescriptor persistenceUnit : persistenceUnits) {
            if (persistenceUnit.getName().equals(unitName)) {
                List<Class<?>> providedEntities = getProvidedEntities(session);
                for (Class<?> entityClass : providedEntities) {
                    // 将 SPI 注册的额外实体类加入持久化单元
                    persistenceUnit.addClasses(entityClass.getName());
                }
                // 使用代理 ClassLoader 构建 EMF，使 Hibernate 能加载 SPI 提供的实体
                persistenceUnit.setTransactionType(txType);
                persistenceUnit.setValidationMode(ValidationMode.NONE.name());
                return Bootstrap.getEntityManagerFactoryBuilder(persistenceUnit, properties).build();
            }
        }
        throw new RuntimeException("Persistence unit '" + unitName + "' not found");
    }

    /**
     * 汇总所有已配置 {@link JpaEntityProvider} 注册的 JPA 实体类。
     *
     * @param session the keycloak session
     * @return 实体类列表（可能为空）
     */
    public static List<Class<?>> getProvidedEntities(KeycloakSession session) {
        List<Class<?>> providedEntityClasses = new ArrayList<>();
        // 收集全部 JpaEntityProvider SPI
        Set<JpaEntityProvider> entityProviders = session.getAllProviders(JpaEntityProvider.class);
        // 合并各 Provider 声明的实体
        for (JpaEntityProvider entityProvider : entityProviders) {
            providedEntityClasses.addAll(entityProvider.getEntities());
        }
        return providedEntityClasses;
    }

    /**
     * 根据 {@link JpaEntityProvider} 工厂 ID 生成自定义 Liquibase changelog 表名。
     * @param jpaEntityProviderFactoryId SPI 工厂标识
     * @return 表名，形如 {@code DATABASECHANGELOG_XXXX}
     */
    public static String getCustomChangelogTableName(String jpaEntityProviderFactoryId) {
        String upperCased = jpaEntityProviderFactoryId.toUpperCase();
        upperCased = upperCased.replaceAll("-", "_");
        upperCased = upperCased.replaceAll("[^A-Z_]", "");
        return "DATABASECHANGELOG_" + upperCased.substring(0, Math.min(10, upperCased.length()));
    }

    /**
     * 从 URL 加载 properties 文件。
     * @param url 资源 URL，可为 null
     * @return 加载后的 Properties，url 为 null 时返回 null
     */
    public static Properties loadSqlProperties(URL url) {
        if (url == null) {
            return null;
        }
        Properties props = new Properties();
        try (InputStream is = url.openStream()) {
            props.load(is);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return props;
    }

    /**
     * 在 queries 属性文件中查找查询键：依次尝试 {@code name[native]}、{@code name[jpql]}、{@code name}。
     * @param name 逻辑查询名
     * @param queries 属性文件
     * @return 匹配到的完整键名，未找到返回 null
     */
    private static String getQueryFromProperties(String name, Properties queries) {
        if (queries == null) {
            return null;
        }
        String nameFull = name + QUERY_NATIVE_SUFFIX;
        if (queries.containsKey(nameFull)) {
            return nameFull;
        }
        nameFull = name + QUERY_JPQL_SUFFIX;
        if (queries.containsKey(nameFull)) {
            return nameFull;
        }
        nameFull = name;
        if (queries.containsKey(nameFull)) {
            return nameFull;
        }
        return null;
    }

    /** 去掉查询键上的 {@link #QUERY_NATIVE_SUFFIX} 或 {@link #QUERY_JPQL_SUFFIX} 后缀。 */
    private static String getQueryShortName(String name) {
        if (name.endsWith(QUERY_NATIVE_SUFFIX)) {
            return name.substring(0, name.length() - QUERY_NATIVE_SUFFIX.length());
        } else if (name.endsWith(QUERY_JPQL_SUFFIX)) {
            return name.substring(0, name.length() - QUERY_JPQL_SUFFIX.length());
        } else {
            return name;
        }
    }

    /**
     * 加载数据库类型专属的命名查询（{@code META-INF/queries-{dbType}.properties}）。
     * <p>默认文件 {@code queries-default.properties} 提供全量查询，类型专属文件可覆盖部分条目。</p>
     * @param databaseType 数据库类型标识
     * @return 合并后的查询属性集
     */
    public static Properties loadSpecificNamedQueries(String databaseType) {
        URL specificUrl = JpaUtils.class.getClassLoader().getResource("META-INF/queries-" + databaseType + ".properties");

        Properties specificQueries = loadSqlProperties(specificUrl);
        Properties queries = new Properties();
        if (specificQueries == null) {
            return queries;
        }

        for (String queryNameFull : specificQueries.stringPropertyNames()) {
            String querySql = specificQueries.getProperty(queryNameFull);
            String queryName = getQueryShortName(queryNameFull);
            String specificQueryNameFull = getQueryFromProperties(queryName, specificQueries);

            if (specificQueryNameFull != null) {
                // 该查询在数据库专属文件中重新定义 → 采用专属版本
                queryNameFull = specificQueryNameFull;
                querySql = specificQueries.getProperty(queryNameFull);
            }

            queries.put(queryNameFull, querySql);
        }

        return queries;
    }

    /**
     * 向 Hibernate SessionFactory 注册命名查询（自动识别 native / JPQL）。
     *
     * @param queryName 查询名（可带后缀）
     * @param querySql SQL 或 JPQL 文本
     * @param entityManager 当前 EntityManager
     */
    public static void configureNamedQuery(String queryName, String querySql, EntityManager entityManager) {
        boolean isNative = queryName.endsWith(QUERY_NATIVE_SUFFIX);
        queryName = getQueryShortName(queryName);

        logger.tracef("adding query from properties files native=%b %s:%s", isNative, queryName, querySql);

        SessionFactoryImplementor sessionFactory = entityManager.getEntityManagerFactory().unwrap(SessionFactoryImplementor.class);

        if (isNative) {
            sessionFactory.addNamedQuery(queryName, entityManager.createNativeQuery(querySql));
        } else {
            sessionFactory.addNamedQuery(queryName, entityManager.createQuery(querySql));
        }
    }

    /** 将 JDBC {@code DatabaseProductName} 规范化为 queries 文件名中的 dbType。 */
    public static String getDatabaseType(String productName) {
        switch (productName) {
            case "Microsoft SQL Server":
            case "SQLOLEDB":
                return "mssql";
            case "EnterpriseDB":
                return "postgresql";
            default:
                return productName.toLowerCase();
        }
    }

    /**
     * 安全关闭 EntityManager，吞掉关闭异常并记录警告。
     * @param em 待关闭的 EntityManager
     */
    public static void closeEntityManager(EntityManager em) {
        if (em != null) {
            try {
                em.close();
            } catch (Exception e) {
                logger.warn("Failed to close entity manager", e);
            }
        }
    }
}
