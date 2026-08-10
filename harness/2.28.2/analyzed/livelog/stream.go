// Copyright 2019 Drone IO, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package livelog

import (
	"context"
	"sync"

	"github.com/drone/drone/core"
)

// bufferSize 为每个流/订阅者在内存中保留的日志行数上限，
// 约每流每订阅者占用 10KB 缓冲（不含日志正文）。
const bufferSize = 5000

// stream 表示单条构建步骤的内存日志流，维护历史缓冲与活跃订阅者集合。
type stream struct {
	sync.Mutex

	hist []*core.Line
	list map[*subscriber]struct{}
}

// newStream 构造空的内存日志流。
func newStream() *stream {
	return &stream{
		list: map[*subscriber]struct{}{},
	}
}

// write 追加一行日志并广播给所有订阅者；历史按 FIFO 截断至 bufferSize。
func (s *stream) write(line *core.Line) error {
	s.Lock()
	s.hist = append(s.hist, line)
	for l := range s.list {
		l.publish(line)
	}
	// 限制历史长度，超出容量时丢弃最旧条目。
	if size := len(s.hist); size >= bufferSize {
		s.hist = s.hist[size-bufferSize:]
	}
	s.Unlock()
	return nil
}

// subscribe 注册新订阅者，先回放历史再返回日志行与错误通道。
func (s *stream) subscribe(ctx context.Context) (<-chan *core.Line, <-chan error) {
	sub := &subscriber{
		handler: make(chan *core.Line, bufferSize),
		closec:  make(chan struct{}),
	}
	err := make(chan error)

	s.Lock()
	for _, line := range s.hist {
		sub.publish(line)
	}
	s.list[sub] = struct{}{}
	s.Unlock()

	go func() {
		defer close(err)
		select {
		case <-sub.closec:
		case <-ctx.Done():
			sub.close()
		}
	}()
	return sub.handler, err
}

// close 关闭流并通知所有订阅者退出。
func (s *stream) close() error {
	s.Lock()
	defer s.Unlock()
	for sub := range s.list {
		delete(s.list, sub)
		sub.close()
	}
	return nil
}
