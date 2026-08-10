package launch

import (
	"fmt"
	"os"
	"os/exec"
	"path/filepath"
	"runtime"
	"strconv"
	"strings"

	"github.com/ollama/ollama/envconfig"
)

// Claude 实现 Runner，将 Claude Code CLI 路由到本地 Ollama 并提供安装引导。
// Claude 集成 Claude Code 命令行工具。
type Claude struct{}

func (c *Claude) String() string { return "Claude Code" }

func (c *Claude) args(model string, extra []string) []string {
	var args []string
	if model != "" {
		args = append(args, "--model", model)
	}
	args = append(args, extra...)
	return args
}

// findPath 在 PATH 与常见安装目录中定位 claude 可执行文件。
func (c *Claude) findPath() (string, error) {
	if p, err := exec.LookPath("claude"); err == nil {
		return p, nil
	}
	home, err := os.UserHomeDir()
	if err != nil {
		return "", err
	}
	name := "claude"
	if runtime.GOOS == "windows" {
		name = "claude.exe"
	}
	for _, fallback := range []string{
		filepath.Join(home, ".local", "bin", name),
		filepath.Join(home, ".claude", "local", name),
	} {
		if _, err := os.Stat(fallback); err == nil {
			return fallback, nil
		}
	}
	return "", fmt.Errorf("claude binary not found")
}

// Run 确保 Claude 已安装后以 Ollama 后端环境变量启动子进程。
func (c *Claude) Run(model string, _ []LaunchModel, args []string) error {
	claudePath, err := ensureClaudeInstalled()
	if err != nil {
		return err
	}

	cmd := exec.Command(claudePath, c.args(model, args)...)
	cmd.Stdin = os.Stdin
	cmd.Stdout = os.Stdout
	cmd.Stderr = os.Stderr

	cmd.Env = append(os.Environ(), c.envVars(model)...)
	return cmd.Run()
}

// envVars 设置 ANTHROPIC_BASE_URL 等变量，使 Claude Code 走 Ollama 兼容 API。
func (c *Claude) envVars(model string) []string {
	env := []string{
		"ANTHROPIC_BASE_URL=" + envconfig.Host().String(),
		"ANTHROPIC_API_KEY=",
		"ANTHROPIC_AUTH_TOKEN=ollama",
		"CLAUDE_CODE_ATTRIBUTION_HEADER=0",
		"DISABLE_ERROR_REPORTING=1",
		"DISABLE_FEEDBACK_COMMAND=1",
		"CLAUDE_CODE_DISABLE_FEEDBACK_SURVEY=1",
	}

	env = append(env, c.modelEnvVars(model)...)
	return env
}

// ensureClaudeInstalled 若未安装则交互式运行官方安装脚本。
func ensureClaudeInstalled() (string, error) {
	if path, err := (&Claude{}).findPath(); err == nil {
		return path, nil
	}

	if err := checkClaudeInstallerDependencies(); err != nil {
		return "", err
	}

	ok, err := ConfirmPrompt("Claude Code is not installed. Install now?")
	if err != nil {
		return "", err
	}
	if !ok {
		return "", fmt.Errorf("claude installation cancelled")
	}

	bin, args, err := claudeInstallerCommand(runtime.GOOS)
	if err != nil {
		return "", err
	}

	fmt.Fprintf(os.Stderr, "\nInstalling Claude Code...\n")
	cmd := exec.Command(bin, args...)
	cmd.Stdin = os.Stdin
	cmd.Stdout = os.Stdout
	cmd.Stderr = os.Stderr
	if err := cmd.Run(); err != nil {
		return "", fmt.Errorf("failed to install claude: %w", err)
	}

	path, err := (&Claude{}).findPath()
	if err != nil {
		return "", fmt.Errorf("claude was installed but the binary was not found on PATH\n\nYou may need to restart your shell")
	}

	fmt.Fprintf(os.Stderr, "%sClaude Code installed successfully%s\n\n", ansiGreen, ansiReset)
	return path, nil
}

// checkClaudeInstallerDependencies 检查 curl/bash 或 PowerShell 等安装依赖。
func checkClaudeInstallerDependencies() error {
	switch runtime.GOOS {
	case "windows":
		if _, err := exec.LookPath("powershell"); err != nil {
			return fmt.Errorf("claude is not installed and required dependencies are missing\n\nInstall the following first:\n  PowerShell: https://learn.microsoft.com/powershell/\n\nThen re-run:\n  ollama launch claude")
		}
	default:
		var missing []string
		if _, err := exec.LookPath("curl"); err != nil {
			missing = append(missing, "curl: https://curl.se/")
		}
		if _, err := exec.LookPath("bash"); err != nil {
			missing = append(missing, "bash: https://www.gnu.org/software/bash/")
		}
		if len(missing) > 0 {
			return fmt.Errorf("claude is not installed and required dependencies are missing\n\nInstall the following first:\n  %s\n\nThen re-run:\n  ollama launch claude", strings.Join(missing, "\n  "))
		}
	}
	return nil
}

// claudeInstallerCommand 返回各平台安装 Claude Code 的命令与参数。
func claudeInstallerCommand(goos string) (string, []string, error) {
	switch goos {
	case "windows":
		return "powershell", []string{
			"-NoProfile",
			"-ExecutionPolicy",
			"Bypass",
			"-Command",
			"irm https://claude.ai/install.ps1 | iex",
		}, nil
	case "darwin", "linux":
		return "bash", []string{
			"-c",
			"curl -fsSL https://claude.ai/install.sh | bash",
		}, nil
	default:
		return "", nil, fmt.Errorf("unsupported platform for claude install: %s", goos)
	}
}

// modelEnvVars 将所有 Claude 模型档位环境变量指向同一 Ollama 模型名。
func (c *Claude) modelEnvVars(model string) []string {
	env := []string{
		"ANTHROPIC_DEFAULT_OPUS_MODEL=" + model,
		"ANTHROPIC_DEFAULT_SONNET_MODEL=" + model,
		"ANTHROPIC_DEFAULT_HAIKU_MODEL=" + model,
		"CLAUDE_CODE_SUBAGENT_MODEL=" + model,
	}

	if isCloudModelName(model) {
		if l, ok := lookupCloudModelLimit(model); ok {
			env = append(env, "CLAUDE_CODE_AUTO_COMPACT_WINDOW="+strconv.Itoa(l.Context))
		}
	}

	return env
}
