// CodeMirror 6 主题：PromQL 编辑器的基础/明暗样式、自动补全与语法高亮配色。

import { HighlightStyle } from "@codemirror/language";
import { EditorView } from "@codemirror/view";
import { tags } from "@lezer/highlight";

export const baseTheme = EditorView.theme({
  ".cm-content": {
    paddingTop: "3px",
    paddingBottom: "0px",
  },
  "&.cm-editor": {
    "&.cm-focused": {
      outline: "none",
      outline_fallback: "none",
    },
    backgroundColor: "transparent",
  },
  ".cm-scroller": {
    overflow: "hidden",
    fontFamily: '"DejaVu Sans Mono", monospace',
  },
  ".cm-placeholder": {
    fontFamily:
      '-apple-system,BlinkMacSystemFont,"Segoe UI",Roboto,"Helvetica Neue",Arial,"Noto Sans","Liberation Sans",sans-serif,"Apple Color Emoji","Segoe UI Emoji","Segoe UI Symbol","Noto Color Emoji"',
  },

  ".cm-matchingBracket": {
    fontWeight: "bold",
    outline: "1px dashed transparent",
  },
  ".cm-nonmatchingBracket": { borderColor: "red" },

  ".cm-tooltip.cm-tooltip-autocomplete": {
    "& > ul": {
      maxHeight: "350px",
      fontFamily: '"DejaVu Sans Mono", monospace',
      maxWidth: "unset",
    },
    "& > ul > li": {
      padding: "2px 1em 2px 3px",
    },
    minWidth: "30%",
  },

  ".cm-completionDetail": {
    float: "right",
    color: "#999",
  },

  ".cm-tooltip.cm-completionInfo": {
    padding: "10px",
    fontFamily:
      "'Open Sans', 'Lucida Sans Unicode', 'Lucida Grande', sans-serif;",
    border: "none",
    minWidth: "250px",
    maxWidth: "min-content",
  },

  ".cm-completionInfo.cm-completionInfo-right": {
    "&:before": {
      content: "' '",
      height: "0",
      position: "absolute",
      width: "0",
      left: "-20px",
      borderWidth: "10px",
      borderStyle: "solid",
      borderColor: "transparent",
    },
    marginTop: "-11px",
    marginLeft: "12px",
  },
  ".cm-completionInfo.cm-completionInfo-left": {
    "&:before": {
      content: "' '",
      height: "0",
      position: "absolute",
      width: "0",
      right: "-20px",
      borderWidth: "10px",
      borderStyle: "solid",
      borderColor: "transparent",
    },
    marginTop: "-11px",
    marginRight: "12px",
  },
  ".cm-completionInfo.cm-completionInfo-right-narrow": {
    "&:before": {
      content: "' '",
      height: "0",
      position: "absolute",
      width: "0",
      top: "-20px",
      borderWidth: "10px",
      borderStyle: "solid",
      borderColor: "transparent",
    },
    marginTop: "10px",
    marginLeft: "150px",
  },
  ".cm-completionMatchedText": {
    textDecoration: "none",
    fontWeight: "bold",
  },

  ".cm-selectionMatch": {
    backgroundColor: "#e6f3ff",
  },

  ".cm-diagnostic": {
    "&.cm-diagnostic-error": {
      borderLeft: "3px solid #e65013",
    },
  },

  ".cm-completionIcon": {
    boxSizing: "content-box",
    fontSize: "16px",
    lineHeight: "1",
    marginRight: "10px",
    verticalAlign: "top",
    "&:after": { content: "'\\ea88'" },
    fontFamily: "codicon",
    paddingRight: "0",
    opacity: "1",
  },

  ".cm-completionIcon-function, .cm-completionIcon-method": {
    "&:after": { content: "'\\ea8c'" },
  },
  ".cm-completionIcon-class": {
    "&:after": { content: "'○'" },
  },
  ".cm-completionIcon-interface": {
    "&:after": { content: "'◌'" },
  },
  ".cm-completionIcon-variable": {
    "&:after": { content: "'𝑥'" },
  },
  ".cm-completionIcon-constant": {
    "&:after": { content: "'\\eb5f'" },
  },
  ".cm-completionIcon-type": {
    "&:after": { content: "'𝑡'" },
  },
  ".cm-completionIcon-enum": {
    "&:after": { content: "'∪'" },
  },
  ".cm-completionIcon-property": {
    "&:after": { content: "'□'" },
  },
  ".cm-completionIcon-keyword": {
    "&:after": { content: "'\\eb62'" },
  },
  ".cm-completionIcon-namespace": {
    "&:after": { content: "'▢'" },
  },
  ".cm-completionIcon-text": {
    "&:after": { content: "'\\ea95'" },
    color: "#ee9d28",
  },
});

// lightTheme 在 baseTheme 之上叠加浅色背景下的选中、补全与图标颜色。
export const lightTheme = EditorView.theme(
  {
    ".cm-tooltip": {
      backgroundColor: "#f8f8f8",
      borderColor: "rgba(52, 79, 113, 0.2)",
    },

    ".cm-tooltip.cm-tooltip-autocomplete": {
      "& li:hover": {
        backgroundColor: "#ddd",
      },
      "& > ul > li[aria-selected]": {
        backgroundColor: "#d6ebff",
        color: "unset",
      },
    },

    ".cm-tooltip.cm-completionInfo": {
      backgroundColor: "#d6ebff",
    },

    ".cm-tooltip > .cm-completionInfo.cm-completionInfo-right": {
      "&:before": {
        borderRightColor: "#d6ebff",
      },
    },
    ".cm-tooltip > .cm-completionInfo.cm-completionInfo-right-narrow": {
      "&:before": {
        borderBottomColor: "#d6ebff",
      },
    },
    ".cm-tooltip > .cm-completionInfo.cm-completionInfo-left": {
      "&:before": {
        borderLeftColor: "#d6ebff",
      },
    },

    ".cm-line": {
      "&::selection": {
        backgroundColor: "#add6ff",
      },
      "& > span::selection": {
        backgroundColor: "#add6ff",
      },
    },

    ".cm-matchingBracket": {
      color: "#000",
      backgroundColor: "#dedede",
    },

    ".cm-completionMatchedText": {
      color: "#0066bf",
    },

    ".cm-completionIcon": {
      color: "#007acc",
    },

    ".cm-completionIcon-constant": {
      color: "#007acc",
    },

    ".cm-completionIcon-function, .cm-completionIcon-method": {
      color: "#652d90",
    },

    ".cm-completionIcon-keyword": {
      color: "#616161",
    },
  },
  { dark: false }
);

// darkTheme 适配 Mantine 暗色模式：caret、选中区与补全面板对比度。
export const darkTheme = EditorView.theme(
  {
    ".cm-content": {
      caretColor: "#fff",
    },

    ".cm-tooltip.cm-completionInfo": {
      backgroundColor: "#333338",
    },

    ".cm-tooltip > .cm-completionInfo.cm-completionInfo-right": {
      "&:before": {
        borderRightColor: "#333338",
      },
    },
    ".cm-tooltip > .cm-completionInfo.cm-completionInfo-right-narrow": {
      "&:before": {
        borderBottomColor: "#333338",
      },
    },
    ".cm-tooltip > .cm-completionInfo.cm-completionInfo-left": {
      "&:before": {
        borderLeftColor: "#333338",
      },
    },

    ".cm-line": {
      "&::selection": {
        backgroundColor: "#767676",
      },
      "& > span::selection": {
        backgroundColor: "#767676",
      },
    },

    ".cm-matchingBracket, &.cm-focused .cm-matchingBracket": {
      backgroundColor: "#616161",
    },

    ".cm-completionMatchedText": {
      color: "#7dd3fc",
    },

    ".cm-completionIcon, .cm-completionIcon-constant": {
      color: "#7dd3fc",
    },

    ".cm-completionIcon-function, .cm-completionIcon-method": {
      color: "#d8b4fe",
    },

    ".cm-completionIcon-keyword": {
      color: "#cbd5e1 !important",
    },
  },
  { dark: true }
);

// promqlHighlighter 为浅色主题下的 PromQL Lezer 标签着色方案。
export const promqlHighlighter = HighlightStyle.define([
  { tag: tags.number, color: "#09885a" },
  { tag: tags.string, color: "#a31515" },
  { tag: tags.keyword, color: "#008080" },
  { tag: tags.function(tags.variableName), color: "#008080" },
  { tag: tags.labelName, color: "#800000" },
  { tag: tags.operator },
  { tag: tags.modifier, color: "#008080" },
  { tag: tags.paren },
  { tag: tags.squareBracket },
  { tag: tags.brace },
  { tag: tags.invalid, color: "red" },
  { tag: tags.comment, color: "#888", fontStyle: "italic" },
]);

// darkPromqlHighlighter 在暗色背景下提高数字、字符串与标签名可读性。
export const darkPromqlHighlighter = HighlightStyle.define([
  { tag: tags.number, color: "#22c55e" },
  { tag: tags.string, color: "#fca5a5" },
  { tag: tags.keyword, color: "#14bfad" },
  { tag: tags.function(tags.variableName), color: "#14bfad" },
  { tag: tags.labelName, color: "#ff8585" },
  { tag: tags.operator },
  { tag: tags.modifier, color: "#14bfad" },
  { tag: tags.paren },
  { tag: tags.squareBracket },
  { tag: tags.brace },
  { tag: tags.invalid, color: "#ff3d3d" },
  { tag: tags.comment, color: "#9ca3af", fontStyle: "italic" },
]);
