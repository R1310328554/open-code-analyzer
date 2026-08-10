"""
Markdown 提示词模板加载：按名称缓存 rag/prompts/*.md 文件内容。
"""

"""
Markdown 提示词模板加载：按名称缓存 rag/prompts/*.md 文件内容。
"""

import os

# prompts 目录绝对路径
PROMPT_DIR = os.path.dirname(__file__)

_loaded_prompts = {}


def load_prompt(name: str) -> str:
    # 加载并缓存 {name}.md 提示词正文
    if name in _loaded_prompts:
        return _loaded_prompts[name]

    path = os.path.join(PROMPT_DIR, f"{name}.md")
    if not os.path.isfile(path):
        raise FileNotFoundError(f"Prompt file '{name}.md' not found in prompts/ directory.")

    with open(path, "r", encoding="utf-8") as f:
        content = f.read().strip()
        _loaded_prompts[name] = content
        return content
