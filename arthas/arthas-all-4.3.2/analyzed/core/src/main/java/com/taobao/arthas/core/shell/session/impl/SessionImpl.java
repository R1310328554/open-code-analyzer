package com.taobao.arthas.core.shell.session.impl;

import com.taobao.arthas.core.distribution.SharingResultDistributor;
import com.taobao.arthas.core.shell.command.CommandResolver;
import com.taobao.arthas.core.shell.session.Session;
import com.taobao.arthas.core.shell.system.Job;
import com.taobao.arthas.core.shell.system.impl.InternalCommandManager;

import java.lang.instrument.Instrumentation;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * {@link Session} 默认实现：ConcurrentHashMap 存储属性 + 原子锁序列。
 * <p>
 * 构造时写入创建时间与最后活跃时间；锁采用全局递增序号标识持有者。
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class SessionImpl implements Session {
    /** 全局锁序号生成器，每次 tryLock 成功递增 */
    private final static AtomicInteger lockSequence = new AtomicInteger();
    /** 表示会话未加锁的锁字段哨兵值 */
    private final static int LOCK_TX_EMPTY = -1;
    /** 当前锁持有者序号，-1 表示空闲 */
    private final AtomicInteger lock = new AtomicInteger(LOCK_TX_EMPTY);

    /** 会话键值存储 */
    private Map<String, Object> data = new ConcurrentHashMap<String, Object>();

    public SessionImpl() {
        long now = System.currentTimeMillis();
        data.put(CREATE_TIME, now);
        this.setLastAccessTime(now);
    }

    @Override
    public Session put(String key, Object obj) {
        if (obj == null) {
            data.remove(key);
        } else {
            data.put(key, obj);
        }
        return this;
    }

    @Override
    public <T> T get(String key) {
        return (T) data.get(key);
    }

    @Override
    public <T> T remove(String key) {
        return (T) data.remove(key);
    }

    @Override
    /** CAS 获取锁：成功则写入新序号并返回 true */
    public boolean tryLock() {
        return lock.compareAndSet(LOCK_TX_EMPTY, lockSequence.getAndIncrement());
    }

    @Override
    /** 释放锁：仅当前持有者可将锁重置为 LOCK_TX_EMPTY */
    public void unLock() {
        int currentLockTx = lock.get();
        if (!lock.compareAndSet(currentLockTx, LOCK_TX_EMPTY)) {
            throw new IllegalStateException();
        }
    }

    @Override
    /** @return 锁字段是否非空（已锁定） */
    public boolean isLocked() {
        return lock.get() != LOCK_TX_EMPTY;
    }

    @Override
    /** @return 当前锁序号，未锁定时为 LOCK_TX_EMPTY */
    public int getLock() {
        return lock.get();
    }

    @Override
    public String getSessionId() {
        return (String) data.get(ID);
    }

    @Override
    public long getPid() {
        return (Long) data.get(PID);
    }

    @Override
    /** 从 COMMAND_MANAGER 取出 InternalCommandManager 并返回解析器列表 */
    public List<CommandResolver> getCommandResolvers() {
        InternalCommandManager commandManager = (InternalCommandManager) data.get(COMMAND_MANAGER);
        return commandManager.getResolvers();
    }

    @Override
    public Instrumentation getInstrumentation() {
        return (Instrumentation) data.get(INSTRUMENTATION);
    }

    @Override
    public void setLastAccessTime(long time) {
        this.put(LAST_ACCESS_TIME, time);
    }

    @Override
    public long getLastAccessTime() {
        return (Long)data.get(LAST_ACCESS_TIME);
    }

    @Override
    public long getCreateTime() {
        return (Long)data.get(CREATE_TIME);
    }

    @Override
    /** 设置或清除结果分发器（null 时 remove 键） */
    public void setResultDistributor(SharingResultDistributor resultDistributor) {
        if (resultDistributor == null) {
            data.remove(RESULT_DISTRIBUTOR);
        } else {
            data.put(RESULT_DISTRIBUTOR, resultDistributor);
        }
    }

    @Override
    public SharingResultDistributor getResultDistributor() {
        return (SharingResultDistributor) data.get(RESULT_DISTRIBUTOR);
    }

    @Override
    /** 记录或清除前台 Job 引用 */
    public void setForegroundJob(Job job) {
        if (job == null) {
            data.remove(FOREGROUND_JOB);
        } else {
            data.put(FOREGROUND_JOB, job);
        }
    }

    @Override
    public Job getForegroundJob() {
        return (Job) data.get(FOREGROUND_JOB);
    }

    @Override
    /** @return Session 是否关联 TTY（TTY 键非 null） */
    public boolean isTty() {
        return get(TTY) != null;
    }

    @Override
    public String getUserId() {
        return (String) data.get(USER_ID);
    }

    @Override
    /** 设置或清除用户 id */
    public void setUserId(String userId) {
        if (userId == null) {
            data.remove(USER_ID);
        } else {
            data.put(USER_ID, userId);
        }
    }

}
