// 桌面 WebView 注入 API 与全局 Window 扩展的类型声明
// Type declarations for webview API functions

/** 原生文件选择返回的图片/文件元数据与 base64 预览。 */
interface ImageData {
  filename: string;
  path: string;
  dataURL: string; // base64 编码的文件内容
  // base64 encoded file data
}

/** 原生上下文菜单项。 */
interface MenuItem {
  label: string;
  enabled?: boolean;
  separator?: boolean;
}

/** 桌面壳层暴露的文件/目录选择 API。 */
interface WebviewAPI {
  selectFile: () => Promise<ImageData | null>;
  selectMultipleFiles: () => Promise<ImageData[] | null>;
  selectModelsDirectory: () => Promise<string | null>;
  selectWorkingDirectory: () => Promise<string | null>;
}

/** 扩展浏览器 Window 与 JSX input 属性。 */
declare global {
  interface Window {
    webview?: WebviewAPI;
    drag?: () => void;
    doubleClick?: () => void;
    menu: (items: MenuItem[]) => Promise<string | null>;
    OLLAMA_TOOLS?: boolean;
    OLLAMA_WEBSEARCH?: boolean;
  }

  namespace JSX {
    interface IntrinsicElements {
      input: React.DetailedHTMLProps<
        React.InputHTMLAttributes<HTMLInputElement> & {
          webkitdirectory?: string;
          directory?: string;
        },
        HTMLInputElement
      >;
    }
  }

  interface File {
    readonly webkitRelativePath: string;
  }
}

export type { ImageData, WebviewAPI, ContextMenuItem, ContextMenuResult };
