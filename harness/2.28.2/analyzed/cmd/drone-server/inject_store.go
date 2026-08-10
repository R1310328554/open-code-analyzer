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

package main

import (
	"github.com/drone/drone/cmd/drone-server/config"
	"github.com/drone/drone/core"
	"github.com/drone/drone/metric"
	"github.com/drone/drone/store/batch"
	"github.com/drone/drone/store/batch2"
	"github.com/drone/drone/store/build"
	"github.com/drone/drone/store/card"
	"github.com/drone/drone/store/cron"
	"github.com/drone/drone/store/logs"
	"github.com/drone/drone/store/perm"
	"github.com/drone/drone/store/repos"
	"github.com/drone/drone/store/secret"
	"github.com/drone/drone/store/secret/global"
	"github.com/drone/drone/store/shared/db"
	"github.com/drone/drone/store/shared/encrypt"
	"github.com/drone/drone/store/stage"
	"github.com/drone/drone/store/step"
	"github.com/drone/drone/store/template"
	"github.com/drone/drone/store/user"

	"github.com/google/wire"
	"github.com/sirupsen/logrus"
)

// wire set for loading the stores.
// storeSet 定义数据库连接与各持久化存储的 Wire 提供者集合。
var storeSet = wire.NewSet(
	provideDatabase,
	provideEncrypter,
	provideBuildStore,
	provideLogStore,
	provideRepoStore,
	provideStageStore,
	provideUserStore,
	provideBatchStore,
	// batch.New,
	cron.New,
	card.New,
	perm.New,
	secret.New,
	global.New,
	step.New,
	template.New,
)

// provideDatabase 根据环境配置建立并返回数据库连接。
func provideDatabase(config config.Config) (*db.DB, error) {
	return db.Connect(
		config.Database.Driver,
		config.Database.Datasource,
		config.Database.MaxConnections,
	)
}

// provideEncrypter 根据环境配置返回数据库字段加密器。
func provideEncrypter(config config.Config) (encrypt.Encrypter, error) {
	enc, err := encrypt.New(config.Database.Secret)
	// 混合内容模式：数据库原先未加密时存在明文条目，解密失败时返回原文
	if aesgcm, ok := enc.(*encrypt.Aesgcm); ok {
		logrus.Debugln("main: database encryption enabled")
		if config.Database.EncryptMixedContent {
			logrus.Debugln("main: database encryption mixed-mode enabled")
			aesgcm.Compat = true
		}
	}
	return enc, err
}

// provideBuildStore 返回启用 Prometheus 指标的构建数据存储。
func provideBuildStore(db *db.DB) core.BuildStore {
	builds := build.New(db)
	metric.BuildCount(builds)
	metric.PendingBuildCount(builds)
	metric.RunningBuildCount(builds)
	return builds
}

// provideLogStore 根据环境配置返回构建日志存储（支持 S3/Azure/本地）。
func provideLogStore(db *db.DB, config config.Config) core.LogStore {
	s := logs.New(db)
	if config.S3.Bucket != "" {
		p := logs.NewS3Env(
			config.S3.Bucket,
			config.S3.Prefix,
			config.S3.Endpoint,
			config.S3.PathStyle,
		)
		return logs.NewCombined(p, s)
	}
	if config.AzureBlob.ContainerName != "" {
		p := logs.NewAzureBlobEnv(
			config.AzureBlob.ContainerName,
			config.AzureBlob.StorageAccountName,
			config.AzureBlob.StorageAccessKey,
		)
		return logs.NewCombined(p, s)
	}
	return s
}

// provideStageStore 返回启用 Prometheus 指标的阶段（Job）数据存储。
func provideStageStore(db *db.DB) core.StageStore {
	stages := stage.New(db)
	metric.PendingJobCount(stages)
	metric.RunningJobCount(stages)
	return stages
}

// provideRepoStore 返回启用 Prometheus 指标的仓库数据存储。
func provideRepoStore(db *db.DB) core.RepositoryStore {
	repos := repos.New(db)
	metric.RepoCount(repos)
	return repos
}

// provideBatchStore 返回批量操作器；LegacyBatch 启用时使用旧版实现。
func provideBatchStore(db *db.DB, config config.Config) core.Batcher {
	if config.Database.LegacyBatch {
		return batch.New(db)
	}
	return batch2.New(db)
}

// provideUserStore 返回用户数据存储；EncryptUserTable 启用时对用户表加密。
func provideUserStore(db *db.DB, enc encrypt.Encrypter, config config.Config) core.UserStore {
	// 仅在启用用户表加密特性标志时创建加密存储。
	// 用户表在每次 HTTP 请求中都会被访问，加密可能带来性能影响，
	// 因此默认关闭；如有性能问题可考虑 LRU 内存缓存。
	if config.Database.EncryptUserTable {
		logrus.Debugln("main: database encryption enabled for user table")
		users := user.New(db, enc)
		metric.UserCount(users)
		return users
	}

	noenc, _ := encrypt.New("")
	users := user.New(db, noenc)
	metric.UserCount(users)
	return users
}
