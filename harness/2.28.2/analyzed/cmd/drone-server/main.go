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

// main 包是 Drone 服务器的入口，负责加载配置、依赖注入并启动 HTTP 服务及后台任务。
package main

import (
	"context"
	"flag"
	"fmt"

	"github.com/drone/drone/cmd/drone-server/bootstrap"
	"github.com/drone/drone/cmd/drone-server/config"
	"github.com/drone/drone/core"
	"github.com/drone/drone/metric/sink"
	"github.com/drone/drone/operator/runner"
	"github.com/drone/drone/service/canceler/reaper"
	"github.com/drone/drone/server"
	"github.com/drone/drone/trigger/cron"
	"github.com/drone/signal"

	"github.com/joho/godotenv"
	"github.com/sirupsen/logrus"
	"golang.org/x/sync/errgroup"

	_ "github.com/go-sql-driver/mysql"
	_ "github.com/lib/pq"
	_ "github.com/mattn/go-sqlite3"
)

func main() {
	var envfile string
	// 从 .env 文件读取环境变量覆盖默认配置
	flag.StringVar(&envfile, "env-file", ".env", "Read in a file of environment variables")
	flag.Parse()

	godotenv.Load(envfile)
	config, err := config.Environ()
	if err != nil {
		logger := logrus.WithError(err)
		logger.Fatalln("main: invalid configuration")
	}

	initLogging(config)
	ctx := signal.WithContext(
		context.Background(),
	)

	// 若启用 trace 级别日志，则输出完整配置参数
	if logrus.IsLevelEnabled(logrus.TraceLevel) {
		fmt.Println(config.String())
	}

	app, err := InitializeApplication(config)
	if err != nil {
		logger := logrus.WithError(err)
		logger.Fatalln("main: cannot initialize server")
	}

	// 可选：根据环境变量中配置的管理员或机器用户引导系统初始化
	err = bootstrap.New(app.users).Bootstrap(ctx, &core.User{
		Login:   config.Users.Create.Username,
		Machine: config.Users.Create.Machine,
		Admin:   config.Users.Create.Admin,
		Hash:    config.Users.Create.Token,
	})
	if err != nil {
		logger := logrus.WithError(err)
		logger.Fatalln("cannot bootstrap user account")
	}

	g := errgroup.Group{}
	// 启动 HTTP 服务器
	g.Go(func() error {
		logrus.WithFields(
			logrus.Fields{
				"proto": config.Server.Proto,
				"host":  config.Server.Host,
				"port":  config.Server.Port,
				"url":   config.Server.Addr,
				"acme":  config.Server.Acme,
			},
		).Infoln("starting the http server")
		return app.server.ListenAndServe(ctx)
	})

	// 在 goroutine 中启动 Datadog 指标上报；若未启用则立即退出
	g.Go(func() (err error) {
		if !config.Datadog.Enabled {
			return nil
		}
		return app.sink.Start(ctx)
	})

	// 在 goroutine 中启动定时任务调度器；若禁用则立即退出
	g.Go(func() (err error) {
		if config.Cron.Disabled {
			return nil
		}
		logrus.WithField("interval", config.Cron.Interval.String()).
			Infoln("starting the cron scheduler")
		return app.cron.Start(ctx, config.Cron.Interval)
	})

	// 在 goroutine 中启动僵尸构建清理器；若禁用则立即退出
	g.Go(func() (err error) {
		if config.Cleanup.Disabled {
			return nil
		}
		logrus.WithField("interval", config.Cleanup.Interval.String()).
			Infoln("starting the zombie build reaper")
		return app.reaper.Start(ctx, config.Cleanup.Interval)
	})

	// 在 goroutine 中启动本地构建运行器；若远程 agent 已启用则跳过
	g.Go(func() (err error) {
		if app.runner == nil {
			return nil
		}
		logrus.WithField("threads", config.Runner.Capacity).
			Infoln("main: starting the local build runner")
		return app.runner.Start(ctx, config.Runner.Capacity)
	})

	if err := g.Wait(); err != nil {
		logrus.WithError(err).Fatalln("program terminated")
	}
}

// initLogging 根据配置初始化日志级别与输出格式。
func initLogging(c config.Config) {
	if c.Logging.Debug {
		logrus.SetLevel(logrus.DebugLevel)
	}
	if c.Logging.Trace {
		logrus.SetLevel(logrus.TraceLevel)
	}
	if c.Logging.Text {
		logrus.SetFormatter(&logrus.TextFormatter{
			ForceColors:   c.Logging.Color,
			DisableColors: !c.Logging.Color,
		})
	} else {
		logrus.SetFormatter(&logrus.JSONFormatter{
			PrettyPrint: c.Logging.Pretty,
		})
	}
}

// application 聚合 Drone 服务器运行所需的全部核心组件。
type application struct {
	cron   *cron.Scheduler
	reaper *reaper.Reaper
	sink   *sink.Datadog
	runner *runner.Runner
	server *server.Server
	users  core.UserStore
}

// newApplication 组装并返回 application 结构体实例。
func newApplication(
	cron *cron.Scheduler,
	reaper *reaper.Reaper,
	sink *sink.Datadog,
	runner *runner.Runner,
	server *server.Server,
	users core.UserStore) application {
	return application{
		users:  users,
		cron:   cron,
		sink:   sink,
		server: server,
		runner: runner,
		reaper: reaper,
	}
}
