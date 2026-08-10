package commands

// commands.audit 注册 lokitool audit 子命令：校验给定 index 文件引用的 chunk 是否在对象存储中完整存在。

import (
	"context"
	"flag"
	"fmt"
	"os"

	"github.com/alecthomas/kingpin/v2"
	"github.com/go-kit/log"
	"github.com/go-kit/log/level"

	"github.com/grafana/loki/v3/pkg/tool/audit"
	util_cfg "github.com/grafana/loki/v3/pkg/util/cfg"
)

// AuditCommand 封装 index 路径、配置文件与额外 flag 参数，驱动 audit.Run 执行检查。
// AuditIndexCommand validates an index by checking existing chunks.
type AuditCommand struct {
	path string

	configFile string

	extraArgs []string
}

func (a *AuditCommand) auditIndex(_ *kingpin.ParseContext) error {
	logger := log.NewLogfmtLogger(os.Stdout)

	var auditCfg audit.Config
	if err := util_cfg.DefaultUnmarshal(&auditCfg, a.extraArgs, flag.CommandLine); err != nil {
		fmt.Fprintf(os.Stderr, "failed parsing config: %v\n", err)
		os.Exit(1)
	}
	if err := auditCfg.Validate(); err != nil {
		fmt.Fprintf(os.Stderr, "failed validating config: %v\n", err)
		os.Exit(1)
	}

	found, missing, err := audit.Run(context.Background(), a.path, auditCfg.Period, auditCfg, logger)
	if err != nil {
		return err
	}
	level.Info(logger).Log("msg", "finished auditing index", "chunks_found", found, "chunks_missing", missing)
	if missing > 0 {
		level.Error(logger).Log("msg", "your index is missing chunks, please check previous logs for more information", "index", a.path)
	} else {
		level.Info(logger).Log("msg", "your index is healthy", "index", a.path)
	}
	return nil
}

// Register 挂载 audit index 子命令及 config.file、index.file 等必填 flag。
func (a *AuditCommand) Register(app *kingpin.Application) {
	auditCmd := app.Command("audit", "Audit Loki state.")

	// Register audit commands.
	auditIndexCmd := auditCmd.
		Command("index", "Audit the given index by checking all its expected chunks are present.").
		Action(a.auditIndex)

	auditIndexCmd.Flag("config.file", "Auditing and storage configuration").Required().StringVar(&a.configFile)
	auditIndexCmd.Flag("index.file", "Index to be audited").Required().StringVar(&a.path)
	auditIndexCmd.Arg("args", "").StringsVar(&a.extraArgs)
}
// missing>0 时记录 error 日志提示索引缺块，否则 info 报告索引健康。
