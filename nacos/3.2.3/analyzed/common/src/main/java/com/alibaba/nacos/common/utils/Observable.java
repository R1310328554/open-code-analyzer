/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.common.utils;

import java.util.Objects;
import java.util.Set;

/**
 * 观察者模式可观察对象：基于 {@link ConcurrentHashSet} 维护 {@link Observer} 集合，
 * 支持 changed 标志位与 notify/delete 等操作，语义类似 {@code java.util.Observable}。
 * Observable utils.
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public class Observable {
    
    /** 状态变更标志，{@link #setChanged()} 置 true，通知后由 {@link #clearChanged()} 清除 */
    private transient boolean changed = false;
    
    /** 已注册观察者集合，线程安全的 ConcurrentHashSet 实现 */
    private transient Set<Observer> obs = new ConcurrentHashSet<>();
    
    /** 观察者计数，add/delete 时维护，供 {@link #countObservers()} 读取 */
    private volatile int observerCnt = 0;
    
    /**
     * Adds an observer to the set of observers for this object, provided that it is not the same as some observer
     * already in the set. The order in which notifications will be delivered to multiple observers is not specified.
     * See the class comment.
     *
     * @param o an observer to be added.
     * @throws NullPointerException if the parameter o is null.
      * <p>观察者模式可观察对象；详见类级说明。</p>
     */
    public synchronized void addObserver(Observer o) {
        Objects.requireNonNull(o, "Observer");
        obs.add(o);
        observerCnt++;
        o.update(this);
    }
    
    /**
     * Deletes an observer from the set of observers of this object. Passing {@code null} to this method will have no
     * effect.
     *
     * @param o the observer to be deleted.
      * <p>观察者模式可观察对象；详见类级说明。</p>
     */
    public synchronized void deleteObserver(Observer o) {
        obs.remove(o);
        observerCnt--;
    }
    
    /**
     * If this object has changed, as indicated by the {@code hasChanged} method, then notify all of its observers and
     * then call the {@code clearChanged} method to indicate that this object has no longer changed.
     *
     * <p>Each observer has its {@code update} method called with one argument: this observable object.
      * <p>观察者模式可观察对象；详见类级说明。</p>
     */
    public void notifyObservers() {
        synchronized (this) {
            /* We don't want the Observer doing callbacks into
             * arbitrary code while holding its own Monitor.
             * The code where we extract each Observable from
             * the Vector and store the state of the Observer
             * needs synchronization, but notifying observers
             * does not (should not).  The worst result of any
             * potential race-condition here is that:
             * 1) a newly-added Observer will miss a
             *   notification in progress
             * 2) a recently unregistered Observer will be
             *   wrongly notified when it doesn't care
             */
            if (!changed) {
                return;
            }
            clearChanged();
        }
        for (Observer observer : obs) {
            observer.update(this);
        }
    }
    
    /**
     * Clears the observer list so that this object no longer has any observers.
      * <p>观察者模式可观察对象；详见类级说明。</p>
     */
    public void deleteObservers() {
        obs.clear();
    }
    
    /**
     * Marks this {@code Observable} object as having been changed; the {@code hasChanged} method will now return {@code
     * true}.
      * <p>观察者模式可观察对象；详见类级说明。</p>
     */
    protected synchronized void setChanged() {
        changed = true;
    }
    
    /**
     * Indicates that this object has no longer changed, or that it has already notified all of its observers of its
     * most recent change, so that the {@code hasChanged} method will now return {@code false}. This method is called
     * automatically by the {@code notifyObservers} methods.
     *
     * @see java.util.Observable#notifyObservers()
     * @see java.util.Observable#notifyObservers(java.lang.Object)
      * <p>观察者模式可观察对象；详见类级说明。</p>
     */
    protected synchronized void clearChanged() {
        changed = false;
    }
    
    /**
     * Tests if this object has changed.
     *
     * @return {@code true} if and only if the {@code setChanged} method has been called more recently than the {@code
     * clearChanged} method on this object; {@code false} otherwise.
      * <p>观察者模式可观察对象；详见类级说明。</p>
     */
    public synchronized boolean hasChanged() {
        return changed;
    }
    
    /**
     * Returns the number of observers of this {@code Observable} object.
     *
     * @return the number of observers of this object.
      * <p>观察者模式可观察对象；详见类级说明。</p>
     */
    public int countObservers() {
        return observerCnt;
    }
}
