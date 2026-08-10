/**
 * 由 Go 结构体自动生成的 TypeScript 类型；请勿手工修改。
 * Do not change, this code is generated from Golang structs
 */


/** 聊天会话摘要信息（id、标题、摘录与时间戳）。 */
export class ChatInfo {
    id: string;
    title: string;
    userExcerpt: string;
    createdAt: Date;
    updatedAt: Date;

    constructor(source: any = {}) {
        if ('string' === typeof source) source = JSON.parse(source);
        this.id = source["id"];
        this.title = source["title"];
        this.userExcerpt = source["userExcerpt"];
        this.createdAt = new Date(source["createdAt"]);
        this.updatedAt = new Date(source["updatedAt"]);
    }
}
/** 聊天列表 API 响应，包含多条 ChatInfo。 */
export class ChatsResponse {
    chatInfos: ChatInfo[];

    constructor(source: any = {}) {
        if ('string' === typeof source) source = JSON.parse(source);
        this.chatInfos = this.convertValues(source["chatInfos"], ChatInfo);
    }

	convertValues(a: any, classs: any, asMap: boolean = false): any {
	    if (!a) {
	        return a;
	    }
	    if (Array.isArray(a)) {
	        return (a as any[]).map(elem => this.convertValues(elem, classs));
	    } else if ("object" === typeof a) {
	        if (asMap) {
	            for (const key of Object.keys(a)) {
	                a[key] = new classs(a[key]);
	            }
	            return a;
	        }
	        return new classs(a);
	    }
	    return a;
	}
}
/** Go time.Time 在 TypeScript 侧的占位类型。 */
export class Time {


    constructor(source: any = {}) {
        if ('string' === typeof source) source = JSON.parse(source);

    }
}
/** 工具调用中的函数名、参数 JSON 与可选结果。 */
export class ToolFunction {
    name: string;
    arguments: string;
    result?: any;

    constructor(source: any = {}) {
        if ('string' === typeof source) source = JSON.parse(source);
        this.name = source["name"];
        this.arguments = source["arguments"];
        this.result = source["result"];
    }
}
/** 模型发起的一次工具调用（类型 + 函数详情）。 */
export class ToolCall {
    type: string;
    function: ToolFunction;

    constructor(source: any = {}) {
        if ('string' === typeof source) source = JSON.parse(source);
        this.type = source["type"];
        this.function = this.convertValues(source["function"], ToolFunction);
    }

	convertValues(a: any, classs: any, asMap: boolean = false): any {
	    if (!a) {
	        return a;
	    }
	    if (Array.isArray(a)) {
	        return (a as any[]).map(elem => this.convertValues(elem, classs));
	    } else if ("object" === typeof a) {
	        if (asMap) {
	            for (const key of Object.keys(a)) {
	                a[key] = new classs(a[key]);
	            }
	            return a;
	        }
	        return new classs(a);
	    }
	    return a;
	}
}
/** 消息附件的文件名与二进制数据。 */
export class File {
    filename: string;
    data: number[];

    constructor(source: any = {}) {
        if ('string' === typeof source) source = JSON.parse(source);
        this.filename = source["filename"];
        this.data = source["data"];
    }
}
/** 聊天消息：角色、正文、思考内容、附件与工具调用等。 */
export class Message {
    role: string;
    content: string;
    thinking: string;
    stream: boolean;
    model?: string;
    attachments?: File[];
    tool_calls?: ToolCall[];
    tool_call?: ToolCall;
    tool_name?: string;
    tool_result?: number[];
    created_at: Time;
    updated_at: Time;
    thinkingTimeStart?: Date | undefined;
    thinkingTimeEnd?: Date | undefined;

    constructor(source: any = {}) {
        if ('string' === typeof source) source = JSON.parse(source);
        this.role = source["role"];
        this.content = source["content"];
        this.thinking = source["thinking"];
        this.stream = source["stream"];
        this.model = source["model"];
        this.attachments = this.convertValues(source["attachments"], File);
        this.tool_calls = this.convertValues(source["tool_calls"], ToolCall);
        this.tool_call = this.convertValues(source["tool_call"], ToolCall);
        this.tool_name = source["tool_name"];
        this.tool_result = source["tool_result"];
        this.created_at = this.convertValues(source["created_at"], Time);
        this.updated_at = this.convertValues(source["updated_at"], Time);
        this.thinkingTimeStart = source["thinkingTimeStart"] && new Date(source["thinkingTimeStart"]);
        this.thinkingTimeEnd = source["thinkingTimeEnd"] && new Date(source["thinkingTimeEnd"]);
    }

	convertValues(a: any, classs: any, asMap: boolean = false): any {
	    if (!a) {
	        return a;
	    }
	    if (Array.isArray(a)) {
	        return (a as any[]).map(elem => this.convertValues(elem, classs));
	    } else if ("object" === typeof a) {
	        if (asMap) {
	            for (const key of Object.keys(a)) {
	                a[key] = new classs(a[key]);
	            }
	            return a;
	        }
	        return new classs(a);
	    }
	    return a;
	}
}
/** 完整聊天会话：消息列表、标题与浏览器状态等。 */
export class Chat {
    id: string;
    messages: Message[];
    title: string;
    created_at: Time;
    browser_state?: BrowserStateData;

    constructor(source: any = {}) {
        if ('string' === typeof source) source = JSON.parse(source);
        this.id = source["id"];
        this.messages = this.convertValues(source["messages"], Message);
        this.title = source["title"];
        this.created_at = this.convertValues(source["created_at"], Time);
        this.browser_state = source["browser_state"];
    }

	convertValues(a: any, classs: any, asMap: boolean = false): any {
	    if (!a) {
	        return a;
	    }
	    if (Array.isArray(a)) {
	        return (a as any[]).map(elem => this.convertValues(elem, classs));
	    } else if ("object" === typeof a) {
	        if (asMap) {
	            for (const key of Object.keys(a)) {
	                a[key] = new classs(a[key]);
	            }
	            return a;
	        }
	        return new classs(a);
	    }
	    return a;
	}
}
/** 单条聊天详情 API 响应。 */
export class ChatResponse {
    chat: Chat;

    constructor(source: any = {}) {
        if ('string' === typeof source) source = JSON.parse(source);
        this.chat = this.convertValues(source["chat"], Chat);
    }

	convertValues(a: any, classs: any, asMap: boolean = false): any {
	    if (!a) {
	        return a;
	    }
	    if (Array.isArray(a)) {
	        return (a as any[]).map(elem => this.convertValues(elem, classs));
	    } else if ("object" === typeof a) {
	        if (asMap) {
	            for (const key of Object.keys(a)) {
	                a[key] = new classs(a[key]);
	            }
	            return a;
	        }
	        return new classs(a);
	    }
	    return a;
	}
}
/** 本地或远程模型标识（名称、摘要与修改时间）。 */
export class Model {
    model: string;
    digest?: string;
    modified_at?: Time;

    constructor(source: any = {}) {
        if ('string' === typeof source) source = JSON.parse(source);
        this.model = source["model"];
        this.digest = source["digest"];
        this.modified_at = this.convertValues(source["modified_at"], Time);
    }

	convertValues(a: any, classs: any, asMap: boolean = false): any {
	    if (!a) {
	        return a;
	    }
	    if (Array.isArray(a)) {
	        return (a as any[]).map(elem => this.convertValues(elem, classs));
	    } else if ("object" === typeof a) {
	        if (asMap) {
	            for (const key of Object.keys(a)) {
	                a[key] = new classs(a[key]);
	            }
	            return a;
	        }
	        return new classs(a);
	    }
	    return a;
	}
}
/** 模型列表 API 响应。 */
export class ModelsResponse {
    models: Model[];

    constructor(source: any = {}) {
        if ('string' === typeof source) source = JSON.parse(source);
        this.models = this.convertValues(source["models"], Model);
    }

	convertValues(a: any, classs: any, asMap: boolean = false): any {
	    if (!a) {
	        return a;
	    }
	    if (Array.isArray(a)) {
	        return (a as any[]).map(elem => this.convertValues(elem, classs));
	    } else if ("object" === typeof a) {
	        if (asMap) {
	            for (const key of Object.keys(a)) {
	                a[key] = new classs(a[key]);
	            }
	            return a;
	        }
	        return new classs(a);
	    }
	    return a;
	}
}
/** 单块推理设备的库、驱动与显存描述。 */
export class InferenceCompute {
    library: string;
    variant: string;
    compute: string;
    driver: string;
    name: string;
    vram: string;

    constructor(source: any = {}) {
        if ('string' === typeof source) source = JSON.parse(source);
        this.library = source["library"];
        this.variant = source["variant"];
        this.compute = source["compute"];
        this.driver = source["driver"];
        this.name = source["name"];
        this.vram = source["vram"];
    }
}
/** 推理算力探测结果与默认上下文长度。 */
export class InferenceComputeResponse {
    inferenceComputes: InferenceCompute[];
    defaultContextLength: number;

    constructor(source: any = {}) {
        if ('string' === typeof source) source = JSON.parse(source);
        this.inferenceComputes = this.convertValues(source["inferenceComputes"], InferenceCompute);
        this.defaultContextLength = source["defaultContextLength"];
    }

	convertValues(a: any, classs: any, asMap: boolean = false): any {
	    if (!a) {
	        return a;
	    }
	    if (Array.isArray(a)) {
	        return (a as any[]).map(elem => this.convertValues(elem, classs));
	    } else if ("object" === typeof a) {
	        if (asMap) {
	            for (const key of Object.keys(a)) {
	                a[key] = new classs(a[key]);
	            }
	            return a;
	        }
	        return new classs(a);
	    }
	    return a;
	}
}
/** 模型能力标签列表（如 vision、tools）。 */
export class ModelCapabilitiesResponse {
    capabilities: string[];

    constructor(source: any = {}) {
        if ('string' === typeof source) source = JSON.parse(source);
        this.capabilities = source["capabilities"];
    }
}
/** 聊天流式 SSE/JSONL 事件联合体（正文、思考、工具等）。 */
export class ChatEvent {
    eventName: "chat" | "thinking" | "assistant_with_tools" | "tool_call" | "tool" | "tool_result" | "done" | "chat_created";
    content?: string;
    thinking?: string;
    thinkingTimeStart?: Date | undefined;
    thinkingTimeEnd?: Date | undefined;
    toolCalls?: ToolCall[];
    toolCall?: ToolCall;
    toolName?: string;
    toolResult?: boolean;
    toolResultData?: any;
    chatId?: string;
    toolState?: any;

    constructor(source: any = {}) {
        if ('string' === typeof source) source = JSON.parse(source);
        this.eventName = source["eventName"];
        this.content = source["content"];
        this.thinking = source["thinking"];
        this.thinkingTimeStart = source["thinkingTimeStart"] && new Date(source["thinkingTimeStart"]);
        this.thinkingTimeEnd = source["thinkingTimeEnd"] && new Date(source["thinkingTimeEnd"]);
        this.toolCalls = this.convertValues(source["toolCalls"], ToolCall);
        this.toolCall = this.convertValues(source["toolCall"], ToolCall);
        this.toolName = source["toolName"];
        this.toolResult = source["toolResult"];
        this.toolResultData = source["toolResultData"];
        this.chatId = source["chatId"];
        this.toolState = source["toolState"];
    }

	convertValues(a: any, classs: any, asMap: boolean = false): any {
	    if (!a) {
	        return a;
	    }
	    if (Array.isArray(a)) {
	        return (a as any[]).map(elem => this.convertValues(elem, classs));
	    } else if ("object" === typeof a) {
	        if (asMap) {
	            for (const key of Object.keys(a)) {
	                a[key] = new classs(a[key]);
	            }
	            return a;
	        }
	        return new classs(a);
	    }
	    return a;
	}
}
/** 模型下载进度事件。 */
export class DownloadEvent {
    eventName: "download";
    total: number;
    completed: number;
    done: boolean;

    constructor(source: any = {}) {
        if ('string' === typeof source) source = JSON.parse(source);
        this.eventName = source["eventName"];
        this.total = source["total"];
        this.completed = source["completed"];
        this.done = source["done"];
    }
}
/** 聊天或 API 错误事件。 */
export class ErrorEvent {
    eventName: "error";
    error: string;
    code?: string;
    details?: string;

    constructor(source: any = {}) {
        if ('string' === typeof source) source = JSON.parse(source);
        this.eventName = source["eventName"];
        this.error = source["error"];
        this.code = source["code"];
        this.details = source["details"];
    }
}
/** 桌面应用用户设置（模型、功能开关与 UI 状态）。 */
export class Settings {
    Expose: boolean;
    Browser: boolean;
    Survey: boolean;
    Models: string;
    Agent: boolean;
    Tools: boolean;
    WorkingDir: string;
    ContextLength: number;
    TurboEnabled: boolean;
    WebSearchEnabled: boolean;
    ThinkEnabled: boolean;
    ThinkLevel: string;
    SelectedModel: string;
    SidebarOpen: boolean;
    LastHomeView: string;
    AutoUpdateEnabled: boolean;

    constructor(source: any = {}) {
        if ('string' === typeof source) source = JSON.parse(source);
        this.Expose = source["Expose"];
        this.Browser = source["Browser"];
        this.Survey = source["Survey"];
        this.Models = source["Models"];
        this.Agent = source["Agent"];
        this.Tools = source["Tools"];
        this.WorkingDir = source["WorkingDir"];
        this.ContextLength = source["ContextLength"];
        this.TurboEnabled = source["TurboEnabled"];
        this.WebSearchEnabled = source["WebSearchEnabled"];
        this.ThinkEnabled = source["ThinkEnabled"];
        this.ThinkLevel = source["ThinkLevel"];
        this.SelectedModel = source["SelectedModel"];
        this.SidebarOpen = source["SidebarOpen"];
        this.LastHomeView = source["LastHomeView"];
        this.AutoUpdateEnabled = source["AutoUpdateEnabled"];
    }
}
/** 设置读写 API 的响应包装。 */
export class SettingsResponse {
    settings: Settings;

    constructor(source: any = {}) {
        if ('string' === typeof source) source = JSON.parse(source);
        this.settings = this.convertValues(source["settings"], Settings);
    }

	convertValues(a: any, classs: any, asMap: boolean = false): any {
	    if (!a) {
	        return a;
	    }
	    if (Array.isArray(a)) {
	        return (a as any[]).map(elem => this.convertValues(elem, classs));
	    } else if ("object" === typeof a) {
	        if (asMap) {
	            for (const key of Object.keys(a)) {
	                a[key] = new classs(a[key]);
	            }
	            return a;
	        }
	        return new classs(a);
	    }
	    return a;
	}
}
/** 健康检查布尔结果。 */
export class HealthResponse {
    healthy: boolean;

    constructor(source: any = {}) {
        if ('string' === typeof source) source = JSON.parse(source);
        this.healthy = source["healthy"];
    }
}
/** 已登录 Ollama 账户用户信息。 */
export class User {
    id: string;
    email: string;
    name: string;
    bio?: string;
    avatarurl?: string;
    firstname?: string;
    lastname?: string;
    plan?: string;

    constructor(source: any = {}) {
        if ('string' === typeof source) source = JSON.parse(source);
        this.id = source["id"];
        this.email = source["email"];
        this.name = source["name"];
        this.bio = source["bio"];
        this.avatarurl = source["avatarurl"];
        this.firstname = source["firstname"];
        this.lastname = source["lastname"];
        this.plan = source["plan"];
    }
}
/** 发送消息时的附件（文件名与 base64 数据）。 */
export class Attachment {
    filename: string;
    data?: string;

    constructor(source: any = {}) {
        if ('string' === typeof source) source = JSON.parse(source);
        this.filename = source["filename"];
        this.data = source["data"];
    }
}
/** 向聊天端点发送的用户提示与选项。 */
export class ChatRequest {
    model: string;
    prompt: string;
    index?: number;
    attachments?: Attachment[];
    web_search?: boolean;
    file_tools?: boolean;
    forceUpdate?: boolean;
    think?: any;

    constructor(source: any = {}) {
        if ('string' === typeof source) source = JSON.parse(source);
        this.model = source["model"];
        this.prompt = source["prompt"];
        this.index = source["index"];
        this.attachments = this.convertValues(source["attachments"], Attachment);
        this.web_search = source["web_search"];
        this.file_tools = source["file_tools"];
        this.forceUpdate = source["forceUpdate"];
        this.think = source["think"];
    }

	convertValues(a: any, classs: any, asMap: boolean = false): any {
	    if (!a) {
	        return a;
	    }
	    if (Array.isArray(a)) {
	        return (a as any[]).map(elem => this.convertValues(elem, classs));
	    } else if ("object" === typeof a) {
	        if (asMap) {
	            for (const key of Object.keys(a)) {
	                a[key] = new classs(a[key]);
	            }
	            return a;
	        }
	        return new classs(a);
	    }
	    return a;
	}
}
/** 通用错误消息包装。 */
export class Error {
    error: string;

    constructor(source: any = {}) {
        if ('string' === typeof source) source = JSON.parse(source);
        this.error = source["error"];
    }
}
/** 模型上游版本是否过期的探测结果。 */
export class ModelUpstreamResponse {
    stale: boolean;
    error?: string;

    constructor(source: any = {}) {
        if ('string' === typeof source) source = JSON.parse(source);
        this.stale = source["stale"];
        this.error = source["error"];
    }
}
/** 浏览器工具抓取的网页快照。 */
export class Page {
    url: string;
    title: string;
    text: string;
    lines: string[];
    links?: Record<number, string>;
    fetched_at: Time;

    constructor(source: any = {}) {
        if ('string' === typeof source) source = JSON.parse(source);
        this.url = source["url"];
        this.title = source["title"];
        this.text = source["text"];
        this.lines = source["lines"];
        this.links = source["links"];
        this.fetched_at = this.convertValues(source["fetched_at"], Time);
    }

	convertValues(a: any, classs: any, asMap: boolean = false): any {
	    if (!a) {
	        return a;
	    }
	    if (Array.isArray(a)) {
	        return (a as any[]).map(elem => this.convertValues(elem, classs));
	    } else if ("object" === typeof a) {
	        if (asMap) {
	            for (const key of Object.keys(a)) {
	                a[key] = new classs(a[key]);
	            }
	            return a;
	        }
	        return new classs(a);
	    }
	    return a;
	}
}
/** Agent 浏览器工具的页面栈与 token 视图状态。 */
export class BrowserStateData {
    page_stack: string[];
    view_tokens: number;
    url_to_page: {[key: string]: Page};

    constructor(source: any = {}) {
        if ('string' === typeof source) source = JSON.parse(source);
        this.page_stack = source["page_stack"];
        this.view_tokens = source["view_tokens"];
        this.url_to_page = source["url_to_page"];
    }
}
