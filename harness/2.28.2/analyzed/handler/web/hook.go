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

// web 包提供 SCM Webhook 回调与 Web UI 相关的 HTTP 处理器。
package web

import (
	"context"
	"net/http"
	"net/http/httputil"
	"os"
	"strconv"
	"time"

	"github.com/sirupsen/logrus"

	"github.com/drone/drone/core"
	"github.com/drone/drone/logger"
	"github.com/drone/go-scm/scm"
)

// debugPrintHook 为本地调试开关，启用时将 Webhook 请求内容打印到 stdout。
var debugPrintHook = false

func init() {
	debugPrintHook, _ = strconv.ParseBool(
		os.Getenv("DRONE_DEBUG_DUMP_HOOK"),
	)
}

// HandleHook 返回 HTTP 处理器，接收并处理来自源代码管理系统的 Webhook 回调。
func HandleHook(
	repos core.RepositoryStore,
	builds core.BuildStore,
	triggerer core.Triggerer,
	parser core.HookParser,
) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {

		if debugPrintHook {
			// DRONE_DEBUG_DUMP_HOOK=true 时将完整 HTTP 请求头与正文输出到 stderr。
			out, _ := httputil.DumpRequest(r, true)
			os.Stderr.Write(out)
		}

		hook, remote, err := parser.Parse(r, func(slug string) string {
			namespace, name := scm.Split(slug)
			repo, err := repos.FindName(r.Context(), namespace, name)
			if err != nil {
				logrus.WithFields(
					logrus.Fields{
						"namespace": namespace,
						"name":      name,
					}).Debugln("cannot find repository")
				return ""
			}
			return repo.Signer
		})

		if err != nil {
			logrus.Debugf("cannot parse webhook: %s", err)
			writeBadRequest(w, err)
			return
		}

		if hook == nil {
			logrus.Debugf("webhook ignored")
			return
		}

		// TODO handle ping requests
		// TODO consider using scm.Repository in the function callback.

		log := logrus.WithFields(logrus.Fields{
			"namespace": remote.Namespace,
			"name":      remote.Name,
			"event":     hook.Event,
			"commit":    hook.After,
		})

		log.Debugln("webhook parsed")

		repo, err := repos.FindName(r.Context(), remote.Namespace, remote.Name)
		if err != nil {
			log = log.WithError(err)
			log.Debugln("cannot find repository")
			writeNotFound(w, err)
			return
		}

		if !repo.Active {
			log.Debugln("ignore webhook, repository inactive")
			w.WriteHeader(200)
			return
		}

		ctx, cancel := context.WithTimeout(context.Background(), time.Minute*5)
		ctx = logger.WithContext(ctx, log)
		defer cancel()

		// 分支删除事件：清理该分支关联的构建记录。
		if hook.Event == core.EventPush && hook.Action == core.ActionDelete {
			log.WithField("branch", hook.Target).Debugln("branch deleted")
			builds.DeleteBranch(ctx, repo.ID, hook.Target)
			w.WriteHeader(http.StatusNoContent)
			return
		}
		// Pull Request 关闭事件：清理该 PR 关联的构建记录。
		if hook.Event == core.EventPullRequest && hook.Action == core.ActionClose {
			log.WithField("ref", hook.Ref).Debugln("pull request closed")
			builds.DeletePull(ctx, repo.ID, scm.ExtractPullRequest(hook.Ref))
			w.WriteHeader(http.StatusNoContent)
			return
		}

		builds, err := triggerer.Trigger(ctx, repo, hook)
		if err != nil {
			writeError(w, err)
			return
		}

		writeJSON(w, builds, 200)
	}
}
