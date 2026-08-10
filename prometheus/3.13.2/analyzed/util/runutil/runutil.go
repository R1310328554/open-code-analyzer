// Copyright The Prometheus Authors
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

// 运行工具：按固定间隔重复执行回调，直至成功或收到 stopc 停止信号（改编自 efficientgo/core）。

// Copied from https://github.com/efficientgo/core/blob/a21078e2c723b69e05f95c65dbc5058712b4edd8/runutil/runutil.go#L39
// and adjusted.

package runutil

import "time"

// Retry 每隔 interval 调用 f，出错则等待下一 tick，stopc 关闭时返回最后一次错误。
// Retry executes f every interval seconds until timeout or no error is returned from f.
func Retry(interval time.Duration, stopc <-chan struct{}, f func() error) error {
	tick := time.NewTicker(interval)
	defer tick.Stop()

	var err error
	for {
		if err = f(); err == nil {
			return nil
		}
		select {
		case <-stopc:
			return err
		case <-tick.C:
		}
	}
}
