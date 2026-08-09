// Copyright 2014 Manu Martinez-Almeida. All rights reserved.
// Use of this source code is governed by a MIT style
// license that can be found in the LICENSE file.

package gin

import (
	"fmt"
	"html/template"
	"net"
	"net/http"
	"os"
	"path"
	"strings"
	"sync"

	"github.com/gin-gonic/gin/internal/bytesconv"
	filesystem "github.com/gin-gonic/gin/internal/fs"
	"github.com/gin-gonic/gin/render"
	"github.com/quic-go/quic-go/http3"
	"golang.org/x/net/http2"
	"golang.org/x/net/http2/h2c"
)

const (
	defaultMultipartMemory = 32 << 20 // 32 MB
	escapedColon           = "\\:"
	colon                  = ":"
	backslash              = "\\"
)

var (
	default404Body = []byte("404 page not found")
	default405Body = []byte("405 method not allowed")
)

var defaultPlatform string

var defaultTrustedCIDRs = []*net.IPNet{
	{ // 0.0.0.0/0 (IPv4)
		IP:   net.IP{0x0, 0x0, 0x0, 0x0},
		Mask: net.IPMask{0x0, 0x0, 0x0, 0x0},
	},
	{ // ::/0 (IPv6)
		IP:   net.IP{0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0},
		Mask: net.IPMask{0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0},
	},
}

// HandlerFunc 定义 gin 中间件使用的处理程序作为返回值。
type HandlerFunc func(*Context)

// OptionFunc 定义更改默认配置的函数
type OptionFunc func(*Engine)

// HandlersChain 定义了一个 HandlerFunc 切片。
type HandlersChain []HandlerFunc

// Last 返回链中的最后一个处理程序。即最后一个处理程序是主要的。
func (c HandlersChain) Last() HandlerFunc {
	if length := len(c); length > 0 {
		return c[length-1]
	}
	return nil
}

// RouteInfo 表示请求路由的规范，其中包含方法和路径及其处理程序。
type RouteInfo struct {
	Method      string
	Path        string
	Handler     string
	HandlerFunc HandlerFunc
}

// RoutesInfo 定义了一个 RouteInfo 切片。
type RoutesInfo []RouteInfo

// 值得信赖的平台
const (
	// 在 Google App Engine 上运行时的 PlatformGoogleAppEngine。信任 X-Appengine-Remote-Addr
	//  用于确定客户端的IP
	PlatformGoogleAppEngine = "X-Appengine-Remote-Addr"
	// 使用 Cloudflare 的 CDN 时的 PlatformCloudflare。信任 CF-Connecting-IP 以确定
	//  客户端的IP
	PlatformCloudflare = "CF-Connecting-IP"
	// 在 Fly.io 上运行时的 PlatformFlyIO。 Trust Fly-Client-IP 用于确定客户端的 IP
	PlatformFlyIO = "Fly-Client-IP"
)

// 引擎是框架的实例，它包含复用器、中间件和配置设置。
//  使用 New() 或 Default() 创建 Engine 实例
type Engine struct {
	RouterGroup

	// RouteTreesUpdated 确保路由树的初始化或更新
	//  （用于路由 HTTP 请求）即使同时调用多次，也仅发生一次。
	routeTreesUpdated sync.Once

	// RedirectTrailingSlash 启用自动重定向，如果当前路由无法匹配，但
	//  存在带有（不带有）尾部斜杠的路径的处理程序。
	//  例如，如果请求 /foo/ 但仅存在 /foo 的路由，则
	//  对于 GET 请求，客户端将重定向到 /foo，HTTP 状态代码为 301
	//  307 用于所有其他请求方法。
	RedirectTrailingSlash bool

	// RedirectFixedPath 如果启用，则路由器尝试修复当前请求路径，如果没有
	//  已为其注册句柄。
	//  首先删除多余的路径元素，例如 ../ 或 //。
	//  然后，路由器对已清理的路径进行不区分大小写的查找。
	//  如果可以找到该路由的句柄，则路由器会进行重定向
	//  到正确的路径，状态代码为 301（对于 GET 请求）和 307（对于 GET 请求）
	//  所有其他请求方法。
	//  例如 /FOO 和 /..//Foo 可以重定向到 /foo。
	//  RedirectTrailingSlash 独立于此选项。
	RedirectFixedPath bool

	// HandleMethodNotAllowed 如果启用，路由器将检查是否允许使用其他方法
	//  当前路由，如果当前请求无法路由。
	//  如果是这种情况，请求将得到“不允许的方法”的答复
	//  和 HTTP 状态代码 405。
	//  如果不允许其他方法，则将请求委托给 NotFound
	//  处理程序。
	HandleMethodNotAllowed bool

	// ForwardedByClientIP 如果启用，将从请求的标头中解析客户端 IP
	//  与 `(*gin.Engine).RemoteIPHeaders` 中存储的内容匹配。如果没有IP
	//  获取后，它会回退到从获取的IP
	//  `(*gin.Context).Request.RemoteAddr`。
	ForwardedByClientIP bool

	// AppEngine 已被弃用。
	//  已弃用：使用 `TrustedPlatform` 代替值 `gin.PlatformGoogleAppEngine`
	//  #726 #755 如果启用，它将信任一些以
	//  “X-AppEngine...”以便更好地与该 PaaS 集成。
	AppEngine bool

	// UseRawPath 如果启用，则 url.RawPath 将用于查找参数。
	//  RawPath 只是一个提示，应该使用 EscapedPath() 代替。 （https://pkg.go.dev/net/url@master#URL）
	//  仅当您知道自己在做什么时才使用 RawPath。
	UseRawPath bool

	// UseEscapedPath 如果启用，将使用 url.EscapedPath() 来查找参数
	//  它覆盖 UseRawPath
	UseEscapedPath bool

	// UnescapePathValues 如果为 true，则路径值将不转义。
	//  如果 UseRawPath 和 UseEscapedPath 为 false（默认情况下），则 UnescapePathValues 实际上为 true，
	//  因为 url.Path 将被使用，它已经是未转义的。
	UnescapePathValues bool

	// 即使带有额外的斜杠，RemoveExtraSlash 也可以从 URL 中解析参数。
	//  请参阅 PR #1817 和问题 #1644
	RemoveExtraSlash bool

	// RemoteIPHeaders 用于获取客户端 IP 时的标头列表
	//  `(*gin.Engine).ForwardedByClientIP` 是 `true` 并且
	//  `(*gin.Context).Request.RemoteAddr` 至少与以下之一匹配
	//  `(*gin.Engine).SetTrustedProxies()` 定义的列表的网络来源。
	RemoteIPHeaders []string

	// TrustedPlatform 如果设置为常量值 gin.Platform*，则信任由
	//  该平台，例如确定客户端IP
	TrustedPlatform string

	// 赋予 http.Request 的 ParseMultipartForm 的“maxMemory”参数的 MaxMultipartMemory 值
	//  方法调用。
	MaxMultipartMemory int64

	// UseH2C 启用 h2c 支持。
	UseH2C bool

	// 当 Context.Request.Context() 不为零时，ContextWithFallback 启用回退 Context.Deadline()、Context.Done()、Context.Err() 和 Context.Value()。
	ContextWithFallback bool

	delims           render.Delims
	secureJSONPrefix string
	HTMLRender       render.HTMLRender
	FuncMap          template.FuncMap
	allNoRoute       HandlersChain
	allNoMethod      HandlersChain
	noRoute          HandlersChain
	noMethod         HandlersChain
	pool             sync.Pool
	trees            methodTrees
	maxParams        uint16
	maxSections      uint16
	trustedProxies   []string
	trustedCIDRs     []*net.IPNet
}

var _ IRouter = (*Engine)(nil)

// New 返回一个新的空白 Engine 实例，不附加任何中间件。
//  默认情况下，配置为：
//  -RedirectTrailingSlash：true
//  - 重定向固定路径：假
//  - HandleMethodNotAllowed：假
//  - 由客户端IP转发：true
//  - 使用原始路径：假
//  - 使用转义路径：假
//  - UnescapePathValues：true
func New(opts ...OptionFunc) *Engine {
	debugPrintWARNINGNew()
	engine := &Engine{
		RouterGroup: RouterGroup{
			Handlers: nil,
			basePath: "/",
			root:     true,
		},
		FuncMap:                template.FuncMap{},
		RedirectTrailingSlash:  true,
		RedirectFixedPath:      false,
		HandleMethodNotAllowed: false,
		ForwardedByClientIP:    true,
		RemoteIPHeaders:        []string{"X-Forwarded-For", "X-Real-IP"},
		TrustedPlatform:        defaultPlatform,
		UseRawPath:             false,
		UseEscapedPath:         false,
		RemoveExtraSlash:       false,
		UnescapePathValues:     true,
		MaxMultipartMemory:     defaultMultipartMemory,
		trees:                  make(methodTrees, 0, 9),
		delims:                 render.Delims{Left: "{{", Right: "}}"},
		secureJSONPrefix:       "while(1);",
		trustedProxies:         []string{"0.0.0.0/0", "::/0"},
		trustedCIDRs:           defaultTrustedCIDRs,
	}
	engine.engine = engine
	engine.pool.New = func() any {
		return engine.allocateContext(engine.maxParams)
	}
	return engine.With(opts...)
}

// 默认返回一个已连接 Logger 和 Recovery 中间件的 Engine 实例。
func Default(opts ...OptionFunc) *Engine {
	debugPrintWARNINGDefault()
	engine := New()
	engine.Use(Logger(), Recovery())
	return engine.With(opts...)
}

func (engine *Engine) Handler() http.Handler {
	if !engine.UseH2C {
		return engine
	}

	h2s := &http2.Server{}
	return h2c.NewHandler(engine, h2s)
}

func (engine *Engine) allocateContext(maxParams uint16) *Context {
	v := make(Params, 0, maxParams)
	skippedNodes := make([]skippedNode, 0, engine.maxSections)
	return &Context{engine: engine, params: &v, skippedNodes: &skippedNodes}
}

// Delims 设置模板左右分隔符并返回一个 Engine 实例。
func (engine *Engine) Delims(left, right string) *Engine {
	engine.delims = render.Delims{Left: left, Right: right}
	return engine
}

// SecureJsonPrefix 设置 Context.SecureJSON 中使用的 secureJSONPrefix。
func (engine *Engine) SecureJsonPrefix(prefix string) *Engine {
	engine.secureJSONPrefix = prefix
	return engine
}

// LoadHTMLGlob 加载由 glob 模式标识的 HTML 文件
//  并将结果与 HTML 渲染器相关联。
func (engine *Engine) LoadHTMLGlob(pattern string) {
	left := engine.delims.Left
	right := engine.delims.Right
	templ := template.Must(template.New("").Delims(left, right).Funcs(engine.FuncMap).ParseGlob(pattern))

	if IsDebugging() {
		debugPrintLoadTemplate(templ)
		engine.HTMLRender = render.HTMLDebug{Glob: pattern, FuncMap: engine.FuncMap, Delims: engine.delims}
		return
	}

	engine.SetHTMLTemplate(templ)
}

// LoadHTMLFiles 加载一段 HTML 文件
//  并将结果与 HTML 渲染器相关联。
func (engine *Engine) LoadHTMLFiles(files ...string) {
	if IsDebugging() {
		engine.HTMLRender = render.HTMLDebug{Files: files, FuncMap: engine.FuncMap, Delims: engine.delims}
		return
	}

	templ := template.Must(template.New("").Delims(engine.delims.Left, engine.delims.Right).Funcs(engine.FuncMap).ParseFiles(files...))
	engine.SetHTMLTemplate(templ)
}

// LoadHTMLFS 加载 http.FileSystem 和一片模式
//  并将结果与 HTML 渲染器相关联。
func (engine *Engine) LoadHTMLFS(fs http.FileSystem, patterns ...string) {
	if IsDebugging() {
		engine.HTMLRender = render.HTMLDebug{FileSystem: fs, Patterns: patterns, FuncMap: engine.FuncMap, Delims: engine.delims}
		return
	}

	templ := template.Must(template.New("").Delims(engine.delims.Left, engine.delims.Right).Funcs(engine.FuncMap).ParseFS(
		filesystem.FileSystem{FileSystem: fs}, patterns...))
	engine.SetHTMLTemplate(templ)
}

// SetHTMLTemplate 将模板与 HTML 渲染器关联起来。
func (engine *Engine) SetHTMLTemplate(templ *template.Template) {
	if len(engine.trees) > 0 {
		debugPrintWARNINGSetHTMLTemplate()
	}

	engine.HTMLRender = render.HTMLProduction{Template: templ.Funcs(engine.FuncMap)}
}

// SetFuncMap 设置用于 template.FuncMap 的 FuncMap。
func (engine *Engine) SetFuncMap(funcMap template.FuncMap) {
	engine.FuncMap = funcMap
}

// NoRoute 添加了 NoRoute 的处理程序。默认情况下它返回 404 代码。
func (engine *Engine) NoRoute(handlers ...HandlerFunc) {
	engine.noRoute = handlers
	engine.rebuild404Handlers()
}

// NoMethod 设置当 Engine.HandleMethodNotAllowed = true 时调用的处理程序。
func (engine *Engine) NoMethod(handlers ...HandlerFunc) {
	engine.noMethod = handlers
	engine.rebuild405Handlers()
}

// 使用将全局中间件附加到路由器。即通过 Use() 附加的中间件将是
//  包含在每个请求的处理程序链中。甚至404、405、静态文件……
//  例如，这是记录器或错误管理中间件的正确位置。
func (engine *Engine) Use(middleware ...HandlerFunc) IRoutes {
	engine.RouterGroup.Use(middleware...)
	engine.rebuild404Handlers()
	engine.rebuild405Handlers()
	return engine
}

// With 返回一个具有 OptionFunc 中设置的配置的引擎。
func (engine *Engine) With(opts ...OptionFunc) *Engine {
	for _, opt := range opts {
		opt(engine)
	}

	return engine
}

func (engine *Engine) rebuild404Handlers() {
	engine.allNoRoute = engine.combineHandlers(engine.noRoute)
}

func (engine *Engine) rebuild405Handlers() {
	engine.allNoMethod = engine.combineHandlers(engine.noMethod)
}

func (engine *Engine) addRoute(method, path string, handlers HandlersChain) {
	assert1(path[0] == '/', "path must begin with '/'")
	assert1(method != "", "HTTP method can not be empty")
	assert1(len(handlers) > 0, "there must be at least one handler")

	debugPrintRoute(method, path, handlers)

	root := engine.trees.get(method)
	if root == nil {
		root = new(node)
		root.fullPath = "/"
		engine.trees = append(engine.trees, methodTree{method: method, root: root})
	}
	root.addRoute(path, handlers)

	if paramsCount := countParams(path); paramsCount > engine.maxParams {
		engine.maxParams = paramsCount
	}

	if sectionsCount := countSections(path); sectionsCount > engine.maxSections {
		engine.maxSections = sectionsCount
	}
}

// 路由返回注册路由的一部分，包括一些有用的信息，例如：
//  http 方法、路径和处理程序名称。
func (engine *Engine) Routes() (routes RoutesInfo) {
	for _, tree := range engine.trees {
		routes = iterate("", tree.method, routes, tree.root)
	}
	return routes
}

func iterate(path, method string, routes RoutesInfo, root *node) RoutesInfo {
	path += root.path
	if len(root.handlers) > 0 {
		handlerFunc := root.handlers.Last()
		routes = append(routes, RouteInfo{
			Method:      method,
			Path:        path,
			Handler:     nameOfFunction(handlerFunc),
			HandlerFunc: handlerFunc,
		})
	}
	for _, child := range root.children {
		routes = iterate(path, method, routes, child)
	}
	return routes
}

func (engine *Engine) prepareTrustedCIDRs() ([]*net.IPNet, error) {
	if engine.trustedProxies == nil {
		return nil, nil
	}

	cidr := make([]*net.IPNet, 0, len(engine.trustedProxies))
	for _, trustedProxy := range engine.trustedProxies {
		if !strings.Contains(trustedProxy, "/") {
			ip := parseIP(trustedProxy)
			if ip == nil {
				return cidr, &net.ParseError{Type: "IP address", Text: trustedProxy}
			}

			switch len(ip) {
			case net.IPv4len:
				trustedProxy += "/32"
			case net.IPv6len:
				trustedProxy += "/128"
			}
		}
		_, cidrNet, err := net.ParseCIDR(trustedProxy)
		if err != nil {
			return cidr, err
		}
		cidr = append(cidr, cidrNet)
	}
	return cidr, nil
}

// SetTrustedProxies 设置网络来源列表（IPv4 地址、
//  值得信任的 IPv4 CIDR、IPv6 地址或 IPv6 CIDR）
//  请求的标头包含备用客户端 IP
//  `(*gin.Engine).ForwardedByClientIP` 是 `true`。 `TrustedProxies`
//  该功能默认启用，并且它还信任所有代理
//  默认情况下。如果您想禁用此功能，请使用
//  Engine.SetTrustedProxies(nil)，然后 Context.ClientIP() 将
//  直接返回远程地址。
func (engine *Engine) SetTrustedProxies(trustedProxies []string) error {
	engine.trustedProxies = trustedProxies
	return engine.parseTrustedProxies()
}

// isUnsafeTrustedProxies 检查 Engine.trustedCIDRs 是否包含所有 IP，如果包含则不安全（返回 true）
func (engine *Engine) isUnsafeTrustedProxies() bool {
	return engine.isTrustedProxy(net.ParseIP("0.0.0.0")) || engine.isTrustedProxy(net.ParseIP("::"))
}

// parseTrustedProxies 将 Engine.trustedProxies 解析为 Engine.trustedCIDRs
func (engine *Engine) parseTrustedProxies() error {
	trustedCIDRs, err := engine.prepareTrustedCIDRs()
	engine.trustedCIDRs = trustedCIDRs
	return err
}

// isTrustedProxy会根据Engine.trustedCIDRs检查IP地址是否包含在可信列表中
func (engine *Engine) isTrustedProxy(ip net.IP) bool {
	if engine.trustedCIDRs == nil {
		return false
	}
	for _, cidr := range engine.trustedCIDRs {
		if cidr.Contains(ip) {
			return true
		}
	}
	return false
}

// validateHeader 将解析 X-Forwarded-For 标头并返回可信客户端 IP 地址
func (engine *Engine) validateHeader(header string) (clientIP string, valid bool) {
	if header == "" {
		return "", false
	}
	items := strings.Split(header, ",")
	for i := len(items) - 1; i >= 0; i-- {
		ipStr := strings.TrimSpace(items[i])
		ip := net.ParseIP(ipStr)
		if ip == nil {
			break
		}

		// X-Forwarded-For 由代理附加
		//  按相反顺序检查 IP，发现不受信任的代理时停止
		if (i == 0) || (!engine.isTrustedProxy(ip)) {
			return ipStr, true
		}
	}
	return "", false
}

// updateRouteTree 递归更新路由树
func updateRouteTree(n *node) {
	n.path = strings.ReplaceAll(n.path, escapedColon, colon)
	n.fullPath = strings.ReplaceAll(n.fullPath, escapedColon, colon)
	n.indices = strings.ReplaceAll(n.indices, backslash, colon)
	if n.children == nil {
		return
	}
	for _, child := range n.children {
		updateRouteTree(child)
	}
}

// updateRouteTrees 更新路由树
func (engine *Engine) updateRouteTrees() {
	for _, tree := range engine.trees {
		updateRouteTree(tree.root)
	}
}

// parseIP 解析 IP 的字符串表示形式并返回 net.IP
//  最小字节表示或 nil（如果输入无效）。
func parseIP(ip string) net.IP {
	parsedIP := net.ParseIP(ip)

	if ipv4 := parsedIP.To4(); ipv4 != nil {
		// 以 4 字节表示形式返回 ip
		return ipv4
	}

	// 以 16 字节表示形式返回 ip 或 nil
	return parsedIP
}

// Run 将路由器连接到 http.Server 并开始侦听和服务 HTTP 请求。
//  这是 http.ListenAndServe(addr, router) 的快捷方式
//  注意：除非发生错误，否则该方法将无限期地阻塞调用的 goroutine。
func (engine *Engine) Run(addr ...string) (err error) {
	defer func() { debugPrintError(err) }()

	if engine.isUnsafeTrustedProxies() {
		debugPrint("[WARNING] You trusted all proxies, this is NOT safe. We recommend you to set a value.\n" +
			"Please check https://github.com/gin-gonic/gin/blob/master/docs/doc.md#dont-trust-all-proxies for details.")
	}
	engine.updateRouteTrees()
	address := resolveAddress(addr)
	debugPrint("Listening and serving HTTP on %s\n", address)
	server := &http.Server{ // #nosec G112
		Addr:    address,
		Handler: engine.Handler(),
	}
	err = server.ListenAndServe()
	return
}

// RunTLS 将路由器连接到 http.Server 并开始侦听和服务 HTTPS（安全）请求。
//  它是 http.ListenAndServeTLS(addr, certFile, keyFile, router) 的快捷方式
//  注意：除非发生错误，否则该方法将无限期地阻塞调用的 goroutine。
func (engine *Engine) RunTLS(addr, certFile, keyFile string) (err error) {
	debugPrint("Listening and serving HTTPS on %s\n", addr)
	defer func() { debugPrintError(err) }()

	if engine.isUnsafeTrustedProxies() {
		debugPrint("[WARNING] You trusted all proxies, this is NOT safe. We recommend you to set a value.\n" +
			"Please check https://github.com/gin-gonic/gin/blob/master/docs/doc.md#dont-trust-all-proxies for details.")
	}

	server := &http.Server{ // #nosec G112
		Addr:    addr,
		Handler: engine.Handler(),
	}
	err = server.ListenAndServeTLS(certFile, keyFile)
	return
}

// RunUnix 将路由器连接到 http.Server 并开始侦听和服务 HTTP 请求
//  通过指定的unix套接字（即文件）。
//  注意：除非发生错误，否则该方法将无限期地阻塞调用的 goroutine。
func (engine *Engine) RunUnix(file string) (err error) {
	debugPrint("Listening and serving HTTP on unix:/%s", file)
	defer func() { debugPrintError(err) }()

	if engine.isUnsafeTrustedProxies() {
		debugPrint("[WARNING] You trusted all proxies, this is NOT safe. We recommend you to set a value.\n" +
			"Please check https://github.com/gin-gonic/gin/blob/master/docs/doc.md#dont-trust-all-proxies for details.")
	}

	listener, err := net.Listen("unix", file)
	if err != nil {
		return
	}
	defer listener.Close()
	defer os.Remove(file)

	server := &http.Server{ // #nosec G112
		Handler: engine.Handler(),
	}
	err = server.Serve(listener)
	return
}

// RunFd 将路由器连接到 http.Server 并开始侦听和服务 HTTP 请求
//  通过指定的文件描述符。
//  注意：除非发生错误，否则该方法将无限期地阻塞调用的 goroutine。
func (engine *Engine) RunFd(fd int) (err error) {
	debugPrint("Listening and serving HTTP on fd@%d", fd)
	defer func() { debugPrintError(err) }()

	if engine.isUnsafeTrustedProxies() {
		debugPrint("[WARNING] You trusted all proxies, this is NOT safe. We recommend you to set a value.\n" +
			"Please check https://github.com/gin-gonic/gin/blob/master/docs/doc.md#dont-trust-all-proxies for details.")
	}

	f := os.NewFile(uintptr(fd), fmt.Sprintf("fd@%d", fd))
	defer f.Close()
	listener, err := net.FileListener(f)
	if err != nil {
		return
	}
	defer listener.Close()
	err = engine.RunListener(listener)
	return
}

// RunQUIC 将路由器连接到 http.Server 并开始侦听和服务 QUIC 请求。
//  这是 http3.ListenAndServeQUIC(addr, certFile, keyFile, router) 的快捷方式
//  注意：除非发生错误，否则该方法将无限期地阻塞调用的 goroutine。
func (engine *Engine) RunQUIC(addr, certFile, keyFile string) (err error) {
	debugPrint("Listening and serving QUIC on %s\n", addr)
	defer func() { debugPrintError(err) }()

	if engine.isUnsafeTrustedProxies() {
		debugPrint("[WARNING] You trusted all proxies, this is NOT safe. We recommend you to set a value.\n" +
			"Please check https://github.com/gin-gonic/gin/blob/master/docs/doc.md#dont-trust-all-proxies for details.")
	}

	err = http3.ListenAndServeQUIC(addr, certFile, keyFile, engine.Handler())
	return
}

// RunListener 将路由器连接到 http.Server 并开始侦听和服务 HTTP 请求
//  通过指定的net.Listener
func (engine *Engine) RunListener(listener net.Listener) (err error) {
	debugPrint("Listening and serving HTTP on listener what's bind with address@%s", listener.Addr())
	defer func() { debugPrintError(err) }()

	if engine.isUnsafeTrustedProxies() {
		debugPrint("[WARNING] You trusted all proxies, this is NOT safe. We recommend you to set a value.\n" +
			"Please check https://github.com/gin-gonic/gin/blob/master/docs/doc.md#dont-trust-all-proxies for details.")
	}

	server := &http.Server{ // #nosec G112
		Handler: engine.Handler(),
	}
	err = server.Serve(listener)
	return
}

// ServeHTTP 符合 http.Handler 接口。
func (engine *Engine) ServeHTTP(w http.ResponseWriter, req *http.Request) {
	engine.routeTreesUpdated.Do(func() {
		engine.updateRouteTrees()
	})

	c := engine.pool.Get().(*Context)
	c.writermem.reset(w)
	c.Request = req
	c.reset()

	engine.handleHTTPRequest(c)

	engine.pool.Put(c)
}

// HandleContext 重新进入已重写的上下文。
//  这可以通过将 c.Request.URL.Path 设置为新目标来完成。
//  免责声明：你可以自己循环来处理这个问题，明智地使用。
func (engine *Engine) HandleContext(c *Context) {
	oldIndexValue := c.index
	oldHandlers := c.handlers
	c.reset()
	engine.handleHTTPRequest(c)

	c.index = oldIndexValue
	c.handlers = oldHandlers
}

func (engine *Engine) handleHTTPRequest(c *Context) {
	httpMethod := c.Request.Method
	rPath := c.Request.URL.Path
	unescape := false

	if engine.UseEscapedPath {
		rPath = c.Request.URL.EscapedPath()
		unescape = engine.UnescapePathValues
	} else if engine.UseRawPath && len(c.Request.URL.RawPath) > 0 {
		rPath = c.Request.URL.RawPath
		unescape = engine.UnescapePathValues
	}

	if engine.RemoveExtraSlash {
		rPath = cleanPath(rPath)
	}

	// 查找给定 HTTP 方法的树根
	t := engine.trees
	for i, tl := 0, len(t); i < tl; i++ {
		if t[i].method != httpMethod {
			continue
		}
		root := t[i].root
		// 在树中查找路径
		value := root.getValue(rPath, c.params, c.skippedNodes, unescape)
		if value.params != nil {
			c.Params = *value.params
		}
		if value.handlers != nil {
			c.handlers = value.handlers
			c.fullPath = value.fullPath
			c.Next()
			c.writermem.WriteHeaderNow()
			return
		}
		if httpMethod != http.MethodConnect && rPath != "/" {
			if value.tsr && engine.RedirectTrailingSlash {
				redirectTrailingSlash(c)
				return
			}
			if engine.RedirectFixedPath && redirectFixedPath(c, root, engine.RedirectFixedPath) {
				return
			}
		}
		break
	}

	if engine.HandleMethodNotAllowed && len(t) > 0 {
		// 根据 RFC 7231 第 6.5.5 节，必须在响应中生成允许头字段
		//  包含目标资源当前支持的方法的列表。
		allowed := make([]string, 0, len(t)-1)
		for _, tree := range engine.trees {
			if tree.method == httpMethod {
				continue
			}
			if value := tree.root.getValue(rPath, nil, c.skippedNodes, unescape); value.handlers != nil {
				allowed = append(allowed, tree.method)
			}
		}
		if len(allowed) > 0 {
			c.handlers = engine.allNoMethod
			c.writermem.Header().Set("Allow", strings.Join(allowed, ", "))
			serveError(c, http.StatusMethodNotAllowed, default405Body)
			return
		}
	}

	c.handlers = engine.allNoRoute
	serveError(c, http.StatusNotFound, default404Body)
}

var mimePlain = []string{MIMEPlain}

func serveError(c *Context, code int, defaultMessage []byte) {
	c.writermem.status = code
	c.Next()
	if c.writermem.Written() {
		return
	}
	if c.writermem.Status() == code {
		c.writermem.Header()["Content-Type"] = mimePlain
		_, err := c.Writer.Write(defaultMessage)
		if err != nil {
			debugPrint("cannot write message to writer during serve error: %v", err)
		}
		return
	}
	c.writermem.WriteHeaderNow()
}

func redirectTrailingSlash(c *Context) {
	req := c.Request
	p := req.URL.Path
	if prefix := path.Clean(c.Request.Header.Get("X-Forwarded-Prefix")); prefix != "." {
		prefix = sanitizePathChars(prefix)
		prefix = removeRepeatedChar(prefix, '/')

		p = prefix + "/" + req.URL.Path
	}
	req.URL.Path = p + "/"
	if length := len(p); length > 1 && p[length-1] == '/' {
		req.URL.Path = p[:length-1]
	}
	redirectRequest(c)
}

// sanitizePathChars 从路径字符串中删除不安全的字符，
//  仅保留 ASCII 字母、ASCII 数字、正斜杠和连字符。
func sanitizePathChars(s string) string {
	return strings.Map(func(r rune) rune {
		if (r >= 'a' && r <= 'z') || (r >= 'A' && r <= 'Z') || (r >= '0' && r <= '9') || r == '/' || r == '-' {
			return r
		}
		return -1
	}, s)
}

func redirectFixedPath(c *Context, root *node, trailingSlash bool) bool {
	req := c.Request
	rPath := req.URL.Path

	if fixedPath, ok := root.findCaseInsensitivePath(cleanPath(rPath), trailingSlash); ok {
		req.URL.Path = bytesconv.BytesToString(fixedPath)
		redirectRequest(c)
		return true
	}
	return false
}

func redirectRequest(c *Context) {
	req := c.Request
	rPath := req.URL.Path
	rURL := req.URL.String()

	code := http.StatusMovedPermanently // Permanent redirect, request with GET method
	if req.Method != http.MethodGet {
		code = http.StatusTemporaryRedirect
	}
	debugPrint("redirecting request %d: %s --> %s", code, rPath, rURL)
	http.Redirect(c.Writer, req, rURL, code)
	c.writermem.WriteHeaderNow()
}
