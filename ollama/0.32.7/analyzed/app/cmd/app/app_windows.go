//go:build windows || darwin

// app_windows.go 提供 Windows 托盘、窗口焦点、登录快捷方式与单实例通信。
package main

import (
	"errors"
	"fmt"
	"io"
	"log"
	"log/slog"
	"os"
	"os/exec"
	"os/signal"
	"path/filepath"
	"runtime"
	"strings"
	"syscall"
	"unsafe"

	"github.com/ollama/ollama/app/updater"
	"github.com/ollama/ollama/app/version"
	"github.com/ollama/ollama/app/wintray"
	"golang.org/x/sys/windows"
)

var (
	u32                  = windows.NewLazySystemDLL("User32.dll")
	pBringWindowToTop    = u32.NewProc("BringWindowToTop")
	pShowWindow          = u32.NewProc("ShowWindow")
	pSendMessage         = u32.NewProc("SendMessageA")
	pGetSystemMetrics    = u32.NewProc("GetSystemMetrics")
	pGetWindowRect       = u32.NewProc("GetWindowRect")
	pSetWindowPos        = u32.NewProc("SetWindowPos")
	pSetForegroundWindow = u32.NewProc("SetForegroundWindow")
	pSetActiveWindow     = u32.NewProc("SetActiveWindow")
	pIsIconic            = u32.NewProc("IsIconic")

	appPath         = filepath.Join(os.Getenv("LOCALAPPDATA"), "Programs", "Ollama")
	appLogPath      = filepath.Join(os.Getenv("LOCALAPPDATA"), "Ollama", "app.log")
	startupShortcut = filepath.Join(os.Getenv("APPDATA"), "Microsoft", "Windows", "Start Menu", "Programs", "Startup", "Ollama.lnk")
	ollamaPath      string
	DesktopAppName  = "ollama app.exe"
)

func init() {
	// With alternate install location use executable location
	exe, err := os.Executable()
	if err != nil {
		slog.Warn("error discovering executable directory", "error", err)
	} else {
		appPath = filepath.Dir(exe)
	}
	ollamaPath = filepath.Join(appPath, "ollama.exe")

	// Handle developer mode (go run ./cmd/app)
	if _, err := os.Stat(ollamaPath); err != nil {
		pwd, err := os.Getwd()
		if err != nil {
			slog.Warn("missing ollama.exe and failed to get pwd", "error", err)
			return
		}
		distAppPath := filepath.Join(pwd, "dist", "windows-"+runtime.GOARCH)
		distOllamaPath := filepath.Join(distAppPath, "ollama.exe")
		if _, err := os.Stat(distOllamaPath); err == nil {
			slog.Info("detected developer mode")
			appPath = distAppPath
			ollamaPath = distOllamaPath
		}
	}
}

// maybeMoveAndRestart Windows 安装路径固定，无需迁移。
func maybeMoveAndRestart() appMove {
	return 0
}

// handleExistingInstance 检测已有实例并聚焦或退出当前进程。
// handleExistingInstance checks for existing instances and optionally focuses them
func handleExistingInstance(startHidden bool) {
	if wintray.CheckAndFocusExistingInstance(!startHidden) {
		slog.Info("existing instance found, exiting")
		os.Exit(0)
	}
}

// installSymlink Windows 无 CLI 符号链接安装逻辑。
func installSymlink() {}

// appCallbacks 实现 wintray.TrayCallbacks，桥接托盘与 WebView。
type appCallbacks struct {
	t        wintray.TrayCallbacks
	shutdown func()
}

var app = &appCallbacks{}

// UIRun 启动 WebView 并加载指定路径。
func (ac *appCallbacks) UIRun(path string) {
	wv.Run(path)
}

// UIShow 显示 WebView 窗口。
func (*appCallbacks) UIShow() {
	if wv.webview != nil {
		showWindow(wv.webview.Window())
	} else {
		wv.Run("/")
	}
}

// UITerminate 关闭 WebView。
func (*appCallbacks) UITerminate() {
	wv.Terminate()
}

// UIRunning 报告 WebView 是否仍在运行。
func (*appCallbacks) UIRunning() bool {
	return wv.IsRunning()
}

// Quit 退出托盘并终止 WebView。
func (app *appCallbacks) Quit() {
	app.t.Quit()
	wv.Terminate()
}

// TODO - reconcile with above for consistency between mac/windows
// quit 终止 WebView（Windows 简化版）。
func quit() {
	wv.Terminate()
}

// DoUpdate 关闭服务后执行就地升级。
func (app *appCallbacks) DoUpdate() {
	// Safeguard in case we have requests in flight that need to drain...
	slog.Info("Waiting for server to shutdown")

	app.shutdown()

	if err := updater.DoUpgrade(true); err != nil {
		slog.Warn(fmt.Sprintf("upgrade attempt failed: %s", err))
	}
}

// HandleURLScheme 处理来自其他实例转发的 ollama:// URL。
// HandleURLScheme implements the URLSchemeHandler interface
func (app *appCallbacks) HandleURLScheme(urlScheme string) {
	handleURLSchemeRequest(urlScheme)
}

// handleURLSchemeRequest 解析 URL 并打开 connect 或主窗口。
// handleURLSchemeRequest processes URL scheme requests from other instances
func handleURLSchemeRequest(urlScheme string) {
	isConnect, err := parseURLScheme(urlScheme)
	if err != nil {
		slog.Error("failed to parse URL scheme request", "url", urlScheme, "error", err)
		return
	}

	if isConnect {
		handleConnectURLScheme()
	} else {
		if wv.webview != nil {
			showWindow(wv.webview.Window())
		}
	}
}

// UpdateAvailable 通过系统托盘通知有可用更新。
func UpdateAvailable(ver string) error {
	if app.t == nil {
		slog.Debug("tray not yet initialized, skipping update notification")
		return nil
	}
	return app.t.UpdateAvailable(ver)
}

// osRun 初始化托盘、处理隐藏启动逻辑并阻塞于托盘事件循环。
func osRun(shutdown func(), hasCompletedFirstRun, startHidden bool) {
	var err error
	app.shutdown = shutdown
	app.t, err = wintray.NewTray(app)
	if err != nil {
		log.Fatalf("Failed to start: %s", err)
	}

	// Check for pending updates now that the tray is initialized.
	// The platform-independent check in app.go fires before osRun,
	// when app.t is still nil, so we must re-check here.
	if updater.IsUpdatePending() {
		slog.Debug("update pending on startup, showing tray notification")
		UpdateAvailable("")
	}

	signals := make(chan os.Signal, 1)
	signal.Notify(signals, syscall.SIGINT, syscall.SIGTERM)

	// TODO - can this be generalized?
	go func() {
		<-signals
		slog.Debug("shutting down due to signal")
		app.t.Quit()
		wv.Terminate()
	}()

	// On windows, we run the final tasks in the main thread
	// before starting the tray event loop.  These final tasks
	// may trigger the UI, and must do that from the main thread.
	if !startHidden {
		// Determine if the process was started from a shortcut
		// ~\AppData\Roaming\Microsoft\Windows\Start Menu\Programs\Startup\Ollama
		const STARTF_TITLEISLINKNAME = 0x00000800
		var info windows.StartupInfo
		if err := windows.GetStartupInfo(&info); err != nil {
			slog.Debug("unable to retrieve startup info", "error", err)
		} else if info.Flags&STARTF_TITLEISLINKNAME == STARTF_TITLEISLINKNAME {
			linkPath := windows.UTF16PtrToString(info.Title)
			if strings.Contains(linkPath, "Startup") {
				startHidden = true
			}
		}
	}
	if startHidden {
		startHiddenTasks()
	} else {
		ptr := wv.Run("/")

		// Set the window icon using the tray icon
		if ptr != nil {
			iconHandle := app.t.GetIconHandle()
			if iconHandle != 0 {
				hwnd := uintptr(ptr)
				const ICON_SMALL = 0
				const ICON_BIG = 1
				const WM_SETICON = 0x0080

				pSendMessage.Call(hwnd, uintptr(WM_SETICON), uintptr(ICON_SMALL), uintptr(iconHandle))
				pSendMessage.Call(hwnd, uintptr(WM_SETICON), uintptr(ICON_BIG), uintptr(iconHandle))
			}
		}

		centerWindow(ptr)
	}

	if !hasCompletedFirstRun {
		// Only create the login shortcut on first start
		// so we can respect users deletion of the link
		err = createLoginShortcut()
		if err != nil {
			slog.Warn("unable to create login shortcut", "error", err)
		}
	}

	app.t.TrayRun() // This will block the main thread
}

// createLoginShortcut 首次运行时复制安装程序提供的开机启动快捷方式。
func createLoginShortcut() error {
	// The installer lays down a shortcut for us so we can copy it without
	// having to resort to calling COM APIs to establish the shortcut
	shortcutOrigin := filepath.Join(appPath, "lib", "Ollama.lnk")

	_, err := os.Stat(startupShortcut)
	if err != nil {
		if errors.Is(err, os.ErrNotExist) {
			in, err := os.Open(shortcutOrigin)
			if err != nil {
				return fmt.Errorf("unable to open shortcut %s : %w", shortcutOrigin, err)
			}
			defer in.Close()
			out, err := os.Create(startupShortcut)
			if err != nil {
				return fmt.Errorf("unable to open startup link %s : %w", startupShortcut, err)
			}
			defer out.Close()
			_, err = io.Copy(out, in)
			if err != nil {
				return fmt.Errorf("unable to copy shortcut %s : %w", startupShortcut, err)
			}
			err = out.Sync()
			if err != nil {
				return fmt.Errorf("unable to sync shortcut %s : %w", startupShortcut, err)
			}
			slog.Info("Created Startup shortcut", "shortcut", startupShortcut)
		} else {
			slog.Warn("unexpected error looking up Startup shortcut", "error", err)
		}
	} else {
		slog.Debug("Startup link already exists", "shortcut", startupShortcut)
	}
	return nil
}

// LaunchNewApp Windows 升级由安装程序处理，此处为空实现。
func LaunchNewApp() {
}

// logStartup 记录 Windows 应用路径与版本。
func logStartup() {
	slog.Info("starting Ollama", "app", appPath, "version", version.Version, "OS", updater.UserAgentOS)
}

const (
	SW_HIDE        = 0  // Hides the window
	SW_SHOW        = 5  // Shows window in its current size/position
	SW_SHOWNA      = 8  // Shows without activating
	SW_MINIMIZE    = 6  // Minimizes the window
	SW_RESTORE     = 9  // Restores to previous size/position
	SW_SHOWDEFAULT = 10 // Sets show state based on program state
	SM_CXSCREEN    = 0
	SM_CYSCREEN    = 1
	HWND_TOP       = 0
	SWP_NOSIZE     = 0x0001
	SWP_NOMOVE     = 0x0002
	SWP_NOZORDER   = 0x0004
	SWP_SHOWWINDOW = 0x0040

	// Menu constants
	MF_STRING     = 0x00000000
	MF_SEPARATOR  = 0x00000800
	MF_GRAYED     = 0x00000001
	TPM_RETURNCMD = 0x0100
)

// POINT 表示屏幕坐标点。
// POINT structure for cursor position
type POINT struct {
	X int32
	Y int32
}

// Rect 为 GetWindowRect 使用的矩形结构。
// Rect structure for GetWindowRect
type Rect struct {
	Left   int32
	Top    int32
	Right  int32
	Bottom int32
}

// centerWindow 将窗口居中到主屏幕。
func centerWindow(ptr unsafe.Pointer) {
	hwnd := uintptr(ptr)
	if hwnd == 0 {
		return
	}

	var rect Rect
	pGetWindowRect.Call(hwnd, uintptr(unsafe.Pointer(&rect)))

	screenWidth, _, _ := pGetSystemMetrics.Call(uintptr(SM_CXSCREEN))
	screenHeight, _, _ := pGetSystemMetrics.Call(uintptr(SM_CYSCREEN))

	windowWidth := rect.Right - rect.Left
	windowHeight := rect.Bottom - rect.Top

	x := (int32(screenWidth) - windowWidth) / 2
	y := (int32(screenHeight) - windowHeight) / 2

	// Ensure the window is not positioned off-screen
	if x < 0 {
		x = 0
	}
	if y < 0 {
		y = 0
	}

	pSetWindowPos.Call(
		hwnd,
		uintptr(HWND_TOP),
		uintptr(x),
		uintptr(y),
		uintptr(windowWidth),  // Keep original width
		uintptr(windowHeight), // Keep original height
		uintptr(SWP_SHOWWINDOW),
	)
}

// showWindow 恢复、显示并将窗口置于前台。
func showWindow(ptr unsafe.Pointer) {
	hwnd := uintptr(ptr)
	if hwnd != 0 {
		iconHandle := app.t.GetIconHandle()
		if iconHandle != 0 {
			const ICON_SMALL = 0
			const ICON_BIG = 1
			const WM_SETICON = 0x0080

			pSendMessage.Call(hwnd, uintptr(WM_SETICON), uintptr(ICON_SMALL), uintptr(iconHandle))
			pSendMessage.Call(hwnd, uintptr(WM_SETICON), uintptr(ICON_BIG), uintptr(iconHandle))
		}

		// Check if window is minimized
		isMinimized, _, _ := pIsIconic.Call(hwnd)
		if isMinimized != 0 {
			// Restore the window if it's minimized
			pShowWindow.Call(hwnd, uintptr(SW_RESTORE))
		}

		// Show the window
		pShowWindow.Call(hwnd, uintptr(SW_SHOW))

		// Bring window to top
		pBringWindowToTop.Call(hwnd)

		// Force window to foreground
		pSetForegroundWindow.Call(hwnd)

		// Make it the active window
		pSetActiveWindow.Call(hwnd)

		// Ensure window is positioned on top
		pSetWindowPos.Call(
			hwnd,
			uintptr(HWND_TOP),
			0, 0, 0, 0,
			uintptr(SWP_NOSIZE|SWP_NOMOVE|SWP_SHOWWINDOW),
		)
	}
}

// HideWindow 隐藏应用主窗口。
// HideWindow hides the application window
func hideWindow(ptr unsafe.Pointer) {
	hwnd := uintptr(ptr)
	if hwnd != 0 {
		pShowWindow.Call(
			hwnd,
			uintptr(SW_HIDE),
		)
	}
}

// runInBackground 以 hidden 参数启动新的应用进程。
func runInBackground() {
	exe, err := os.Executable()
	if err != nil {
		slog.Error("failed to get executable path", "error", err)
		os.Exit(1)
	}
	cmd := exec.Command(exe, "hidden")
	if cmd != nil {
		err = cmd.Run()
		if err != nil {
			slog.Error("failed to run Ollama", "exe", exe, "error", err)
			os.Exit(1)
		}
	} else {
		slog.Error("failed to start Ollama", "exe", exe)
		os.Exit(1)
	}
}

// drag Windows 标题栏拖动由 WebView 处理，此处为空。
func drag(ptr unsafe.Pointer) {}

// doubleClick Windows 双击最大化暂未实现。
func doubleClick(ptr unsafe.Pointer) {}

// checkAndHandleExistingInstance 将 URL 发送给已运行实例并退出。
// checkAndHandleExistingInstance checks if another instance is running and sends the URL to it
func checkAndHandleExistingInstance(urlSchemeRequest string) bool {
	if urlSchemeRequest == "" {
		return false
	}

	// Try to send URL to existing instance using wintray messaging
	if wintray.CheckAndSendToExistingInstance(urlSchemeRequest) {
		os.Exit(0)
		return true
	}

	// No existing instance, we'll handle it ourselves
	return false
}
