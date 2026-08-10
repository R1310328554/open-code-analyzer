// Copyright The Prometheus Authors
// Based on golang.org/x/net/netutil:
//   Copyright 2013 The Go Authors
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

// netconnlimit 提供跨多个 Listener 共享连接数上限的网络工具，源自 golang.org/x/net/netutil。
// Package netconnlimit provides network utility functions for limiting
// simultaneous connections across multiple listeners.
package netconnlimit

import (
	"net"
	"sync"
)

// NewSharedSemaphore 创建容量为 n 的共享信号量 channel，供多个限流 Listener 复用。
// NewSharedSemaphore creates and returns a new semaphore channel that can be used
// to limit the number of simultaneous connections across multiple listeners.
func NewSharedSemaphore(n int) chan struct{} {
	return make(chan struct{}, n)
}

// SharedLimitListener 包装 net.Listener，通过共享 sem 限制全局并发连接数。
// SharedLimitListener returns a listener that accepts at most n simultaneous
// connections across multiple listeners using the provided shared semaphore.
func SharedLimitListener(l net.Listener, sem chan struct{}) net.Listener {
	return &sharedLimitListener{
		Listener: l,
		sem:      sem,
		done:     make(chan struct{}),
	}
}

// sharedLimitListener 在 Accept 时占用 sem 槽位，Close 后通过 done 通知 acquire 失败。
type sharedLimitListener struct {
	net.Listener
	sem       chan struct{}
	closeOnce sync.Once     // Ensures the done chan is only closed once.
	done      chan struct{} // No values sent; closed when Close is called.
}

// acquire 非阻塞尝试写入 sem；若 Listener 已关闭则返回 false。
// Acquire acquires the shared semaphore. Returns true if successfully
// acquired, false if the listener is closed and the semaphore is not
// acquired.
func (l *sharedLimitListener) acquire() bool {
	select {
	case <-l.done:
		return false
	case l.sem <- struct{}{}:
		return true
	}
}

// release 从 sem 取出一个令牌，在 Accept 失败或连接关闭时归还配额。
func (l *sharedLimitListener) release() { <-l.sem }

func (l *sharedLimitListener) Accept() (net.Conn, error) {
	if !l.acquire() {
		for {
			c, err := l.Listener.Accept()
			if err != nil {
				return nil, err
			}
			c.Close()
		}
	}

	c, err := l.Listener.Accept()
	if err != nil {
		l.release()
		return nil, err
	}
	return &sharedLimitListenerConn{Conn: c, release: l.release}, nil
}

func (l *sharedLimitListener) Close() error {
	err := l.Listener.Close()
	l.closeOnce.Do(func() { close(l.done) })
	return err
}

// sharedLimitListenerConn 包装 net.Conn，Close 时通过 sync.Once 仅释放一次 sem。
type sharedLimitListenerConn struct {
	net.Conn
	releaseOnce sync.Once
	release     func()
}

func (l *sharedLimitListenerConn) Close() error {
	err := l.Conn.Close()
	l.releaseOnce.Do(l.release)
	return err
}
