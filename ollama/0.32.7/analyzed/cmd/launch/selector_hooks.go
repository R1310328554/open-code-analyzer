package launch

import (
	"errors"
	"fmt"
	"os"

	"golang.org/x/term"
)

// 终端 ANSI 转义序列，用于启动器 stderr 着色与强调。
const (
	ansiBold   = "\033[1m"
	ansiReset  = "\033[0m"
	ansiGray   = "\033[37m"
	ansiGreen  = "\033[32m"
	ansiYellow = "\033[33m"
)

// ErrCancelled 表示用户在确认或选择流程中取消操作。
var ErrCancelled = errors.New("cancelled")

// errCancelled 为既有调用点保留的内部别名。
var errCancelled = ErrCancelled

// DefaultConfirmPrompt 若设置，ConfirmPrompt 委托给 TUI 而非 raw 终端 I/O。
// When set, ConfirmPrompt delegates to it instead of using raw terminal I/O.
var DefaultConfirmPrompt func(prompt string, options ConfirmOptions) (bool, error)

type ConfirmDefault int

const (
	ConfirmDefaultYes ConfirmDefault = iota
	ConfirmDefaultNo
)

// ConfirmOptions 自定义确认对话框的是/否标签与默认选项。
type ConfirmOptions struct {
	YesLabel string
	NoLabel  string
	Default  ConfirmDefault
}

// SingleSelector 单选模型/集成项的回调类型；current 为预选高亮项。
// current is the name of the previously selected item to highlight; empty means no pre-selection.
type SingleSelector func(title string, items []SelectionItem, current string) (string, error)

// SingleSelectorWithUpdates 支持在列表打开时接收刷新后的 SelectionItem。
type SingleSelectorWithUpdates func(title string, items []SelectionItem, current string, updates <-chan []SelectionItem) (string, error)

// MultiSelector 多选模型列表的回调类型。
type MultiSelector func(title string, items []SelectionItem, preChecked []string) ([]string, error)

// MultiSelectorWithUpdates 多选器，支持打开期间接收账户状态驱动的列表更新。
type MultiSelectorWithUpdates func(title string, items []SelectionItem, preChecked []string, updates <-chan []SelectionItem) ([]string, error)

// DefaultSingleSelector 默认单选实现，由 cmd/tui 注册。
var DefaultSingleSelector SingleSelector

// DefaultSingleSelectorWithUpdates 带 live updates 的默认单选实现。
var DefaultSingleSelectorWithUpdates SingleSelectorWithUpdates

// DefaultMultiSelector 默认多选实现。
var DefaultMultiSelector MultiSelector

// DefaultMultiSelectorWithUpdates 带 live updates 的默认多选实现。
var DefaultMultiSelectorWithUpdates MultiSelectorWithUpdates

// DefaultSignIn 若设置，ensureAuth 使用 TUI 登录而非纯文本提示。
// When set, ensureAuth uses it instead of plain text prompts.
// Returns the signed-in username or an error.
var DefaultSignIn func(modelName, signInURL string) (string, error)

// DefaultUpgrade 若设置，云端套餐升级走 TUI 流程。
// Returns the updated plan or an error.
var DefaultUpgrade func(modelName, requiredPlan string) (string, error)

type launchConfirmPolicy struct {
	yes               bool
	requireYesMessage bool
}

var currentLaunchConfirmPolicy launchConfirmPolicy

func withLaunchConfirmPolicy(policy launchConfirmPolicy) func() {
	old := currentLaunchConfirmPolicy
	currentLaunchConfirmPolicy = policy
	return func() {
		currentLaunchConfirmPolicy = old
	}
}

// ConfirmPrompt 是启动流程的共享确认门控（安装、拉模型、登录等），受 currentLaunchConfirmPolicy 与 --yes 影响。
// edits, missing-model pulls, sign-in prompts, OpenClaw install/security, etc).
// Behavior is controlled by currentLaunchConfirmPolicy, typically scoped by
// withLaunchConfirmPolicy in LaunchCmd (e.g. auto-approve with --yes).
func ConfirmPrompt(prompt string) (bool, error) {
	return ConfirmPromptWithOptions(prompt, ConfirmOptions{})
}

// ConfirmPromptWithOptions 支持自定义是/否标签的确认门控。
// that need custom yes/no labels in interactive UIs.
func ConfirmPromptWithOptions(prompt string, options ConfirmOptions) (bool, error) {
	if currentLaunchConfirmPolicy.yes {
		return true, nil
	}
	if currentLaunchConfirmPolicy.requireYesMessage {
		return false, fmt.Errorf("%s requires confirmation; re-run with --yes to continue", prompt)
	}

	if DefaultConfirmPrompt != nil {
		return DefaultConfirmPrompt(prompt, options)
	}

	fd := int(os.Stdin.Fd())
	oldState, err := term.MakeRaw(fd)
	if err != nil {
		return false, err
	}
	defer term.Restore(fd, oldState)

	defaultNo := options.Default == ConfirmDefaultNo
	if defaultNo {
		fmt.Fprintf(os.Stderr, "%s (y/\033[1mN\033[0m) ", prompt)
	} else {
		fmt.Fprintf(os.Stderr, "%s (\033[1my\033[0m/n) ", prompt)
	}

	buf := make([]byte, 1)
	for {
		if _, err := os.Stdin.Read(buf); err != nil {
			return false, err
		}

		switch buf[0] {
		case 'Y', 'y':
			fmt.Fprintf(os.Stderr, "yes\r\n")
			return true, nil
		case 13:
			if defaultNo {
				fmt.Fprintf(os.Stderr, "no\r\n")
				return false, nil
			}
			fmt.Fprintf(os.Stderr, "yes\r\n")
			return true, nil
		case 'N', 'n', 27, 3:
			fmt.Fprintf(os.Stderr, "no\r\n")
			return false, nil
		}
	}
}
