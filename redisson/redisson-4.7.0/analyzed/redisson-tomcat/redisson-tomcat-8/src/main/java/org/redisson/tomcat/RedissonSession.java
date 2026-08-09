/**
 * Copyright (c) 2013-2026 Nikita Koksharov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.redisson.tomcat;

import org.apache.catalina.session.StandardSession;
import org.redisson.api.RMap;
import org.redisson.api.RSet;
import org.redisson.api.RTopic;
import org.redisson.client.protocol.Encoder;
import org.redisson.tomcat.RedissonSessionManager.ReadMode;
import org.redisson.tomcat.RedissonSessionManager.UpdateMode;

import java.io.IOException;
import java.lang.reflect.Field;
import java.security.Principal;
import java.time.Duration;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 基于 Redis {@link RMap} 的 Apache Tomcat {@link org.apache.catalina.Session} 实现。
 * <p>支持 {@link RedissonSessionManager.ReadMode#REDIS} 按需从 Redis 读取属性
 * 与 {@link RedissonSessionManager.UpdateMode#AFTER_REQUEST} 请求末批量写回两种模式；
 * 通过 {@link RTopic} 在集群节点间同步 Session 变更。
 *
 * @author Nikita Koksharov
 */
public class RedissonSession extends StandardSession {

    private static final String IS_NEW_ATTR = "session:isNew";
    private static final String IS_VALID_ATTR = "session:isValid";
    private static final String THIS_ACCESSED_TIME_ATTR = "session:thisAccessedTime";
    private static final String MAX_INACTIVE_INTERVAL_ATTR = "session:maxInactiveInterval";
    private static final String LAST_ACCESSED_TIME_ATTR = "session:lastAccessedTime";
    private static final String CREATION_TIME_ATTR = "session:creationTime";
    private static final String IS_EXPIRATION_LOCKED = "session:isExpirationLocked";
    private static final String PRINCIPAL_ATTR = "session:principal";
    private static final String AUTHTYPE_ATTR = "session:authtype";
    
    public static final Set<String> ATTRS = new HashSet<String>(Arrays.asList(
            IS_NEW_ATTR, IS_VALID_ATTR, 
            THIS_ACCESSED_TIME_ATTR, MAX_INACTIVE_INTERVAL_ATTR, 
            LAST_ACCESSED_TIME_ATTR, CREATION_TIME_ATTR, IS_EXPIRATION_LOCKED,
            PRINCIPAL_ATTR, AUTHTYPE_ATTR
            ));
    
    private boolean isExpirationLocked;
    private boolean loaded;
    /** 所属 Session 管理器。 */
    private final RedissonSessionManager redissonManager;
    private final Map<String, Object> attrs;
    /** 持久化 Session 属性的 Redis Map。 */
    private RMap<String, Object> map;
    /** 集群 Session 变更广播 Topic。 */
    private final RTopic topic;
    /** Session 属性读取模式。 */
    private final ReadMode readMode;
    /** Session 属性写回模式。 */
    private final UpdateMode updateMode;

    /** 当前请求对 Session 的并发使用计数。 */
    private final AtomicInteger usages = new AtomicInteger();
    private Map<String, Object> loadedAttributes = Collections.emptyMap();
    private Map<String, Object> updatedAttributes = Collections.emptyMap();
    private Set<String> removedAttributes = Collections.emptySet();

    private final boolean broadcastSessionEvents;
    private final boolean broadcastSessionUpdates;

    public RedissonSession(RedissonSessionManager manager, ReadMode readMode, UpdateMode updateMode, boolean broadcastSessionEvents, boolean broadcastSessionUpdates) {
        super(manager);
        this.redissonManager = manager;
        this.readMode = readMode;
        this.updateMode = updateMode;
        this.topic = redissonManager.getTopic();
        this.broadcastSessionEvents = broadcastSessionEvents;
        this.broadcastSessionUpdates = broadcastSessionUpdates;
        
        if (updateMode == UpdateMode.AFTER_REQUEST) {
            removedAttributes = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
        }
        if (readMode == ReadMode.REDIS) {
            loadedAttributes = new ConcurrentHashMap<>();
            updatedAttributes = new ConcurrentHashMap<>();
        }
        
        try {
            Field attr = StandardSession.class.getDeclaredField("attributes");
            attrs = (Map<String, Object>) attr.get(this);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static final long serialVersionUID = -2518607181636076487L;

    /** 读取 Session 属性；REDIS 模式下按需从 Redis 加载。 */
    @Override
    public Object getAttribute(String name) {
        if (readMode == ReadMode.REDIS) {
            if (!isValidInternal()) {
                throw new IllegalStateException(sm.getString("standardSession.getAttribute.ise"));
            }

            if (name == null) {
                return null;
            }

            if (removedAttributes.contains(name)) {
                return super.getAttribute(name);
            }

            Object value = loadedAttributes.get(name);
            if (value == null) {
                value = map.get(name);
                if (value != null) {
                    loadedAttributes.put(name, value);
                }
            }

            return value;
        } else {
            if (!loaded) {
                synchronized (this) {
                    if (!loaded) {
                        Map<String, Object> storedAttrs = map.readAllMap();
                        
                        load(storedAttrs);
                        loaded = true;
                    }
                }
            }
        }

        return super.getAttribute(name);
    }
    
    /** 返回全部属性名枚举。 */
    @Override
    public Enumeration<String> getAttributeNames() {
        if (readMode == ReadMode.REDIS) {
            if (!isValidInternal()) {
                throw new IllegalStateException
                    (sm.getString("standardSession.getAttributeNames.ise"));
            }

            Set<String> attributeKeys = new HashSet<>();
            attributeKeys.addAll(map.readAllKeySet());
            attributeKeys.addAll(loadedAttributes.keySet());
            return Collections.enumeration(attributeKeys);
        }
        
        return super.getAttributeNames();
    }

    /** 返回用户属性名数组（不含内部元数据键）。 */
    @Override
    public String[] getValueNames() {
        if (readMode == ReadMode.REDIS) {
            if (!isValidInternal()) {
                throw new IllegalStateException
                    (sm.getString("standardSession.getAttributeNames.ise"));
            }
            Set<String> keys = map.readAllKeySet();
            return keys.toArray(new String[keys.size()]);
        }
        
        return super.getValueNames();
    }
    
    /** 删除键或 Session。 */
    public void delete() {
        if (map == null) {
            map = redissonManager.getMap(id);
        }
        
        if (broadcastSessionEvents) {
            RSet<String> set = redissonManager.getNotifiedNodes(id);
            set.add(redissonManager.getNodeId());
            set.expire(Duration.ofSeconds(60));
            map.fastPut(IS_EXPIRATION_LOCKED, true);
            map.expire(Duration.ofSeconds(60));
        } else {
            map.delete();
        }
        if (readMode == ReadMode.MEMORY && this.broadcastSessionUpdates) {
            topic.publish(new AttributesClearMessage(redissonManager.getNodeId(), getId()));
        }
        map = null;
        loadedAttributes.clear();
        updatedAttributes.clear();
    }
    
    /** 设置 Session 创建时间戳。 */
    @Override
    public void setCreationTime(long time) {
        super.setCreationTime(time);

        if (map != null) {
            Map<String, Object> newMap = new HashMap<String, Object>(3);
            newMap.put(CREATION_TIME_ATTR, creationTime);
            newMap.put(LAST_ACCESSED_TIME_ATTR, lastAccessedTime);
            newMap.put(THIS_ACCESSED_TIME_ATTR, thisAccessedTime);
            map.putAll(newMap);
            if (readMode == ReadMode.MEMORY && this.broadcastSessionUpdates) {
                topic.publish(createPutAllMessage(newMap));
            }
        }
    }
    
    /** 更新 Session 最后访问时间。 */
    @Override
    public void access() {
        super.access();

        fastPut(THIS_ACCESSED_TIME_ATTR, thisAccessedTime);
        expireSession();
    }

    /** 委托 {@link StandardSession#access()}。 */
    public void superAccess() {
        super.access();
    }

    /** 委托 {@link StandardSession#endAccess()}。 */
    public void superEndAccess() {
        super.endAccess();
    }

    /** 触发 Session 过期逻辑。 */
    protected void expireSession() {
        RMap<String, Object> m = map;
        if (isExpirationLocked || m == null) {
            return;
        }
        if (maxInactiveInterval >= 0) {
            m.expire(Duration.ofSeconds(maxInactiveInterval + 60));
        }
    }

    /** 构造批量属性更新集群消息。 */
    protected AttributesPutAllMessage createPutAllMessage(Map<String, Object> newMap) {
        try {
            return new AttributesPutAllMessage(redissonManager, getId(), newMap, this.map.getCodec().getMapValueEncoder());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
    
    /** 设置 Session 最大非活动间隔（秒）。 */
    @Override
    public void setMaxInactiveInterval(int interval) {
        super.setMaxInactiveInterval(interval);

        fastPut(MAX_INACTIVE_INTERVAL_ATTR, maxInactiveInterval);
        expireSession();
    }

    /** 快速写入属性到 Redis Map。 */
    private void fastPut(String name, Object value) {
        RMap<String, Object> m = map;
        if (m == null) {
            return;
        }
        m.fastPut(name, value);
        if (readMode == ReadMode.MEMORY && this.broadcastSessionUpdates) {
            try {
                Encoder encoder = m.getCodec().getMapValueEncoder();
                topic.publish(new AttributeUpdateMessage(redissonManager.getNodeId(), getId(), name, value, encoder));
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    /** 设置认证主体并持久化。 */
    @Override
    public void setPrincipal(Principal principal) {
        super.setPrincipal(principal);

        if (principal == null) {
            removeRedisAttribute(PRINCIPAL_ATTR);
        } else {
            fastPut(PRINCIPAL_ATTR, principal);
        }
    }

    /** 设置认证类型并持久化。 */
    @Override
    public void setAuthType(String authType) {
        super.setAuthType(authType);

        if (authType == null) {
            removeRedisAttribute(AUTHTYPE_ATTR);
        } else {
            fastPut(AUTHTYPE_ATTR, authType);
        }
    }

    /** 设置 Session 有效标志。 */
    @Override
    public void setValid(boolean isValid) {
        super.setValid(isValid);
        
        if (map != null) {
            if (!isValid && !map.isExists()) {
                return;
            }
            
            fastPut(IS_VALID_ATTR, isValid);
        }
    }
    
    /** 设置 Session 是否为新创建。 */
    @Override
    public void setNew(boolean isNew) {
        super.setNew(isNew);
        
        fastPut(IS_NEW_ATTR, isNew);
    }
    
    /** 结束访问并触发过期检查或写回。 */
    @Override
    public void endAccess() {
        boolean oldValue = isNew;
        super.endAccess();

        RMap<String, Object> m = map;
        if (m != null) {
            Map<String, Object> newMap = new HashMap<>(3);
            if (isNew != oldValue) {
                newMap.put(IS_NEW_ATTR, isNew);
            }
            newMap.put(LAST_ACCESSED_TIME_ATTR, lastAccessedTime);
            newMap.put(THIS_ACCESSED_TIME_ATTR, thisAccessedTime);
            m.putAll(newMap);
            if (readMode == ReadMode.MEMORY && this.broadcastSessionUpdates) {
                topic.publish(createPutAllMessage(newMap));
            }
            expireSession();
        }
    }
    
    /** 设置 Session 属性并标记变更。 */
    @Override
    public void setAttribute(String name, Object value, boolean notify) {
        super.setAttribute(name, value, notify);
        
        if (value == null) {
            return;
        }
        if (updateMode == UpdateMode.DEFAULT) {
            fastPut(name, value);
        }
        if (readMode == ReadMode.REDIS) {
            loadedAttributes.put(name, value);
            updatedAttributes.put(name, value);
        }
        if (updateMode == UpdateMode.AFTER_REQUEST) {
            removedAttributes.remove(name);
        }
    }
    
    /** 委托父类移除属性逻辑。 */
    public void superRemoveAttributeInternal(String name, boolean notify) {
        super.removeAttributeInternal(name, notify);
    }

    /** 返回自上次访问以来的空闲毫秒数。 */
    @Override
    public long getIdleTimeInternal() {
        long idleTime = super.getIdleTimeInternal();
        if (map != null && readMode == ReadMode.REDIS) {
            if (idleTime >= getMaxInactiveInterval() * 1000) {
                load(map.getAll(RedissonSession.ATTRS));
                idleTime = super.getIdleTimeInternal();
            }
        }
        return idleTime;
    }

    /** removeAttributeInternal：移除操作。 */
    @Override
    protected void removeAttributeInternal(String name, boolean notify) {
        super.removeAttributeInternal(name, notify);

        removeRedisAttribute(name);
    }

    /** removeRedisAttribute：移除操作。 */
    private void removeRedisAttribute(String name) {
        if (updateMode == UpdateMode.DEFAULT && map != null) {
            map.fastRemove(name);
            if (readMode == ReadMode.MEMORY && this.broadcastSessionUpdates) {
                topic.publish(new AttributeRemoveMessage(redissonManager.getNodeId(), getId(), new HashSet<String>(Arrays.asList(name))));
            }
        }
        if (readMode == ReadMode.REDIS) {
            loadedAttributes.remove(name);
            updatedAttributes.remove(name);
        }
        if (updateMode == UpdateMode.AFTER_REQUEST) {
            removedAttributes.add(name);
        }
    }

    /** 设置Id。 */
    @Override
    public void setId(String id, boolean notify) {
        if ((this.id != null) && (manager != null)) {
            redissonManager.superRemove(this);
            if (map == null) {
                map = redissonManager.getMap(this.id);
            }
            String newName = redissonManager.getTomcatSessionKeyName(id);
            if (!map.getName().equals(newName)) {
                map.rename(newName);
            }
        }

        boolean idWasNull = this.id == null;
        this.id = id;

        if (manager != null) {
            if (idWasNull) {
                redissonManager.add(this);
            } else {
                redissonManager.superAdd(this);
            }
        }

        if (notify) {
            tellNew();
        }
    }

    /** Tomcat Session save 操作。 */
    public void save() {
        if (map == null) {
            map = redissonManager.getMap(id);
        }
        
        Map<String, Object> newMap = new HashMap<String, Object>();
        newMap.put(CREATION_TIME_ATTR, creationTime);
        newMap.put(LAST_ACCESSED_TIME_ATTR, lastAccessedTime);
        newMap.put(THIS_ACCESSED_TIME_ATTR, thisAccessedTime);
        newMap.put(MAX_INACTIVE_INTERVAL_ATTR, maxInactiveInterval);
        newMap.put(IS_VALID_ATTR, isValid);
        newMap.put(IS_NEW_ATTR, isNew);
        if (principal != null) {
            newMap.put(PRINCIPAL_ATTR, principal);
        }
        if (authType != null) {
            newMap.put(AUTHTYPE_ATTR, authType);
        }
        if (broadcastSessionEvents) {
            newMap.put(IS_EXPIRATION_LOCKED, isExpirationLocked);
        }

        if (readMode == ReadMode.MEMORY) {
            if (attrs != null) {
                for (Entry<String, Object> entry : attrs.entrySet()) {
                    newMap.put(entry.getKey(), copy(entry.getValue()));
                }
            }
        } else {
            newMap.putAll(updatedAttributes);
            updatedAttributes.clear();
        }

        map.putAll(newMap);
        map.fastRemove(removedAttributes.toArray(new String[0]));
        
        if (readMode == ReadMode.MEMORY && this.broadcastSessionUpdates) {
            topic.publish(createPutAllMessage(newMap));
            
            if (updateMode == UpdateMode.AFTER_REQUEST) {
                if (!removedAttributes.isEmpty()) {
                    topic.publish(new AttributeRemoveMessage(redissonManager.getNodeId(), getId(), new HashSet<>(removedAttributes)));
                }
            }
        }

        removedAttributes.clear();

        expireSession();
    }

    /** 复制键到目标名称。 */
    private Object copy(Object value) {
        try {
            if (value instanceof Collection) {
                Collection newInstance = (Collection) value.getClass().getDeclaredConstructor().newInstance();
                newInstance.addAll((Collection) value);
                value = newInstance;
            }
            if (value instanceof Map) {
                Map newInstance = (Map) value.getClass().getDeclaredConstructor().newInstance();
                newInstance.putAll((Map) value);
                value = newInstance;
            }
        } catch (Exception e) {
            // can't be copied
        }
        return value;
    }
    
    /** Tomcat 生命周期：加载 Session 管理器。 */
    public void load(Map<String, Object> attrs) {
        Number creationTime = (Number) attrs.remove(CREATION_TIME_ATTR);
        if (creationTime != null) {
            this.creationTime = creationTime.longValue();
        }
        Number lastAccessedTime = (Number) attrs.remove(LAST_ACCESSED_TIME_ATTR);
        if (lastAccessedTime != null) {
            this.lastAccessedTime = lastAccessedTime.longValue();
        }
        Integer maxInactiveInterval = (Integer) attrs.remove(MAX_INACTIVE_INTERVAL_ATTR);
        if (maxInactiveInterval != null) {
            this.maxInactiveInterval = maxInactiveInterval;
        }
        Number thisAccessedTime = (Number) attrs.remove(THIS_ACCESSED_TIME_ATTR);
        if (thisAccessedTime != null) {
            this.thisAccessedTime = thisAccessedTime.longValue();
        }
        Boolean isValid = (Boolean) attrs.remove(IS_VALID_ATTR);
        if (isValid != null) {
            this.isValid = isValid;
        }
        Boolean isNew = (Boolean) attrs.remove(IS_NEW_ATTR);
        if (isNew != null) {
            this.isNew = isNew;
        }
        Boolean isExpirationLocked = (Boolean) attrs.remove(IS_EXPIRATION_LOCKED);
        if (isExpirationLocked != null) {
            this.isExpirationLocked = isExpirationLocked;
        }
        Principal p = (Principal) attrs.remove(PRINCIPAL_ATTR);
        if (p != null) {
            this.principal = p;
        }
        String authType = (String) attrs.remove(AUTHTYPE_ATTR);
        if (authType != null) {
            this.authType = authType;
        }

        if (readMode == ReadMode.MEMORY) {
            for (Entry<String, Object> entry : attrs.entrySet()) {
                super.setAttribute(entry.getKey(), entry.getValue(), false);
            }
        }
    }
    
    /** Tomcat Session recycle 操作。 */
    @Override
    public void recycle() {
        super.recycle();
        map = null;
        loadedAttributes.clear();
        updatedAttributes.clear();
        removedAttributes.clear();
    }

    /** 递增使用计数，防止并发覆盖。 */
    public void startUsage() {
        usages.incrementAndGet();
    }

    /** 递减使用计数。 */
    public void endUsage() {
        // don't decrement usages if startUsage wasn't called
//        if (usages.decrementAndGet() == 0) {
        if (usages.get() == 0 || usages.decrementAndGet() == 0) {
            loadedAttributes.clear();
        }
    }
}
