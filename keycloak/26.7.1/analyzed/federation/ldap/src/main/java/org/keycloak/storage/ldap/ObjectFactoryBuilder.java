package org.keycloak.storage.ldap;

import java.util.Hashtable;
import java.util.List;
import javax.naming.CommunicationException;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.ldap.LdapContext;
import javax.naming.spi.NamingManager;
import javax.naming.spi.ObjectFactory;

import org.keycloak.storage.ldap.idm.store.ldap.SessionBoundInitialLdapContext;
import org.keycloak.utils.KeycloakSessionUtil;

import org.jboss.logging.Logger;

/**
 * JNDI {@link javax.naming.spi.ObjectFactoryBuilder}：仅处理含 LDAP URL 的 referral，否则拒绝非安全转介。
 * <p>
 * 遇到 referral 时若引用含 LDAP URL，则创建 {@link DirContextObjectFactory} 建立 {@link org.keycloak.storage.ldap.idm.store.ldap.SessionBoundInitialLdapContext}；
 * 否则抛出 {@link CommunicationException}。
 */
final class ObjectFactoryBuilder implements javax.naming.spi.ObjectFactoryBuilder, ObjectFactory {

    private static final Logger logger = Logger.getLogger(ObjectFactoryBuilder.class);
    private static final String IS_KC_OBJECT_FACTORY_BUILDER = "kc.jndi.object.factory.builder";

    /** 检测当前 JVM 是否已安装本 Keycloak ObjectFactoryBuilder。 */
    static boolean isSet() {
        Hashtable<Object, Object> env = new Hashtable<>();

        env.put(ObjectFactoryBuilder.IS_KC_OBJECT_FACTORY_BUILDER, Boolean.TRUE);

        try {
            Object instance = NamingManager.getObjectInstance(null, null, null, env);

            if (instance != null && instance.getClass().getName().equals(ObjectFactoryBuilder.class.getName())) {
                return true;
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to determine if ObjectFactoryBuilder is set", e);
        }

        return false;
    }

    @Override
    public ObjectFactory createObjectFactory(Object obj, Hashtable<?, ?> environment) throws NamingException {
        if (logger.isTraceEnabled()) {
            logger.tracef("Creating ObjectFactory for object: %s", obj);
        }

        if (obj instanceof Reference ref) {
            String factoryClassName = ref.getFactoryClassName();

            if (factoryClassName != null) {
                logger.warnf("Referral refence contains an object factory %s but it will be ignored", factoryClassName);
            }

            String ldapUrl = getLdapUrl(ref);

            if (ldapUrl != null) {
                return new DirContextObjectFactory(ldapUrl);
            }
        } else {
            logger.debugf("Unsupported reference object of type %s: ", obj);
            return this;
        }

        throw new CommunicationException("Referral reference does not contain an LDAP URL: " + obj);
    }

    @Override
    public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable<?, ?> env) {
        if (env != null && env.containsKey(IS_KC_OBJECT_FACTORY_BUILDER)) {
            return this;
        }
        return obj;
    }

    private String getLdapUrl(Reference ref) {
        for (int i = 0; i < ref.size(); i++) {
            RefAddr addr = ref.get(i);
            String addrType = addr.getType();

            if ("URL".equalsIgnoreCase(addrType)) {
                Object content = addr.getContent();

                if (content == null) {
                    return null;
                }

                String rawUrl = content.toString();

                for (String url : List.of(rawUrl.split(" "))) {
                    if (!url.toLowerCase().startsWith("ldap")) {
                        logger.warnf("Unsupported scheme from reference URL %s. Ignoring reference.", url);
                        return null;
                    }
                }

                return rawUrl;
            } else {
                logger.warnf("Ignoring address of type '%s' from referral reference", addrType);
            }
        }

        return null;
    }

    /** 按 referral URL 克隆环境并打开会话绑定的 LDAP 上下文。 */
    private record DirContextObjectFactory(String ldapUrl) implements ObjectFactory {

        @Override
        public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable<?, ?> env) throws Exception {
            @SuppressWarnings("unchecked")
            Hashtable<Object, Object> newEnv = (Hashtable<Object, Object>) env.clone();
            newEnv.put(LdapContext.PROVIDER_URL, ldapUrl);
            return new SessionBoundInitialLdapContext(KeycloakSessionUtil.getKeycloakSession(), newEnv, null);
        }
    }
}
