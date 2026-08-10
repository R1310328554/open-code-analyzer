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
	"sync"

	"github.com/drone/drone/core"
)

// subscriber 表示单个日志流消费者，通过缓冲 channel 接收 core.Line。
type subscriber struct {
	sync.Mutex

	handler chan *core.Line
	closec  chan struct{}
	closed  bool
}

// publish 非阻塞地向订阅者发送一行日志；缓冲满时丢弃新消息以保护生产者。
func (s *subscriber) publish(line *core.Line) {
	select {
	case <-s.closec:
	case s.handler <- line:
	default:
		// lines are sent on a buffered channel. If there
		// is a slow consumer that is not processing events,
		// the buffered channel will fill and newer messages
		// are ignored.
	}
}

// close 关闭订阅者，仅执行一次 closec 信号。
func (s *subscriber) close() {
	s.Lock()
	if !s.closed {
		close(s.closec)
		s.closed = true
	}
	s.Unlock()
}
