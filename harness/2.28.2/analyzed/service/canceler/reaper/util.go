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

package reaper

import "time"

// now 可替换的时间源，便于测试注入。
var now = time.Now

// isExceeded 判断自 unix 时刻起是否已超过 timeout+buffer 时长。
func isExceeded(unix int64, timeout, buffer time.Duration) bool {
	return now().After(
		time.Unix(unix, 0).Add(timeout).Add(buffer),
	)
}
