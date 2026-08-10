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

package org.keycloak.storage.ldap.idm.store.ldap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import javax.naming.AuthenticationException;
import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.BasicControl;
import javax.naming.ldap.Control;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.PagedResultsControl;
import javax.naming.ldap.PagedResultsResponseControl;
import javax.naming.ldap.StartTlsResponse;
import javax.net.ssl.SSLSocketFactory;

import org.keycloak.common.util.Time;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.LDAPConstants;
import org.keycloak.models.ModelException;
import org.keycloak.storage.ldap.LDAPConfig;
import org.keycloak.storage.ldap.idm.model.LDAPDn;
import org.keycloak.storage.ldap.idm.query.Condition;
import org.keycloak.storage.ldap.idm.query.internal.LDAPQuery;
import org.keycloak.storage.ldap.idm.query.internal.LDAPQueryConditionsBuilder;
import org.keycloak.storage.ldap.idm.store.ldap.control.PasswordPolicyControl;
import org.keycloak.storage.ldap.idm.store.ldap.control.PasswordPolicyControlFactory;
import org.keycloak.storage.ldap.idm.store.ldap.control.PasswordPolicyPasswordChangeException;
import org.keycloak.storage.ldap.idm.store.ldap.extended.PasswordModifyRequest;
import org.keycloak.storage.ldap.mappers.LDAPOperationDecorator;
import org.keycloak.tracing.TracingProvider;
import org.keycloak.truststore.TruststoreProvider;

import org.jboss.logging.Logger;

/**
 * <p>LDAP 目录操作管理器：搜索、修改、认证、分页、密码扩展操作等。</p>
 *
 * @author Anil Saldhana
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 */
public class LDAPOperationManager {

    private static final Logger logger = Logger.getLogger(LDAPOperationManager.class);

    private static final Logger perfLogger = Logger.getLogger(LDAPOperationManager.class, "perf");

    private final KeycloakSession session;
    private final LDAPConfig config;

    /**
     * @param session Keycloak 会话
     * @param config LDAP 配置
     */
    public LDAPOperationManager(KeycloakSession session, LDAPConfig config) {
        this.session = session;
        this.config = config;
    }

    /**
     * <p>
     * 对指定 DN 执行 REPLACE_ATTRIBUTE 修改单个 {@link javax.naming.directory.Attribute}。
     * </p>
     *
     * @param dn
     * @param attribute
     */
    public void modifyAttribute(LdapName dn, Attribute attribute) {
        ModificationItem[] mods = new ModificationItem[]{new ModificationItem(DirContext.REPLACE_ATTRIBUTE, attribute)};
        modifyAttributes(dn, mods, null);
    }

    /**
     * <p>
     * 对指定 DN 批量 REPLACE_ATTRIBUTE 修改多个 {@link Attribute}。
     * </p>
     *
     * @param dn
     * @param attributes
     */
    public void modifyAttributes(LdapName dn,  NamingEnumeration<Attribute> attributes) {
        try {
            List<ModificationItem> modItems = new ArrayList<>();
            while (attributes.hasMore()) {
                ModificationItem modItem = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, attributes.next());
                modItems.add(modItem);
            }

            modifyAttributes(dn, modItems.toArray(ModificationItem[]::new), null);
        } catch (NamingException ne) {
            throw new ModelException("Could not modify attributes on entry from DN [" + dn + "]", ne);
        }

    }

    /**
     * <p>
     * 对指定 DN 执行 REMOVE_ATTRIBUTE 移除属性。
     * </p>
     *
     * @param dn
     * @param attribute
     */
    public void removeAttribute(LdapName dn, Attribute attribute) {
        ModificationItem[] mods = new ModificationItem[]{new ModificationItem(DirContext.REMOVE_ATTRIBUTE, attribute)};
        modifyAttributes(dn, mods, null);
    }

    /**
     * <p>
     * 对指定 DN 执行 ADD_ATTRIBUTE 添加属性。
     * </p>
     *
     * @param dn
     * @param attribute
     */
    public void addAttribute(LdapName dn, Attribute attribute) {
        ModificationItem[] mods = new ModificationItem[]{new ModificationItem(DirContext.ADD_ATTRIBUTE, attribute)};
        modifyAttributes(dn, mods, null);
    }

    /**
     * <p>
     * 从 LDAP 树中递归删除条目及其子条目。
     * </p>
     */
    public void removeEntry(final LdapName entryDn) {
        try {
            execute(new LdapOperation<SearchResult>() {

                @Override
                public SearchResult execute(LdapContext context) throws NamingException {
                    if (logger.isTraceEnabled()) {
                        logger.tracef("Removing entry with DN [%s]", entryDn);
                    }
                    destroySubcontext(context, entryDn);
                    return null;
                }


                @Override
                public String toString() {
                    return new StringBuilder("LdapOperation: remove\n")
                            .append(" dn: ").append(entryDn)
                            .toString();
                }

            });
        } catch (NamingException e) {
            throw new ModelException("Could not remove entry from DN [" + entryDn + "]", e);
        }
    }


    /**
     * 重命名 LDAP 条目（修改 DN）
     *
     * @param oldDn
     * @param newDn
     * @param fallback 为 true 时 DN 冲突则尝试追加数字后缀（如 CN=John Doe0）
     * @return 最终使用的无冲突 DN
     */
    public LdapName renameEntry(LdapName oldDn, LdapName newDn, boolean fallback) {
        try {
            LdapName newNonConflictingDn = execute(new LdapOperation<LdapName>() {

                @Override
                public LdapName execute(LdapContext context) throws NamingException {
                    LdapName dn = newDn;

                    // 最多尝试 5 次备选 DN
                    int max = 5;
                    for (int i=0 ; i<max ; i++) {
                        try {
                            context.rename(oldDn, dn);
                            return dn;
                        } catch (NameAlreadyBoundException ex) {
                            if (!fallback) {
                                throw ex;
                            } else {
                                LdapName failedDn = dn;
                                if (i<max) {
                                    dn = findNextDNForFallback(newDn, i);
                                    logger.warnf("Failed to rename DN [%s] to [%s]. Will try to fallback to DN [%s]", oldDn, failedDn, dn);
                                } else {
                                    logger.warnf("Failed all fallbacks for renaming [%s]", oldDn);
                                    throw ex;
                                }
                            }
                        }
                    }

                    throw new ModelException("Could not rename entry from DN [" + oldDn + "] to new DN [" + newDn + "]. All fallbacks failed");
                }


                @Override
                public String toString() {
                    return new StringBuilder("LdapOperation: renameEntry\n")
                            .append(" oldDn: ").append(oldDn).append("\n")
                            .append(" newDn: ").append(newDn)
                            .toString();
                }

            });
            return newNonConflictingDn;
        } catch (NamingException e) {
            throw new ModelException("Could not rename entry from DN [" + oldDn + "] to new DN [" + newDn + "]", e);
        }
    }

    /** 生成冲突重命名时的备选 DN（RDN 值追加计数器）。 */
    private LdapName findNextDNForFallback(LdapName newDn, int counter) {
        LDAPDn dn = LDAPDn.fromLdapName(newDn);
        LDAPDn.RDN firstRdn = dn.getFirstRdn();
        String rdnAttrName = firstRdn.getAllKeys().get(0);
        String rdnAttrVal = firstRdn.getAttrValue(rdnAttrName);
        LDAPDn parentDn = dn.getParentDn();
        parentDn.addFirst(rdnAttrName, rdnAttrVal + counter);
        return parentDn.getLdapName();
    }

    /** 在 baseDN 下按条件与范围执行 LDAP 搜索。 */
    public List<SearchResult> search(final LdapName baseDN, final Condition condition, Collection<String> returningAttributes, int searchScope) throws NamingException {
        final List<SearchResult> result = new ArrayList<>();
        final SearchControls cons = getSearchControls(returningAttributes, searchScope);
        final String filter = condition.toFilter();

        try {
            return execute(new LdapOperation<List<SearchResult>>() {
                @Override
                public List<SearchResult> execute(LdapContext context) throws NamingException {
                    NamingEnumeration<SearchResult> search = context.search(baseDN, filter, cons);

                    while (search.hasMoreElements()) {
                        result.add(search.nextElement());
                    }

                    search.close();

                    return result;
                }


                @Override
                public String toString() {
                    return new StringBuilder("LdapOperation: search\n")
                            .append(" baseDn: ").append(baseDN).append("\n")
                            .append(" filter: ").append(filter).append("\n")
                            .append(" searchScope: ").append(searchScope).append("\n")
                            .append(" returningAttrs: ").append(returningAttributes).append("\n")
                            .append(" resultSize: ").append(result.size())
                            .toString();
                }


            });
        } catch (NamingException e) {
            logger.errorf(e, "Could not query server using DN [%s] and filter [%s]", baseDN, filter);
            throw e;
        }
    }

    /** 使用 PagedResultsControl 执行分页搜索。 */
    public List<SearchResult> searchPaginated(final LdapName baseDN, final Condition condition, final LDAPQuery identityQuery) throws NamingException {
        final List<SearchResult> result = new ArrayList<>();
        final SearchControls cons = getSearchControls(identityQuery.getReturningLdapAttributes(), identityQuery.getSearchScope());
        final String filter = condition.toFilter();

        // 首页：分页上下文尚未初始化
        if (identityQuery.getPaginationContext() == null) {
            identityQuery.initPagination();
        }

        try {
            return execute(new LdapOperation<List<SearchResult>>() {

                @Override
                public List<SearchResult> execute(LdapContext context) throws NamingException {
                    try {
                        byte[] cookie = identityQuery.getPaginationContext().getCookie();
                        PagedResultsControl pagedControls = new PagedResultsControl(identityQuery.getLimit(), cookie, Control.CRITICAL);
                        context.setRequestControls(new Control[] { pagedControls });

                        NamingEnumeration<SearchResult> search = context.search(baseDN, filter, cons);

                        while (search.hasMoreElements()) {
                            result.add(search.nextElement());
                        }

                        search.close();

                        Control[] responseControls = context.getResponseControls();
                        if (responseControls != null) {
                            for (Control respControl : responseControls) {
                                if (respControl instanceof PagedResultsResponseControl) {
                                    PagedResultsResponseControl prrc = (PagedResultsResponseControl)respControl;
                                    cookie = prrc.getCookie();
                                    identityQuery.getPaginationContext().setCookie(cookie);
                                }
                            }
                        } else {
                            /*
                             * This ensures that PaginationContext#hasNextPage() will return false if we don't get ResponseControls back
                             * from the LDAP query response. This helps to avoid an infinite loop in org.keycloak.storage.ldap.LDAPUtils.loadAllLDAPObjects
                             * See KEYCLOAK-19036
                             */
                            identityQuery.getPaginationContext().setCookie(null);
                            logger.warnf("Did not receive response controls for paginated query using DN [%s], filter [%s]. Did you hit a query result size limit?", baseDN, filter);
                        }

                        return result;
                    } catch (IOException ioe) {
                        logger.errorf(ioe, "Could not query server with paginated query using DN [%s], filter [%s]", baseDN, filter);
                        throw new NamingException(ioe.getMessage());
                    }
                }


                @Override
                public String toString() {
                    return new StringBuilder("LdapOperation: searchPaginated\n")
                            .append(" baseDn: ").append(baseDN).append("\n")
                            .append(" filter: ").append(filter).append("\n")
                            .append(" searchScope: ").append(identityQuery.getSearchScope()).append("\n")
                            .append(" returningAttrs: ").append(identityQuery.getReturningLdapAttributes()).append("\n")
                            .append(" limit: ").append(identityQuery.getLimit()).append("\n")
                            .append(" resultSize: ").append(result.size())
                            .toString();
                }

            }, identityQuery.getPaginationContext().getLdapContext(), null);
        } catch (NamingException e) {
            logger.errorf(e, "Could not query server using DN [%s] and filter [%s]", baseDN, filter);
            throw e;
        }
    }

    /** 构造 {@link SearchControls}，自动附加 UUID 与 objectClass 返回属性。 */
    private SearchControls getSearchControls(Collection<String> returningAttributes, int searchScope) {
        final SearchControls cons = new SearchControls();

        cons.setSearchScope(searchScope);
        cons.setReturningObjFlag(false);

        returningAttributes = getReturningAttributes(returningAttributes);

        cons.setReturningAttributes(returningAttributes.toArray(new String[returningAttributes.size()]));
        return cons;
    }

    /** 根据条目 ID 构造 LDAP 过滤器（含 objectGUID/eDirectory GUID 编码）。 */
    public Condition getFilterById(String id) {
        LDAPQueryConditionsBuilder builder = new LDAPQueryConditionsBuilder();
        Condition conditionId;

        if (this.config.isObjectGUID()) {
            byte[] objectGUID = LDAPUtil.encodeObjectGUID(id);
            conditionId = builder.equal(getUuidAttributeName(), objectGUID);
        } else if (this.config.isEdirectoryGUID()) {
            byte[] objectGUID = LDAPUtil.encodeObjectEDirectoryGUID(id);
            conditionId = builder.equal(getUuidAttributeName(), objectGUID);
        } else {
            conditionId = builder.equal(getUuidAttributeName(), id);
        }

        if (config.getCustomUserSearchFilter() != null) {
            return builder.andCondition(new Condition[]{conditionId, builder.addCustomLDAPFilter(config.getCustomUserSearchFilter())});
        } else {
            return conditionId;
        }
    }

    /** 按 UUID/ID 在 baseDN 下查找单条 {@link SearchResult}。 */
    public SearchResult lookupById(final LdapName baseDN, final String id, final Collection<String> returningAttributes) {
        final String filter = getFilterById(id).toFilter();

        try {
            final SearchControls cons = getSearchControls(returningAttributes, this.config.getSearchScope());

            return execute(new LdapOperation<SearchResult>() {

                @Override
                public SearchResult execute(LdapContext context) throws NamingException {
                    NamingEnumeration<SearchResult> search = context.search(baseDN, filter, cons);

                    try {
                        if (search.hasMoreElements()) {
                            return search.next();
                        }
                    } finally {
                        if (search != null) {
                            search.close();
                        }
                    }

                    return null;
                }


                @Override
                public String toString() {
                    return new StringBuilder("LdapOperation: lookupById\n")
                            .append(" baseDN: ").append(baseDN).append("\n")
                            .append(" filter: ").append(filter).append("\n")
                            .append(" searchScope: ").append(cons.getSearchScope()).append("\n")
                            .append(" returningAttrs: ").append(returningAttributes)
                            .toString();
                }

            });
        } catch (NamingException e) {
            throw new ModelException("Could not query server using DN [" + baseDN + "] and filter [" + filter + "]", e);
        }
    }

    /**
     * <p>
     * 递归删除指定 DN 及其所有子上下文。
     * </p>
     *
     * @param dn
     */
    private void destroySubcontext(LdapContext context, final LdapName dn) {
        try {
            NamingEnumeration<Binding> enumeration = null;

            try {
                enumeration = context.listBindings(dn);

                while (enumeration.hasMore()) {
                    Binding binding = enumeration.next();
                    String name = binding.getNameInNamespace();

                    destroySubcontext(context, new LdapName(name));
                }

                context.unbind(dn);
            } finally {
                try {
                    enumeration.close();
                } catch (Exception e) {
                }
            }
        } catch (Exception e) {
            throw new ModelException("Could not unbind DN [" + dn + "]", e);
        }
    }

    /**
     * <p>
     * 使用给定 DN 与密码进行简单绑定认证。
     * </p>
     *
     * @param dn
     * @param password
     * @throws AuthenticationException if authentication is not successful
     *
     */
    public void authenticate(LdapName dn, String password) throws AuthenticationException {

        if (password == null || password.isEmpty()) {
            throw new AuthenticationException("Empty password used");
        }

        LdapContext authCtx = null;
        StartTlsResponse tlsResponse = null;

        var tracing = session.getProvider(TracingProvider.class);
        tracing.startSpan(LDAPOperationManager.class, "authenticate");

        try {
            Hashtable<Object, Object> env = LDAPContextManager.getNonAuthConnectionProperties(config);

            // 禁用连接池，避免密码被缓存
            env.put("com.sun.jndi.ldap.connect.pool", "false");

            // 注册密码策略响应控制工厂
            env.put(LdapContext.CONTROL_FACTORIES, PasswordPolicyControlFactory.class.getName());

            // 创建连接但不立即绑定，以便先完成 StartTLS by not setting security principal and credentials yet.
            // That allows us to send optional StartTLS request before binding.
            authCtx = new InitialLdapContext(env, null);

            // 按需发起 StartTLS 并配置 SSL
            if (config.isStartTls()) {
                SSLSocketFactory sslSocketFactory = null;
                if (LDAPUtil.shouldUseTruststoreSpi(config)) {
                    TruststoreProvider provider = session.getProvider(TruststoreProvider.class);
                    sslSocketFactory = provider.getSSLSocketFactory();
                }

                tlsResponse = LDAPContextManager.startTLS(authCtx, sslSocketFactory);

                // Exception should be already thrown by LDAPContextManager.startTLS if "startTLS" could not be established, but rather do some additional check
                if (tlsResponse == null) {
                    throw new AuthenticationException("Null TLS Response returned from the authentication");
                }
            }

            // 配置用户凭证
            authCtx.addToEnvironment(Context.SECURITY_AUTHENTICATION, "simple");
            authCtx.addToEnvironment(Context.SECURITY_PRINCIPAL, dn.toString());
            authCtx.addToEnvironment(Context.SECURITY_CREDENTIALS, password);

            // 发送 bind 请求；失败时抛出 AuthenticationException
            authCtx.reconnect(getControls());

            // 检查响应中的密码策略控制
            // If present and forced password change is required, throw an exception.
            Control[] responseControls = authCtx.getResponseControls();
            if (responseControls != null) {
                for (Control control : responseControls) {
                    if (control instanceof PasswordPolicyControl) {
                        PasswordPolicyControl response = (PasswordPolicyControl) control;
                        if (response.changeAfterReset()) {
                            throw new PasswordPolicyPasswordChangeException();
                        }
                    }
                }
            }

        } catch (AuthenticationException ae) {
            if (logger.isDebugEnabled()) {
                logger.debugf(ae, "Authentication failed for DN [%s]", dn);
            }
            tracing.error(ae);
            throw ae;
        } catch(RuntimeException re){
            if (logger.isDebugEnabled()) {
                logger.debugf(re, "LDAP Connection TimeOut for DN [%s]", dn);
            }
            tracing.error(re);
            throw re;
        } catch (Exception e) {
            logger.errorf(e, "Unexpected exception when validating password of DN [%s]", dn);
            tracing.error(e);
            throw new AuthenticationException("Unexpected exception when validating password of user");
        } finally {
            if (tlsResponse != null) {
                try {
                    tlsResponse.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (authCtx != null) {
                try {
                    authCtx.close();
                } catch (NamingException e) {
                    e.printStackTrace();
                }
            }
            tracing.endSpan();
        }
    }

    /** 修改条目属性（可抛出 {@link NamingException}）。 */
    public void modifyAttributesNaming(final LdapName dn, final ModificationItem[] mods, LDAPOperationDecorator decorator) throws NamingException {
        if (logger.isTraceEnabled()) {
            logger.tracef("Modifying attributes for entry [%s]: [", dn);

            for (ModificationItem item : mods) {
                Object values;

                if (item.getAttribute().size() > 0) {
                    values = item.getAttribute().get();
                } else {
                    values = "No values";
                }

                String attrName = item.getAttribute().getID().toUpperCase();
                if (attrName.contains("PASSWORD") || attrName.contains("UNICODEPWD")) {
                    values = "********************";
                }

                logger.tracef("  Op [%s]: %s = %s", item.getModificationOp(), item.getAttribute().getID(), values);
            }

            logger.tracef("]");
        }

        execute(new LdapOperation<Void>() {

            @Override
            public Void execute(LdapContext context) throws NamingException {
                context.modifyAttributes(dn, mods);
                return null;
            }

            @Override
            public String toString() {
                return new StringBuilder("LdapOperation: modify\n")
                        .append(" dn: ").append(dn).append("\n")
                        .append(" modificationsSize: ").append(mods.length)
                        .toString();
            }

        }, decorator);
    }

    /** 修改条目属性，失败时包装为 {@link ModelException}。 */
    public void modifyAttributes(final LdapName dn, final ModificationItem[] mods, LDAPOperationDecorator decorator) {
        try {
            modifyAttributesNaming(dn, mods, decorator);
        } catch (NamingException e) {
            throw new ModelException("Could not modify attribute for DN [" + dn + "]", e);
        }
    }

    /** 在 LDAP 中创建子上下文并返回新条目的 UUID。 */
    public String createSubContext(final LdapName name, final Attributes attributes) {
        try {
            if (logger.isTraceEnabled()) {
                logger.tracef("Creating entry [%s] with attributes: [", name);

                NamingEnumeration<? extends Attribute> all = attributes.getAll();

                while (all.hasMore()) {
                    Attribute attribute = all.next();

                    String attrName = attribute.getID().toUpperCase();
                    Object attrVal = attribute.get();
                    if (attrName.contains("PASSWORD") || attrName.contains("UNICODEPWD")) {
                        attrVal = "********************";
                    }

                    logger.tracef("  %s = %s", attribute.getID(), attrVal);
                }

                logger.tracef("]");
            }

            return execute(new LdapOperation<>() {
                @Override
                public String execute(LdapContext context) throws NamingException {
                    DirContext subcontext = context.createSubcontext(name, attributes);
                    try {
                        String uuidLDAPAttributeName = config.getUuidLDAPAttributeName();
                        Attribute id = subcontext.getAttributes("", new String[]{uuidLDAPAttributeName}).get(uuidLDAPAttributeName);
                        if (id == null) {
                            throw new ModelException("Could not retrieve identifier for entry [" + name + "].");
                        }
                        return decodeEntryUUID(id.get());
                    } catch (NamingException ne) {
                        throw new ModelException("Could not retrieve identifier for entry [" + name + "].", ne);
                    } finally {
                        subcontext.close();
                    }
                }


                @Override
                public String toString() {
                    return new StringBuilder("LdapOperation: create\n")
                            .append(" dn: ").append(name).append("\n")
                            .append(" attributesSize: ").append(attributes.size())
                            .toString();
                }

            });
        } catch (NamingException e) {
            throw new ModelException("Error creating subcontext [" + name + "]", e);
        }
    }

    /** 若启用密码策略，返回非关键 PasswordPolicy 请求控制。 */
    private Control[] getControls() {
        // 启用时发送非关键 PasswordPolicy 请求控制
        if (config.isEnableLdapPasswordPolicy()) {
            return new Control[] { new BasicControl(PasswordPolicyControl.OID, false, null) };
        }
        return null;
    }

    /** 返回配置中的 UUID LDAP 属性名。 */
    private String getUuidAttributeName() {
        return this.config.getUuidLDAPAttributeName();
    }

    /** 按 entryUUID 在 baseDN 下加载条目属性。 */
    public Attributes getAttributes(final String entryUUID, final LdapName baseDN, Set<String> returningAttributes) {
        SearchResult search = lookupById(baseDN, entryUUID, returningAttributes);

        if (search == null) {
            throw new ModelException("Couldn't find item with ID [" + entryUUID + " under base DN [" + baseDN + "]");
        }

        return search.getAttributes();
    }

    /** 将 LDAP 返回的 UUID 原始值解码为字符串（AD/eDirectory 二进制或普通字符串）。 */
    public String decodeEntryUUID(final Object entryUUID) {
        if (entryUUID instanceof byte[]) {
            if (this.config.isObjectGUID()) {
                return LDAPUtil.decodeObjectGUID((byte[]) entryUUID);
            }
            if (this.config.isEdirectory() && this.config.isEdirectoryGUID()) {
                return LDAPUtil.decodeGuid((byte[]) entryUUID);
            }
        }
        return entryUUID.toString();
    }

    /**
     * 执行 RFC 3062 密码修改扩展操作更新指定 DN 的密码。
     *
     * @param dn distinguished name of the entry.
     * @param password the new password.
     * @param decorator A decorator to apply to the ldap operation.
     */

    /** {@inheritDoc} */
    public void passwordModifyExtended(LdapName dn, String password, LDAPOperationDecorator decorator) {
        try {
            execute(context -> {
                PasswordModifyRequest modifyRequest = new PasswordModifyRequest(dn.toString(), null, password);
                return context.extendedOperation(modifyRequest);
            }, decorator);
        } catch (NamingException e) {
            throw new ModelException("Could not execute the password modify extended operation for DN [" + dn + "]", e);
        }
    }

    private <R> R execute(LdapOperation<R> operation) throws NamingException {
        return execute(operation, null);
    }

    private <R> R execute(LdapOperation<R> operation, LDAPOperationDecorator decorator) throws NamingException {
        try (LDAPContextManager ldapContextManager = LDAPContextManager.create(session, config)) {
            return execute(operation, ldapContextManager.getLdapContext(), decorator);
        }
    }

    private <R> R execute(LdapOperation<R> operation, LdapContext context, LDAPOperationDecorator decorator) throws NamingException {
        if (context == null) {
            throw new IllegalArgumentException("Ldap context cannot be null");
        }

        Long start = null;

        if (perfLogger.isDebugEnabled()) {
            start = Time.currentTimeMillis();
        }

        var tracing = session.getProvider(TracingProvider.class);
        var span = tracing.startSpan(LDAPOperationManager.class, "execute");

        try {
            if (span.isRecording()) {
                span.setAttribute(Context.PROVIDER_URL, context.getEnvironment().get(Context.PROVIDER_URL).toString());
                span.setAttribute("operation", operation.toString());
            }

            if (decorator != null) {
                decorator.beforeLDAPOperation(context, operation);
            }

            return operation.execute(context);
        } catch (NamingException e) {
            tracing.error(e);
            throw e;
        } finally {
            tracing.endSpan();
            if (perfLogger.isDebugEnabled()) {
                long took = Time.currentTimeMillis() - start;

                if (took > 100) {
                    perfLogger.debugf("\n%s\ntook: %d ms\n", operation.toString(), took);
                } else if (perfLogger.isTraceEnabled()) {
                    perfLogger.tracef("\n%s\ntook: %d ms\n", operation.toString(), took);
                }
            }
        }
    }

    /** 在 {@link LdapContext} 上执行的 LDAP 操作回调。 */
    public interface LdapOperation<R> {
        R execute(LdapContext context) throws NamingException;
    }

    /** 确保返回属性集合包含 UUID 与 objectClass。 */
    private Set<String> getReturningAttributes(final Collection<String> returningAttributes) {
        Set<String> result = new HashSet<String>();

        result.addAll(returningAttributes);
        result.add(getUuidAttributeName());
        result.add(LDAPConstants.OBJECT_CLASS);

        return result;
    }
}
