// Copyright The Prometheus Authors
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

// treecache 在本地缓存 ZooKeeper 子树数据，并通过 watch 事件增量更新。

package treecache

import (
	"bytes"
	"errors"
	"fmt"
	"log/slog"
	"strings"
	"sync"
	"time"

	"github.com/go-zookeeper/zk"
	"github.com/prometheus/client_golang/prometheus"
)

var (
	failureCounter = prometheus.NewCounter(prometheus.CounterOpts{
		Namespace: "prometheus",
		Subsystem: "treecache",
		Name:      "zookeeper_failures_total",
		Help:      "The total number of ZooKeeper failures.",
	})
	numWatchers = prometheus.NewGauge(prometheus.GaugeOpts{
		Namespace: "prometheus",
		Subsystem: "treecache",
		Name:      "watcher_goroutines",
		Help:      "The current number of watcher goroutines.",
	})
)

func init() {
	prometheus.MustRegister(failureCounter)
	prometheus.MustRegister(numWatchers)
}

// ZookeeperLogger 将 slog.Logger 适配为 go-zookeeper 的 Logger 接口。
// ZookeeperLogger wraps a *slog.Logger into a zk.Logger.
type ZookeeperLogger struct {
	logger *slog.Logger
}

// NewZookeeperLogger is a constructor for ZookeeperLogger.
func NewZookeeperLogger(logger *slog.Logger) ZookeeperLogger {
	return ZookeeperLogger{logger: logger}
}

// Printf implements zk.Logger.
func (zl ZookeeperLogger) Printf(s string, i ...any) {
	zl.logger.Info(s, i...)
}

// ZookeeperTreeCache 递归 watch 指定 ZK 路径，将节点数据变更推送到 events channel。
// A ZookeeperTreeCache keeps data from all children of a Zookeeper path
// locally cached and updated according to received events.
type ZookeeperTreeCache struct {
	conn   *zk.Conn
	prefix string
	events chan ZookeeperTreeCacheEvent
	stop   chan struct{}
	wg     *sync.WaitGroup
	head   *zookeeperTreeCacheNode

	logger *slog.Logger
}

// ZookeeperTreeCacheEvent 表示某 ZK 路径的数据更新；Data 为 nil 表示节点已删除。
// A ZookeeperTreeCacheEvent models a Zookeeper event for a path.
type ZookeeperTreeCacheEvent struct {
	Path string
	Data *[]byte
}

type zookeeperTreeCacheNode struct {
	data     *[]byte
	events   chan zk.Event
	done     chan struct{}
	stopped  bool
	children map[string]*zookeeperTreeCacheNode
}

// NewZookeeperTreeCache 启动后台 loop 递归订阅 path 及其子节点。
// NewZookeeperTreeCache creates a new ZookeeperTreeCache for a given path.
func NewZookeeperTreeCache(conn *zk.Conn, path string, events chan ZookeeperTreeCacheEvent, logger *slog.Logger) *ZookeeperTreeCache {
	tc := &ZookeeperTreeCache{
		conn:   conn,
		prefix: path,
		events: events,
		stop:   make(chan struct{}),
		wg:     &sync.WaitGroup{},

		logger: logger,
	}
	tc.head = &zookeeperTreeCacheNode{
		events:   make(chan zk.Event),
		children: map[string]*zookeeperTreeCacheNode{},
		done:     make(chan struct{}, 1),
		stopped:  true, // Set head's stop to be true so that recursiveDelete will not stop the head node.
	}
	tc.wg.Add(1)
	go tc.loop(path)
	return tc
}

// Stop 通知 loop 退出，排空 head.events 后关闭对外 events channel。
// Stop stops the tree cache.
func (tc *ZookeeperTreeCache) Stop() {
	tc.stop <- struct{}{}
	go func() {
		// Drain tc.head.events so that go routines can make progress and exit.
		for range tc.head.events {
		}
	}()
	go func() {
		tc.wg.Wait()
		// Close the tc.head.events after all members of the wait group have exited.
		// This makes the go routine above exit.
		close(tc.head.events)
		close(tc.events)
	}()
}

// loop 处理初始同步、ZK 事件、失败重试与 Stop 信号的主循环。
func (tc *ZookeeperTreeCache) loop(path string) {
	defer tc.wg.Done()

	failureMode := false
	retryChan := make(chan struct{})

	failure := func() {
		failureCounter.Inc()
		failureMode = true
		time.AfterFunc(time.Second*10, func() {
			retryChan <- struct{}{}
		})
	}

	err := tc.recursiveNodeUpdate(path, tc.head)
	if err != nil {
		tc.logger.Error("Error during initial read of Zookeeper", "err", err)
		failure()
	}

	for {
		select {
		case ev := <-tc.head.events:
			tc.logger.Debug("Received Zookeeper event", "event", ev)
			if failureMode {
				continue
			}

			if ev.Type == zk.EventNotWatching {
				tc.logger.Info("Lost connection to Zookeeper.")
				failure()
			} else {
				path := strings.TrimPrefix(ev.Path, tc.prefix)
				parts := strings.Split(path, "/")
				node := tc.head
				for _, part := range parts[1:] {
					childNode := node.children[part]
					if childNode == nil {
						childNode = &zookeeperTreeCacheNode{
							events:   tc.head.events,
							children: map[string]*zookeeperTreeCacheNode{},
							done:     make(chan struct{}, 1),
						}
						node.children[part] = childNode
					}
					node = childNode
				}

				switch err := tc.recursiveNodeUpdate(ev.Path, node); {
				case err != nil:
					tc.logger.Error("Error during processing of Zookeeper event", "err", err)
					failure()
				case tc.head.data == nil:
					tc.logger.Error("Error during processing of Zookeeper event", "err", "path no longer exists", "path", tc.prefix)
					failure()
				}
			}
		case <-retryChan:
			tc.logger.Info("Attempting to resync state with Zookeeper")
			previousState := &zookeeperTreeCacheNode{
				children: tc.head.children,
			}
			// Reset root child nodes before traversing the Zookeeper path.
			tc.head.children = make(map[string]*zookeeperTreeCacheNode)

			if err := tc.recursiveNodeUpdate(tc.prefix, tc.head); err != nil {
				tc.logger.Error("Error during Zookeeper resync", "err", err)
				// Revert to our previous state.
				tc.head.children = previousState.children
				failure()
			} else {
				tc.resyncState(tc.prefix, tc.head, previousState)
				tc.logger.Info("Zookeeper resync successful")
				failureMode = false
			}
		case <-tc.stop:
			// Stop head as well.
			tc.head.done <- struct{}{}
			tc.recursiveStop(tc.head)
			return
		}
	}
}

// recursiveNodeUpdate 对单节点 GetW/ChildrenW，更新本地缓存并注册 data/child watcher。
func (tc *ZookeeperTreeCache) recursiveNodeUpdate(path string, node *zookeeperTreeCacheNode) error {
	data, _, dataWatcher, err := tc.conn.GetW(path)
	switch {
	case errors.Is(err, zk.ErrNoNode):
		tc.recursiveDelete(path, node)
		if node == tc.head {
			return fmt.Errorf("path %s does not exist", path)
		}
		return nil
	case err != nil:
		return err
	}

	if node.data == nil || !bytes.Equal(*node.data, data) {
		node.data = &data
		tc.events <- ZookeeperTreeCacheEvent{Path: path, Data: node.data}
	}

	children, _, childWatcher, err := tc.conn.ChildrenW(path)
	switch {
	case errors.Is(err, zk.ErrNoNode):
		tc.recursiveDelete(path, node)
		return nil
	case err != nil:
		return err
	}

	currentChildren := map[string]struct{}{}
	for _, child := range children {
		currentChildren[child] = struct{}{}
		childNode := node.children[child]
		// Does not already exists or we previous had a watch that
		// triggered.
		if childNode == nil || childNode.stopped {
			node.children[child] = &zookeeperTreeCacheNode{
				events:   node.events,
				children: map[string]*zookeeperTreeCacheNode{},
				done:     make(chan struct{}, 1),
			}
			err = tc.recursiveNodeUpdate(path+"/"+child, node.children[child])
			if err != nil {
				return err
			}
		}
	}

	// Remove nodes that no longer exist
	for name, childNode := range node.children {
		if _, ok := currentChildren[name]; !ok || node.data == nil {
			tc.recursiveDelete(path+"/"+name, childNode)
			delete(node.children, name)
		}
	}

	tc.wg.Go(func() {
		numWatchers.Inc()
		// Pass up zookeeper events, until the node is deleted.
		select {
		case event := <-dataWatcher:
			node.events <- event
		case event := <-childWatcher:
			node.events <- event
		case <-node.done:
		}
		numWatchers.Dec()
	})
	return nil
}

// resyncState 在 ZK 重连成功后删除新树中已不存在的旧子节点。
func (tc *ZookeeperTreeCache) resyncState(path string, currentState, previousState *zookeeperTreeCacheNode) {
	for child, previousNode := range previousState.children {
		if currentNode, present := currentState.children[child]; present {
			tc.resyncState(path+"/"+child, currentNode, previousNode)
		} else {
			tc.recursiveDelete(path+"/"+child, previousNode)
		}
	}
}

// recursiveDelete 停止 watcher 并向 events 发送 Data=nil 表示路径删除。
func (tc *ZookeeperTreeCache) recursiveDelete(path string, node *zookeeperTreeCacheNode) {
	if !node.stopped {
		node.done <- struct{}{}
		node.stopped = true
	}
	if node.data != nil {
		tc.events <- ZookeeperTreeCacheEvent{Path: path, Data: nil}
		node.data = nil
	}
	for name, childNode := range node.children {
		tc.recursiveDelete(path+"/"+name, childNode)
	}
}

func (tc *ZookeeperTreeCache) recursiveStop(node *zookeeperTreeCacheNode) {
	if !node.stopped {
		node.done <- struct{}{}
		node.stopped = true
	}
	for _, childNode := range node.children {
		tc.recursiveStop(childNode)
	}
}
