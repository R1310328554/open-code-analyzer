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

import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.naming.AuthenticationException;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.AttributeInUseException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.NoSuchAttributeException;
import javax.naming.directory.SchemaViolationException;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapName;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.LDAPConstants;
import org.keycloak.models.ModelException;
import org.keycloak.representations.idm.LDAPCapabilityRepresentation;
import org.keycloak.representations.idm.LDAPCapabilityRepresentation.CapabilityType;
import org.keycloak.storage.ldap.LDAPConfig;
import org.keycloak.storage.ldap.idm.model.LDAPDn;
import org.keycloak.storage.ldap.idm.model.LDAPObject;
import org.keycloak.storage.ldap.idm.query.Condition;
import org.keycloak.storage.ldap.idm.query.internal.EqualCondition;
import org.keycloak.storage.ldap.idm.query.internal.LDAPQuery;
import org.keycloak.storage.ldap.idm.query.internal.LDAPQueryConditionsBuilder;
import org.keycloak.storage.ldap.idm.store.IdentityStore;
import org.keycloak.storage.ldap.mappers.LDAPOperationDecorator;

import org.jboss.logging.Logger;

/**
 * 基于 LDAP 目录的 {@link org.keycloak.storage.ldap.idm.store.IdentityStore} 实现，封装 CRUD、查询、凭证与组 membership。
 *
 * @author Shane Bryzak
 * @author Anil Saldhana
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 */
public class LDAPIdentityStore implements IdentityStore {

    private static final Logger logger = Logger.getLogger(LDAPIdentityStore.class);
    private static final Pattern rangePattern = Pattern.compile("([^;]+);range=([0-9]+)-([0-9]+|\\*)");

    private final LDAPConfig config;
    private final LDAPOperationManager operationManager;

    public LDAPIdentityStore(KeycloakSession session, LDAPConfig config) {
        this.config = config;
        this.operationManager = new LDAPOperationManager(session, config);
    }

    /** {@inheritDoc} */
    @Override
    public LDAPConfig getConfig() {
        return this.config;
    }

    /** {@inheritDoc} 在 LDAP 中创建条目，UUID 由服务器分配。 */
    @Override
    public void add(LDAPObject ldapObject) {
        // UUID 由 LDAP 服务器分配
        if (ldapObject.getUuid() != null) {
            throw new ModelException("Can't add object with already assigned uuid");
        }

        BasicAttributes ldapAttributes = extractAttributesForSaving(ldapObject, true);
        ldapObject.setUuid(operationManager.createSubContext(ldapObject.getDn().getLdapName(), ldapAttributes));

        if (logger.isDebugEnabled()) {
            logger.debugf("Type with identifier [%s] and dn [%s] successfully added to LDAP store.", ldapObject.getUuid(), ldapObject.getDn());
        }
    }

    /** {@inheritDoc} 向组添加成员属性值。 */
    @Override
    public void addMemberToGroup(LdapName groupDn, String memberAttrName, String value) {
        // 不检查 EMPTY_MEMBER_ATTRIBUTE_VALUE，避免多余查询
        // the value will be there forever for objectclasses that enforces the attribute as MUST
        BasicAttribute attr = new BasicAttribute(memberAttrName, value);
        ModificationItem item = new ModificationItem(DirContext.ADD_ATTRIBUTE, attr);
        try {
            this.operationManager.modifyAttributesNaming(groupDn, new ModificationItem[]{item}, null);
        } catch (AttributeInUseException | NameAlreadyBoundException e) {
            logger.debugf("Group %s already contains the member %s", groupDn, value);
        } catch (NamingException e) {
            throw new ModelException("Could not modify attribute for DN [" + groupDn + "]", e);
        }
    }

    /** {@inheritDoc} 从组移除成员；schema 违反时尝试写入空成员占位值。 */
    @Override
    public void removeMemberFromGroup(LdapName groupDn, String memberAttrName, String value) {
        BasicAttribute attr = new BasicAttribute(memberAttrName, value);
        ModificationItem item = new ModificationItem(DirContext.REMOVE_ATTRIBUTE, attr);
        try {
            this.operationManager.modifyAttributesNaming(groupDn, new ModificationItem[]{item}, null);
        } catch (NoSuchAttributeException e) {
            logger.debugf("Group %s does not contain the member %s", groupDn, value);
        } catch (SchemaViolationException e) {
            // 移除唯一成员导致 schema 违反时，添加空成员占位属性
            logger.infof("Schema violation in group %s removing member %s. Trying adding empty member attribute.", groupDn, value);
            try {
                this.operationManager.modifyAttributesNaming(groupDn,
                        new ModificationItem[]{item, new ModificationItem(DirContext.ADD_ATTRIBUTE, new BasicAttribute(memberAttrName, LDAPConstants.EMPTY_MEMBER_ATTRIBUTE_VALUE))},
                        null);
            } catch (NamingException ex) {
                throw new ModelException("Could not modify attribute for DN [" + groupDn + "]", ex);
            }
        } catch (NamingException e) {
            throw new ModelException("Could not modify attribute for DN [" + groupDn + "]", e);
        }
    }

    /** {@inheritDoc} 更新 LDAP 条目，必要时重命名 DN。 */
    @Override
    public void update(LDAPObject ldapObject) {
        checkRename(ldapObject);

        BasicAttributes updatedAttributes = extractAttributesForSaving(ldapObject, false);
        NamingEnumeration<Attribute> attributes = updatedAttributes.getAll();

        this.operationManager.modifyAttributes(ldapObject.getDn().getLdapName(), attributes);

        if (logger.isDebugEnabled()) {
            logger.debugf("Type with identifier [%s] and DN [%s] successfully updated to LDAP store.", ldapObject.getUuid(), ldapObject.getDn());
        }
    }

    /** 检测 RDN 变更并在需要时执行 LDAP rename。 */
    protected void checkRename(LDAPObject ldapObject) {
        LDAPDn.RDN firstRdn = ldapObject.getDn().getFirstRdn();
        LdapName oldDn = ldapObject.getDn().getLdapName();

        // Detect which keys will need to be updated in RDN, which are new keys to be added, and which are to be removed
        List<String> toUpdateKeys = firstRdn.getAllKeys();
        toUpdateKeys.retainAll(ldapObject.getRdnAttributeNames());

        List<String> toRemoveKeys = firstRdn.getAllKeys();
        toRemoveKeys.removeAll(ldapObject.getRdnAttributeNames());

        List<String> toAddKeys = new ArrayList<>(ldapObject.getRdnAttributeNames());
        toAddKeys.removeAll(firstRdn.getAllKeys());

        // Go through all the keys in the oldRDN and doublecheck if they are changed or not
        boolean changed = false;
        for (String attrKey : toUpdateKeys) {
            if (ldapObject.getReadOnlyAttributeNames().contains(attrKey.toLowerCase())) {
                continue;
            }

            String rdnAttrVal = ldapObject.getAttributeAsString(attrKey);

            // Could be the case when RDN attribute of the target object is not included in Keycloak mappers
            if (rdnAttrVal == null) {
                continue;
            }

            String oldRdnAttrVal = firstRdn.getAttrValue(attrKey);

            if (!oldRdnAttrVal.equalsIgnoreCase(rdnAttrVal)) {
                changed = true;
                firstRdn.setAttrValue(attrKey, rdnAttrVal);
            }
        }

        // Add new keys
        for (String attrKey : toAddKeys) {
            String rdnAttrVal = ldapObject.getAttributeAsString(attrKey);

            // Could be the case when RDN attribute of the target object is not included in Keycloak mappers
            if (rdnAttrVal == null) {
                continue;
            }

            changed = true;
            firstRdn.setAttrValue(attrKey, rdnAttrVal);
        }

        // Remove old keys
        for (String attrKey : toRemoveKeys) {
            changed |= firstRdn.removeAttrValue(attrKey);
        }

        if (changed) {
            LDAPDn newLdapDn = ldapObject.getDn().getParentDn();
            newLdapDn.addFirst(firstRdn);

            LdapName newDn = newLdapDn.getLdapName();

            logger.debugf("Renaming LDAP Object. Old DN: [%s], New DN: [%s]", oldDn, newDn);

            // In case, that there is conflict (For example already existing "CN=John Anthony"), the different DN is returned
            newDn = this.operationManager.renameEntry(oldDn, newDn, true);

            ldapObject.setDn(LDAPDn.fromLdapName(newDn));
        }
    }

    /** {@inheritDoc} 从 LDAP 删除条目。 */
    @Override
    public void remove(LDAPObject ldapObject) {
        this.operationManager.removeEntry(ldapObject.getDn().getLdapName());

        if (logger.isDebugEnabled()) {
            logger.debugf("Type with identifier [%s] and DN [%s] successfully removed from LDAP store.", ldapObject.getUuid(), ldapObject.getDn().toString());
        }
    }


    /** {@inheritDoc} 执行 LDAP 查询并填充 {@link LDAPObject} 列表。 */
    @Override
    public List<LDAPObject> fetchQueryResults(LDAPQuery identityQuery) {
        if (identityQuery.getSorting() != null && !identityQuery.getSorting().isEmpty()) {
            throw new ModelException("LDAP Identity Store does not yet support sorted queries.");
        }

        List<LDAPObject> results = new ArrayList<>();

        try {
            LdapName baseDN = identityQuery.getSearchDn();

            for (Condition condition : identityQuery.getConditions()) {

                // Check if we are searching by ID
                String uuidAttrName = getConfig().getUuidLDAPAttributeName();
                if (condition instanceof EqualCondition) {
                    EqualCondition equalCondition = (EqualCondition) condition;
                    if (equalCondition.getParameterName().equalsIgnoreCase(uuidAttrName)) {
                        SearchResult search = this.operationManager
                                .lookupById(baseDN, equalCondition.getValue().toString(), identityQuery.getReturningLdapAttributes());

                        if (search != null) {
                            results.add(populateAttributedType(search, identityQuery));
                        }

                        return results;
                    }
                }
            }


            Condition condition = createIdentityTypeSearchFilter(identityQuery);

            List<SearchResult> search;
            if (getConfig().isPagination() && identityQuery.getLimit() > 0) {
                search = this.operationManager.searchPaginated(baseDN, condition, identityQuery);
            } else {
                search = this.operationManager.search(baseDN, condition, identityQuery.getReturningLdapAttributes(), identityQuery.getSearchScope());
            }

            for (SearchResult result : search) {
                // don't add the branch in subtree search
                if (identityQuery.getSearchScope() != SearchControls.SUBTREE_SCOPE || !baseDN.equals(new LdapName(result.getNameInNamespace()))) {
                    results.add(populateAttributedType(result, identityQuery));
                }
            }
        } catch (NameNotFoundException e) {
            if (identityQuery.getSearchScope() == SearchControls.OBJECT_SCOPE) {
                // if searching in base (dn search) return empty as entry does not exist
                return Collections.emptyList();
            }
            throw new ModelException("Querying of LDAP failed " + identityQuery, e);
        } catch (Exception e) {
            throw new ModelException("Querying of LDAP failed " + identityQuery, e);
        }

        return results;
    }

    /** {@inheritDoc} 通过临时取消 limit 统计查询结果数。 */
    @Override
    public int countQueryResults(LDAPQuery identityQuery) {
        int limit = identityQuery.getLimit();

        identityQuery.setLimit(0);

        int resultCount = identityQuery.getResultList().size();

        identityQuery.setLimit(limit);

        return resultCount;
    }

    /** {@inheritDoc} 查询 root DSE 支持的 LDAP 控制、扩展与特性。 */
    @Override
    public Set<LDAPCapabilityRepresentation> queryServerCapabilities() {
        Set<LDAPCapabilityRepresentation> result = new LinkedHashSet<>();
        try {
            List<String> attrs = new ArrayList<>();
            attrs.add("supportedControl");
            attrs.add("supportedExtension");
            attrs.add("supportedFeatures");
            List<SearchResult> searchResults = operationManager
                .search(new LdapName(Collections.emptyList()),
                        new LDAPQueryConditionsBuilder().present(LDAPConstants.OBJECT_CLASS),
                        Collections.unmodifiableCollection(attrs), SearchControls.OBJECT_SCOPE);
            if (searchResults.size() != 1) {
                throw new ModelException("Could not query root DSE: unexpected result size");
            }
            SearchResult rootDse = searchResults.get(0);
            Attributes attributes = rootDse.getAttributes();
            for (String attr: attrs) {
                Attribute attribute = attributes.get(attr);
                if (null != attribute) {
                    CapabilityType capabilityType = CapabilityType.fromRootDseAttributeName(attr);
                    NamingEnumeration<?> values = attribute.getAll();
                    while (values.hasMoreElements()) {
                        Object o = values.nextElement();
                        LDAPCapabilityRepresentation capability = new LDAPCapabilityRepresentation(o, capabilityType);
                        logger.info("rootDSE query: " + capability);
                        result.add(capability);
                    }
                }
            }
            return result;
        } catch (NamingException e) {
            throw new ModelException("Failed to query root DSE: " + e.getMessage(), e);
        }
    }

    // ========== 凭证与用户相关操作 ==========

    /** {@inheritDoc} 使用用户 DN 与密码进行 LDAP 绑定验证。 */
    @Override
    public void validatePassword(LDAPObject user, String password) throws AuthenticationException {
        if (logger.isTraceEnabled()) {
            logger.tracef("Using DN [%s] for authentication of user", user.getDn());
        }

        operationManager.authenticate(user.getDn().getLdapName(), password);
    }

    /** {@inheritDoc} 更新 LDAP 用户密码（AD unicodePwd 或标准 userPassword/扩展操作）。 */
    @Override
    public void updatePassword(LDAPObject user, String password, LDAPOperationDecorator passwordUpdateDecorator) {
        if (logger.isDebugEnabled()) {
            logger.debugf("Using DN [%s] for updating LDAP password of user", user.getDn());
        }

        if (getConfig().isActiveDirectory()) {
            updateADPassword(user.getDn().getLdapName(), password, passwordUpdateDecorator);
            return;
        }

        try {
            if (config.useExtendedPasswordModifyOp()) {
                operationManager.passwordModifyExtended(user.getDn().getLdapName(), password, passwordUpdateDecorator);
            } else {
                ModificationItem[] mods = new ModificationItem[1];
                BasicAttribute mod0 = new BasicAttribute(LDAPConstants.USER_PASSWORD_ATTRIBUTE, password);
                mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, mod0);
                operationManager.modifyAttributes(user.getDn().getLdapName(), mods, passwordUpdateDecorator);
            }
        } catch (ModelException me) {
            throw me;
        } catch (Exception e) {
            throw new ModelException("Error updating password.", e);
        }
    }

    /** Active Directory 专用：以 UTF-16LE 引号串写入 unicodePwd。 */
    private void updateADPassword(LdapName userDN, String password, LDAPOperationDecorator passwordUpdateDecorator) {
        try {
            // Replace the "unicdodePwd" attribute with a new value
            // Password must be both Unicode and a quoted string
            String newQuotedPassword = "\"" + password + "\"";
            byte[] newUnicodePassword = newQuotedPassword.getBytes("UTF-16LE");

            BasicAttribute unicodePwd = new BasicAttribute("unicodePwd", newUnicodePassword);

            List<ModificationItem> modItems = new ArrayList<ModificationItem>();
            modItems.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE, unicodePwd));

            operationManager.modifyAttributes(userDN, modItems.toArray(new ModificationItem[] {}), passwordUpdateDecorator);
        } catch (ModelException me) {
            throw me;
        } catch (Exception e) {
            throw new ModelException("Error updating password", e);
        }
    }

    // ************ END CREDENTIALS AND USER SPECIFIC STUFF

    /** 为查询附加 objectClass 条件并合并为 AND 过滤器。 */
    protected Condition createIdentityTypeSearchFilter(final LDAPQuery identityQuery) {
        LDAPQueryConditionsBuilder conditionsBuilder = new LDAPQueryConditionsBuilder();
        Set<Condition> conditions = new LinkedHashSet<>(identityQuery.getConditions());
        addObjectClassesConditions(conditionsBuilder, identityQuery.getObjectClasses(), conditions);
        return conditionsBuilder.andCondition(conditions.toArray(Condition[]::new));
    }


    private Set<Condition> addObjectClassesConditions(LDAPQueryConditionsBuilder conditionsBuilder,
            Collection<String> objectClasses, Set<Condition> conditions) {
        if (!objectClasses.isEmpty()) {
            for (String objectClass : objectClasses) {
                conditions.add(conditionsBuilder.equal(LDAPConstants.OBJECT_CLASS, objectClass));
            }
        } else {
            conditions.add(conditionsBuilder.present(LDAPConstants.OBJECT_CLASS));
        }

        return conditions;
    }


    /** 将 {@link SearchResult} 转为 {@link LDAPObject}，处理 ranged 属性与二进制 UUID。 */
    private LDAPObject populateAttributedType(SearchResult searchResult, LDAPQuery ldapQuery) {
        Set<String> readOnlyAttrNames = ldapQuery.getReturningReadOnlyLdapAttributes();
        Set<String> lowerCasedAttrNames = new TreeSet<>();
        for (String attrName : ldapQuery.getReturningLdapAttributes()) {
            lowerCasedAttrNames.add(attrName.toLowerCase());
        }

        try {
            String entryDN = searchResult.getNameInNamespace();
            Attributes attributes = searchResult.getAttributes();

            LDAPObject ldapObject = new LDAPObject();
            LDAPDn dn = LDAPDn.fromString(entryDN);
            ldapObject.setDn(dn);
            ldapObject.setRdnAttributeNames(dn.getFirstRdn().getAllKeys());

            NamingEnumeration<? extends Attribute> ldapAttributes = attributes.getAll();

            while (ldapAttributes.hasMore()) {
                Attribute ldapAttribute = ldapAttributes.next();

                try {
                    ldapAttribute.get();
                } catch (NoSuchElementException nsee) {
                    continue;
                }

                String ldapAttributeName = ldapAttribute.getID();

                // check for ranged attribute
                Matcher m = rangePattern.matcher(ldapAttributeName);
                if (m.matches()) {
                    ldapAttributeName = m.group(1);
                    // range=X-* means all the attributes returned
                    if (!m.group(3).equals("*")) {
                        try {
                            int max = Integer.parseInt(m.group(3));
                            ldapObject.addRangedAttribute(ldapAttributeName, max);
                        } catch (NumberFormatException e) {
                            logger.warnf("Invalid ranged expresion for attribute: %s", m.group(0));
                        }
                    }
                }

                if (ldapAttributeName.equalsIgnoreCase(getConfig().getUuidLDAPAttributeName())) {
                    Object uuidValue = ldapAttribute.get();
                    ldapObject.setUuid(this.operationManager.decodeEntryUUID(uuidValue));
                }

                // Note: UUID is normally not populated here. It's populated just in case that it's used for name of other attribute as well
                if (!ldapAttributeName.equalsIgnoreCase(getConfig().getUuidLDAPAttributeName()) || (lowerCasedAttrNames.contains(ldapAttributeName.toLowerCase()))) {
                    Set<String> attrValues = new LinkedHashSet<>();
                    NamingEnumeration<?> enumm = ldapAttribute.getAll();
                    while (enumm.hasMoreElements()) {
                        Object val = enumm.next();

                        if (val instanceof byte[]) { // byte[]
                            String attrVal = Base64.getEncoder().encodeToString((byte[]) val);
                            attrValues.add(attrVal);
                        } else { // String
                            String attrVal = val.toString().trim();
                            attrValues.add(attrVal);
                        }
                    }

                    if (ldapAttributeName.equalsIgnoreCase(LDAPConstants.OBJECT_CLASS)) {
                        ldapObject.setObjectClasses(attrValues);
                    } else {
                        ldapObject.setAttribute(ldapAttributeName, attrValues);

                        // readOnlyAttrNames are lower-cased
                        if (readOnlyAttrNames.contains(ldapAttributeName.toLowerCase())) {
                            ldapObject.addReadOnlyAttributeName(ldapAttributeName);
                        }
                    }
                }
            }

            if (logger.isTraceEnabled()) {
                logger.tracef("Found ldap object and populated with the attributes. LDAP Object: %s", ldapObject.toString());
            }
            return ldapObject;

        } catch (Exception e) {
            throw new ModelException("Could not populate attribute type " + searchResult.getNameInNamespace() + ".", e);
        }
    }


    /** 从 {@link LDAPObject} 提取待写入 LDAP 的 {@link BasicAttributes}。 */
    protected BasicAttributes extractAttributesForSaving(LDAPObject ldapObject, boolean isCreate) {
        BasicAttributes entryAttributes = new BasicAttributes();

        Set<String> rdnAttrNamesLowerCased = ldapObject.getRdnAttributeNames().stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        for (Map.Entry<String, Set<String>> attrEntry : ldapObject.getAttributes().entrySet()) {
            String attrName = attrEntry.getKey();
            Set<String> attrValue = attrEntry.getValue();

            if (attrValue == null) {
                // Shouldn't happen
                logger.warnf("Attribute '%s' is null on LDAP object '%s' . Using empty value to be saved to LDAP", attrName, ldapObject.getDn().toString());
                attrValue = Collections.emptySet();
            }

            String attrNameLowercased = attrName.toLowerCase();
            if (
                // Ignore empty attributes on create (changetype: add)
                !(isCreate && attrValue.isEmpty()) &&

                // Skip read-only attributes for saving if not create. ldapObject.getReadOnlyAttributeNames() are lower-cased
                (isCreate || !ldapObject.getReadOnlyAttributeNames().contains(attrNameLowercased)) &&

                // Only extract RDN for create since it can't be changed on update
                (isCreate || !rdnAttrNamesLowerCased.contains(attrNameLowercased))
            ) {
                if (getConfig().getBinaryAttributeNames().contains(attrName)) {
                    // Binary attribute
                    entryAttributes.put(createBinaryBasicAttribute(attrName, attrValue));
                } else {
                    // Text attribute
                    entryAttributes.put(createBasicAttribute(attrName, attrValue));
                }
            }
        }

        // Don't extract object classes for update
        if (isCreate) {
            BasicAttribute objectClassAttribute = new BasicAttribute(LDAPConstants.OBJECT_CLASS);

            for (String objectClassValue : ldapObject.getObjectClasses()) {
                objectClassAttribute.add(objectClassValue);
            }

            entryAttributes.put(objectClassAttribute);
        }

        return entryAttributes;
    }

    private BasicAttribute createBasicAttribute(String attrName, Set<String> attrValue) {
        BasicAttribute attr = new BasicAttribute(attrName);

        for (String value : attrValue) {
            if (value == null || value.trim().length() == 0) {
                value = LDAPConstants.EMPTY_ATTRIBUTE_VALUE;
            }

            attr.add(value);
        }

        return attr;
    }

    private BasicAttribute createBinaryBasicAttribute(String attrName, Set<String> attrValue) {
        BasicAttribute attr = new BasicAttribute(attrName);

        for (String value : attrValue) {
            if (value == null || value.trim().length() == 0) {
                value = LDAPConstants.EMPTY_ATTRIBUTE_VALUE;
            }

            try {
                byte[] bytes = Base64.getMimeDecoder().decode(value);
                attr.add(bytes);
            } catch (IllegalArgumentException iae) {
                logger.warnf("Wasn't able to Base64 decode the attribute value. Ignoring attribute update. Attribute: %s, Attribute value: %s", attrName, attrValue);
            }
        }

        return attr;
    }

    /** 返回当前 LDAP 类型对应的密码修改时间属性名。 */
    public String getPasswordModificationTimeAttributeName() {
        if (getConfig().isActiveDirectory()) {
            return LDAPConstants.PWD_LAST_SET;
        }
        if (getConfig().isRHDS()) {
            return LDAPConstants.PWD_UPDATE_TIME;
        }
        return LDAPConstants.PWD_CHANGED_TIME;
    }

}
