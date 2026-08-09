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

import org.apache.catalina.*;
import org.apache.catalina.session.ManagerBase;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.redisson.Redisson;
import org.redisson.api.RMap;
import org.redisson.api.RSet;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.api.listener.MessageListener;
import org.redisson.client.codec.Codec;
import org.redisson.client.codec.StringCodec;
import org.redisson.codec.CompositeCodec;
import org.redisson.config.Config;
import org.redisson.pubsub.PublishSubscribeService;

import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.*;

/**
 * Apache Tomcat {@link org.apache.catalina.Manager}：以 Redis/Valkey 持久化 HTTP Session。
 * <p>支持 YAML 配置、ReadMode（REDIS/MEMORY）、UpdateMode（DEFAULT/AFTER_REQUEST）、
 * Session 事件广播与跨节点属性同步 Topic。
 *
 * @author Nikita Koksharov
 */
public class RedissonSessionManager extends ManagerBase {

    /** Session 属性读取模式：REDIS 按需加载，MEMORY 本地缓存。 */
    public enum ReadMode {REDIS, MEMORY}
    /** Session 写回模式：DEFAULT 即时写入，AFTER_REQUEST 请求结束批量写回。 */
    public enum UpdateMode {DEFAULT, AFTER_REQUEST}
    
    private final Log log = LogFactory.getLog(RedissonSessionManager.class);
    
    /** 底层 Redisson 客户端。 */
    protected RedissonClient redisson;
    private String configPath;
    private Config config;
    
    /** 当前 Session 读取模式。 */
    private ReadMode readMode = ReadMode.REDIS;
    /** 当前 Session 写回模式。 */
    private UpdateMode updateMode = UpdateMode.DEFAULT;

    protected String keyPrefix = "";
    private boolean broadcastSessionEvents = false;
    private boolean broadcastSessionUpdates = true;

    /** 本 Tomcat 节点唯一标识，用于集群 Topic 消息来源。 */
    private final String nodeId = UUID.randomUUID().toString();

    private MessageListener messageListener;
    
    private Codec codecToUse;

    /** 返回本 Tomcat 节点 ID。 */
    public String getNodeId() { return nodeId; }

    /** 获取 UpdateMode。 */
    public String getUpdateMode() {
        return updateMode.toString();
    }

    /** 设置UpdateMode。 */
    public void setUpdateMode(String updateMode) {
        this.updateMode = UpdateMode.valueOf(updateMode);
    }

    /** 是否BroadcastSessionEvents。 */
    public boolean isBroadcastSessionEvents() {
        return broadcastSessionEvents;
    }
    
    /** 设置BroadcastSessionEvents。 */
    public void setBroadcastSessionEvents(boolean replicateSessionEvents) {
        this.broadcastSessionEvents = replicateSessionEvents;
    }

    /** 是否BroadcastSessionUpdates。 */
    public boolean isBroadcastSessionUpdates() {
        return broadcastSessionUpdates;
    }

    /** 设置BroadcastSessionUpdates。 */
    public void setBroadcastSessionUpdates(boolean broadcastSessionUpdates) {
        this.broadcastSessionUpdates = broadcastSessionUpdates;
    }

    /** 获取 ReadMode。 */
    public String getReadMode() {
        return readMode.toString();
    }

    /** 设置ReadMode。 */
    public void setReadMode(String readMode) {
        this.readMode = ReadMode.valueOf(readMode);
    }
    
    /** 设置ConfigPath。 */
    public void setConfigPath(String configPath) {
        this.configPath = configPath;
    }
    
    /** 获取 ConfigPath。 */
    public String getConfigPath() {
        return configPath;
    }

    /** 设置Config。 */
    public void setConfig(Config config) {
        this.config = config;
    }

    /** 获取 Config。 */
    public Config getConfig() {
        return config;
    }

    /** 获取 KeyPrefix。 */
    public String getKeyPrefix() {
        return keyPrefix;
    }

    /** 设置KeyPrefix。 */
    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }

    /** 获取 Name。 */
    @Override
    public String getName() {
        return RedissonSessionManager.class.getSimpleName();
    }
    
    /** Tomcat 生命周期：加载 Session 管理器。 */
    @Override
    public void load() throws ClassNotFoundException, IOException {
    }

    /** Tomcat 生命周期：卸载 Session 管理器。 */
    @Override
    public void unload() throws IOException {
    }

    /** 创建或加载指定 ID 的 Session。 */
    @Override
    public Session createSession(String sessionId) {
        Session session = super.createSession(sessionId);
        
        if (broadcastSessionEvents) {
            getTopic().publish(new SessionCreatedMessage(getNodeId(), session.getId()));
        }
        return session;
    }

    /** 返回已处理 Session 销毁通知的节点集合。 */
    public RSet<String> getNotifiedNodes(String sessionId) {
        String separator = keyPrefix == null || keyPrefix.isEmpty() ? "" : ":";
        String name = keyPrefix + separator + "redisson:tomcat_notified_nodes:" + sessionId;
        return redisson.getSet(name, StringCodec.INSTANCE);
    }

    /** 构造 Session 在 Redis 中的完整键名。 */
    public String getTomcatSessionKeyName(String sessionId) {
        String separator = keyPrefix == null || keyPrefix.isEmpty() ? "" : ":";
        return keyPrefix + separator + "redisson:tomcat_session:" + sessionId;
    }

    /** 获取 Session 属性 Map 或通用 Redis Map。 */
    public RMap<String, Object> getMap(String sessionId) {
        String name = getTomcatSessionKeyName(sessionId);
        return redisson.getMap(name, new CompositeCodec(StringCodec.INSTANCE, codecToUse, codecToUse));
    }

    /** 返回 Session 集群同步 Topic。 */
    public RTopic getTopic() {
        String separator = keyPrefix == null || keyPrefix.isEmpty() ? "" : ":";
        String name = keyPrefix + separator + "redisson:tomcat_session_updates:" + getContainer().getName();
        PublishSubscribeService ss = ((Redisson) redisson).getCommandExecutor().getConnectionManager().getSubscribeService();
        if (ss.isShardingSupported()) {
            return redisson.getShardedTopic(name);
        }
        return redisson.getTopic(name);
    }
    
    /** 按 ID 查找 Session；不存在时返回 null。 */
    @Override
    public Session findSession(String id) throws IOException {
        return findSession(id, true);
    }
    
    /** 按 ID 查找 Session；不存在时返回 null。 */
    private Session findSession(String id, boolean notify) throws IOException {
        RedissonSession result = (RedissonSession) super.findSession(id);
        if (result == null) {
            if (id != null) {
                Map<String, Object> attrs = new HashMap<String, Object>();
                try {
                    attrs = getMap(id).getAll(RedissonSession.ATTRS);
                } catch (Exception e) {
                    log.error("Can't read session object by id: " + id, e);
                }

                if (attrs.isEmpty() || (broadcastSessionEvents && getNotifiedNodes(id).contains(nodeId))) {
                    log.debug("Session " + id + " can't be found");
                    return null;    
                }
                
                RedissonSession session = (RedissonSession) createEmptySession();
                session.load(attrs);
                session.setId(id, notify);
                
                session.superAccess();
                session.endAccess();
                return session;
            }
            return null;
        }

        result.superAccess();
        result.endAccess();
        
        return result;
    }

    /** 创建无 ID 的空 Session（由 Tomcat 分配 ID）。 */
    @Override
    public Session createEmptySession() {
        Session session = new RedissonSession(this, readMode, updateMode, broadcastSessionEvents, this.broadcastSessionUpdates);

        if (broadcastSessionEvents) {
            session.addSessionListener(event -> {
                if (event.getType().equals(Session.SESSION_DESTROYED_EVENT)) {
                    getTopic().publish(new SessionDestroyedMessage(getNodeId(), session.getId()));
                }
            });
        }
        return session;
    }
    
    /** 委托父类 remove Session。 */
    public void superRemove(Session session) {
        super.remove(session, false);
    }

    /** 移除键或元素。 */
    @Override
    public void remove(Session session, boolean update) {
        super.remove(session, update);

        if (session.getIdInternal() != null
                && !redisson.isShuttingDown()) {
            ((RedissonSession)session).delete();
        }
    }
    
    /** 委托父类 add Session。 */
    public void superAdd(Session session) {
        super.add(session);
    }

    /** 向布隆过滤器添加元素。 */
    @Override
    public void add(Session session) {
        super.add(session);
        ((RedissonSession)session).save();
    }
    
    /** 返回底层 Redisson 客户端。 */
    public RedissonClient getRedisson() {
        return redisson;
    }
    
    /** 启动管理器：创建 Redisson 客户端并订阅集群 Topic。 */
    @Override
    protected void startInternal() throws LifecycleException {
        super.startInternal();
        redisson = buildClient();
        
        final ClassLoader applicationClassLoader;
        if (getContainer().getLoader().getClassLoader() != null) {
            applicationClassLoader = getContainer().getLoader().getClassLoader();
        } else if (Thread.currentThread().getContextClassLoader() != null) {
            applicationClassLoader = Thread.currentThread().getContextClassLoader();
        } else {
            applicationClassLoader = getClass().getClassLoader();
        }
        
        Codec codec = redisson.getConfig().getCodec();
        try {
            codecToUse = codec.getClass()
                    .getConstructor(ClassLoader.class, codec.getClass())
                    .newInstance(applicationClassLoader, codec);
        } catch (Exception e) {
            throw new LifecycleException(e);
        }
        
        Pipeline pipeline = getContainer().getPipeline();
        synchronized (pipeline) {
            tryInitSsoValve();
            if (readMode == ReadMode.REDIS) {
                Optional<Valve> res = Arrays.stream(pipeline.getValves()).filter(v -> v.getClass() == UsageValve.class).findAny();
                if (res.isPresent()) {
                    ((UsageValve)res.get()).incUsage();
                } else {
                    pipeline.addValve(new UsageValve());
                }
            }
            if (updateMode == UpdateMode.AFTER_REQUEST) {
                Optional<Valve> res = Arrays.stream(pipeline.getValves()).filter(v -> v.getClass() == UpdateValve.class).findAny();
                if (res.isPresent()) {
                    ((UpdateValve)res.get()).incUsage();
                } else {
                    pipeline.addValve(new UpdateValve());
                }
            }
        }
        
        if (readMode == ReadMode.MEMORY && this.broadcastSessionUpdates || broadcastSessionEvents) {
            RTopic updatesTopic = getTopic();
            messageListener = (MessageListener<AttributeMessage>) (channel, msg) -> {
                try {
                    if (msg.getNodeId().equals(nodeId)) {
                        return;
                    }

                    RedissonSession session = (RedissonSession) RedissonSessionManager.super.findSession(msg.getSessionId());
                    if (session != null) {
                        if (msg instanceof SessionDestroyedMessage) {
                            session.expire();
                        }

                        if (msg instanceof AttributeRemoveMessage) {
                            for (String name : ((AttributeRemoveMessage)msg).getNames()) {
                                session.superRemoveAttributeInternal(name, true);
                            }
                        }

                        if (msg instanceof AttributesClearMessage) {
                            RedissonSessionManager.super.remove(session, false);
                        }

                        if (msg instanceof AttributesPutAllMessage) {
                            AttributesPutAllMessage m = (AttributesPutAllMessage) msg;
                            Map<String, Object> attrs = m.getAttrs(codecToUse.getMapValueDecoder());
                            session.load(attrs);
                        }

                        if (msg instanceof AttributeUpdateMessage) {
                            AttributeUpdateMessage m = (AttributeUpdateMessage)msg;
                            Map<String, Object> attrs = new HashMap<>();
                            attrs.put(m.getName(), m.getValue(codecToUse.getMapValueDecoder()));
                            session.load(attrs);
                        }
                    } else {
                        if (msg instanceof SessionCreatedMessage) {
                            findSession(msg.getSessionId());
                        }

                        if (msg instanceof SessionDestroyedMessage) {
                            Session s = findSession(msg.getSessionId(), false);
                            if (s != null) {
                                s.expire();
                            }
                            RSet<String> set = getNotifiedNodes(msg.getSessionId());
                            set.add(nodeId);
                            set.expire(Duration.ofSeconds(60));
                        }

                    }

                } catch (Exception e) {
                    log.error("Unable to handle topic message", e);
                }
            };
            
            updatesTopic.addListener(AttributeMessage.class, messageListener);
        }
        
        setState(LifecycleState.STARTING);
    }

    /** 根据 configPath 或 config 构建 {@link RedissonClient}。 */
    protected RedissonClient buildClient() throws LifecycleException {
        if (config == null) {
            if (configPath == null) {
                throw new LifecycleException(
                        "Either a Config object (via setConfig) or a configPath must be provided");
            }
            try {
                config = Config.fromYAML(new File(configPath), getClass().getClassLoader());
            } catch (Exception e) {
                throw new LifecycleException("Can't parse yaml config " + configPath, e);
            }
        }

        try {
            return Redisson.create(config);
        } catch (Exception e) {
            throw new LifecycleException(e);
        }
    }

    /** 停止管理器：取消订阅并关闭 Redisson 客户端。 */
    @Override
    protected void stopInternal() throws LifecycleException {
        super.stopInternal();
        
        setState(LifecycleState.STOPPING);
        
        Pipeline pipeline = getContainer().getPipeline();
        synchronized (pipeline) {
            if (readMode == ReadMode.REDIS) {
                Arrays.stream(pipeline.getValves()).filter(v -> v.getClass() == UsageValve.class).forEach(v -> {
                    if (((UsageValve)v).decUsage() == 0){
                        pipeline.removeValve(v);
                    }
                });
            }
            if (updateMode == UpdateMode.AFTER_REQUEST) {
                Arrays.stream(pipeline.getValves()).filter(v -> v.getClass() == UpdateValve.class).forEach(v -> {
                    if (((UpdateValve)v).decUsage() == 0){
                        pipeline.removeValve(v);
                    }
                });
            }
        }
        
        if (messageListener != null) {
             RTopic updatesTopic = getTopic();
             updatesTopic.removeListener(messageListener);
        }

        codecToUse = null;

        try {
            shutdownRedisson();
        } catch (Exception e) {
            throw new LifecycleException(e);
        }
        
    }

    /** 关闭 Redisson 客户端并释放连接。 */
    protected void shutdownRedisson() {
        if (redisson != null) {
            redisson.shutdown();
        }
    }

    /** 将 Session 变更持久化到 Redis 并广播更新。 */
    public void store(HttpSession session) throws IOException {
        if (session == null) {
            return;
        }
        
        RedissonSession sess = (RedissonSession) super.findSession(session.getId());
        if (sess != null) {
            sess.superAccess();
            sess.superEndAccess();
            sess.save();
        }
    }

    /** Tomcat Session tryInitSsoValve 操作。 */
    private void tryInitSsoValve() {
        Container c = getContainer();
        // SSO valve has to be in defined in Host
        // it won't be picked up by Catalina from within Context
        while (c != null && !(c instanceof org.apache.catalina.Host)) {
            c = c.getParent();
        }
        if (c == null) {
            log.warn("No Catalina Host found for current context. Can't configure Redisson SSO.");
            return;
        }
        for (Valve valve : ((Host) c).getPipeline().getValves()) {
            if (valve instanceof RedissonSingleSignOn) {
                log.debug("Found SSO valve, passing RedissionSessionManager to it.");
                ((RedissonSingleSignOn) valve).setSessionManager(this);
                return;
            }
        }
        log.trace("No Redisson SSO valve found. Redisson SSO is not configured.");
    }

}
