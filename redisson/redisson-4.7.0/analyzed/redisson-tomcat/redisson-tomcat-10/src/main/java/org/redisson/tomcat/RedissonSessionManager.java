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

import jakarta.servlet.http.HttpSession;
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

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.*;

/**
 * Apache Tomcat {@link org.apache.catalina.Manager} 的 Redisson 实现。
 * <p>管理 Session 生命周期、Redis 存储、Pub/Sub 集群同步
与 ReadMode/UpdateMode 配置。
 *
 * @author Nikita Koksharov
 *
 */
public class RedissonSessionManager extends ManagerBase {

    public enum ReadMode {REDIS, MEMORY}
    public enum UpdateMode {DEFAULT, AFTER_REQUEST}
    
    private final Log log = LogFactory.getLog(RedissonSessionManager.class);
    
    /** Redisson 客户端，用于 Session 存储与 Pub/Sub。 */
    protected RedissonClient redisson;
    private String configPath;
    private Config config;
    
    private ReadMode readMode = ReadMode.REDIS;
    private UpdateMode updateMode = UpdateMode.DEFAULT;

    protected String keyPrefix = "";
    private boolean broadcastSessionEvents = false;
    private boolean broadcastSessionUpdates = true;

    private final String nodeId = UUID.randomUUID().toString();

    private MessageListener messageListener;
    
    private Codec codecToUse;

    /** 返回当前 Tomcat 节点唯一标识。 */
    public String getNodeId() { return nodeId; }

    /** 返回 Session 更新模式（DEFAULT/AFTER_REQUEST）。 */
    public String getUpdateMode() {
        return updateMode.toString();
    }

    /** 设置 Session 更新模式。 */
    public void setUpdateMode(String updateMode) {
        this.updateMode = UpdateMode.valueOf(updateMode);
    }

    /** 是否广播 Session 创建/销毁事件。 */
    public boolean isBroadcastSessionEvents() {
        return broadcastSessionEvents;
    }
    
    /** 设置是否广播 Session 生命周期事件。 */
    public void setBroadcastSessionEvents(boolean replicateSessionEvents) {
        this.broadcastSessionEvents = replicateSessionEvents;
    }

    /** 是否广播 Session 属性变更。 */
    public boolean isBroadcastSessionUpdates() {
        return broadcastSessionUpdates;
    }

    /** 设置是否广播属性变更到集群。 */
    public void setBroadcastSessionUpdates(boolean broadcastSessionUpdates) {
        this.broadcastSessionUpdates = broadcastSessionUpdates;
    }

    /** 返回 Session 读取模式（REDIS/MEMORY）。 */
    public String getReadMode() {
        return readMode.toString();
    }

    /** 设置 Session 读取模式。 */
    public void setReadMode(String readMode) {
        this.readMode = ReadMode.valueOf(readMode);
    }
    
    /** 设置 Redisson YAML/JSON 配置文件路径。 */
    public void setConfigPath(String configPath) {
        this.configPath = configPath;
    }
    
    /** 返回 Redisson 配置文件路径。 */
    public String getConfigPath() {
        return configPath;
    }

    /** CONFIG SET：设置配置项。 */
    public void setConfig(Config config) {
        this.config = config;
    }

    /** CONFIG GET：读取配置项。 */
    public Config getConfig() {
        return config;
    }

    /** 返回 Redis key 前缀。 */
    public String getKeyPrefix() {
        return keyPrefix;
    }

    /** 设置 Session 在 Redis 中的 key 前缀。 */
    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }

    /** getName：Redis 命令实现。 */
    @Override
    public String getName() {
        return RedissonSessionManager.class.getSimpleName();
    }
    
    /** 从 Redis RMap 加载 Session 属性到内存。 */
    @Override
    public void load() throws ClassNotFoundException, IOException {
    }

    /** unload：Redis 命令实现。 */
    @Override
    public void unload() throws IOException {
    }

    /** 创建新的 {@link RedissonSession} 实例。 */
    @Override
    public Session createSession(String sessionId) {
        Session session = super.createSession(sessionId);
        
        if (broadcastSessionEvents) {
            getTopic().publish(new SessionCreatedMessage(getNodeId(), session.getId()));
        }
        return session;
    }

    /** getNotifiedNodes：Redis 命令实现。 */
    public RSet<String> getNotifiedNodes(String sessionId) {
        String separator = keyPrefix == null || keyPrefix.isEmpty() ? "" : ":";
        String name = keyPrefix + separator + "redisson:tomcat_notified_nodes:" + sessionId;
        return redisson.getSet(name, StringCodec.INSTANCE);
    }
    
    /** getTomcatSessionKeyName：Redis 命令实现。 */
    public String getTomcatSessionKeyName(String sessionId) {
        String separator = keyPrefix == null || keyPrefix.isEmpty() ? "" : ":";
        return keyPrefix + separator + "redisson:tomcat_session:" + sessionId;
    }

    /** getMap：Redis 命令实现。 */
    public RMap<String, Object> getMap(String sessionId) {
        String name = getTomcatSessionKeyName(sessionId);
        return redisson.getMap(name, new CompositeCodec(StringCodec.INSTANCE, codecToUse, codecToUse));
    }

    /** 返回 Pub/Sub 主题，用于跨节点 Session 同步。 */
    public RTopic getTopic() {
        String separator = keyPrefix == null || keyPrefix.isEmpty() ? "" : ":";
        final String name = keyPrefix + separator + "redisson:tomcat_session_updates:" + getContext().getName();
        PublishSubscribeService ss = ((Redisson) redisson).getCommandExecutor().getConnectionManager().getSubscribeService();
        if (ss.isShardingSupported()) {
            return redisson.getShardedTopic(name);
        }
        return redisson.getTopic(name);
    }
    
    /** 按 ID 查找 Session，不存在则返回 null。 */
    @Override
    public Session findSession(String id) throws IOException {
        return findSession(id, true);
    }
    
    /** 按 ID 查找 Session，不存在则返回 null。 */
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
    
    /** createEmptySession：Redis 命令实现。 */
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
    
    /** superRemove：Redis 命令实现。 */
    public void superRemove(Session session) {
        super.remove(session, false);
    }

    /** remove：Redis 命令实现。 */
    @Override
    public void remove(Session session, boolean update) {
        super.remove(session, update);
        
        if (session.getIdInternal() != null
                && !redisson.isShuttingDown()) {
            ((RedissonSession)session).delete();
        }
    }
    
    /** superAdd：Redis 命令实现。 */
    public void superAdd(Session session) {
        super.add(session);
    }

    /** add：Redis 命令实现。 */
    @Override
    public void add(Session session) {
        super.add(session);
        ((RedissonSession)session).save();
    }
    
    /** getRedisson：Redis 命令实现。 */
    public RedissonClient getRedisson() {
        return redisson;
    }
    
    /** Tomcat 生命周期：初始化 Redisson 客户端与 Pub/Sub 监听。 */
    @Override
    protected void startInternal() throws LifecycleException {
        super.startInternal();
        redisson = buildClient();
        
        final ClassLoader applicationClassLoader;
        if (getContext().getLoader().getClassLoader() != null) {
            applicationClassLoader = getContext().getLoader().getClassLoader();
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
        
        Pipeline pipeline = getContext().getPipeline();
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

    /** buildClient：Redis 命令实现。 */
    protected RedissonClient buildClient() throws LifecycleException {
        if (config == null) {
            if (configPath == null) {
                throw new LifecycleException(
                        "Either a Config object (via setConfig) or a configPath must be provided");
            }
            try {
                config = Config.fromYAML(new File(configPath), getClass().getClassLoader());
            } catch (IOException e) {
                throw new LifecycleException("Can't parse yaml config " + configPath, e);
            }
        }

        try {
            return Redisson.create(config);
        } catch (Exception e) {
            throw new LifecycleException(e);
        }
    }

    /** Tomcat 生命周期：关闭 Redisson 客户端并清理资源。 */
    @Override
    protected void stopInternal() throws LifecycleException {
        super.stopInternal();
        
        setState(LifecycleState.STOPPING);

        Pipeline pipeline = getContext().getPipeline();
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

    /** shutdownRedisson：Redis 命令实现。 */
    protected void shutdownRedisson() {
        if (redisson != null) {
            redisson.shutdown();
        }
    }

    /** 将 Session 状态持久化到 Redis RMap。 */
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

    /** tryInitSsoValve：Redis 命令实现。 */
    private void tryInitSsoValve() {
        Container c = getContext();
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
