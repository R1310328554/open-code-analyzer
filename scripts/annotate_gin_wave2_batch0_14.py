#!/usr/bin/env python3
"""Chinese-annotate gin 1.12.0 wave2 batch files [0:14]."""
from __future__ import annotations

import json
import re
import sys
from pathlib import Path

ROOT = Path("/workspace")
sys.path.insert(0, str(ROOT / "framework"))

from oca.annotate.translator import ZhTranslator, _mostly_chinese  # noqa: E402
from oca.annotate.zh_localize import translate_text  # noqa: E402

VER = ROOT / "gin/1.12.0"
ANALYZED = VER / "analyzed"
BATCH_FILES = json.loads((VER / "_reports/class-queue/batch.json").read_text())["files"][:14]

COPYRIGHT_MARKERS = (
    "Copyright",
    "Use of this source code",
    "license that can be found",
    "Based on the path package",
    "BSD-style license",
)

SKIP_BODY_PATTERNS = (
    r"^#nosec\b",
    r"^nolint\b",
    r"^\+build\b",
    r"^go:build\b",
    r"^https?://",
    r"^package main$",
    r"^import ",
    r"^\t",
    r"^    ",
)


def _should_skip_body(body: str) -> bool:
    raw = body.strip()
    if not raw:
        return True
    if _mostly_chinese(raw):
        return True
    if not re.search(r"[A-Za-z]{3,}", raw):
        return True
    for pat in SKIP_BODY_PATTERNS:
        if re.match(pat, raw):
            return True
    return False


def _is_copyright_line(body: str) -> bool:
    raw = body.strip()
    return any(m in raw for m in COPYRIGHT_MARKERS)


def _translate_block(lines: list[tuple[str, str]], tr: ZhTranslator) -> list[str]:
    """Translate a godoc block; lines are (indent, body)."""
    if all(_should_skip_body(body) or _is_copyright_line(body) for _, body in lines):
        return [f"{indent}// {body}" if body else f"{indent}//" for indent, body in lines]

    # Preserve code-example lines (indented in godoc)
    parts: list[str] = []
    code_lines: list[tuple[int, str, str]] = []
    for i, (indent, body) in enumerate(lines):
        if body.startswith("\t") or body.startswith("    ") or body.startswith("router."):
            code_lines.append((i, indent, body))
        else:
            parts.append(body)
    text = "\n".join(p for p in parts if p.strip())
    if not text.strip() or _should_skip_body(text):
        return [f"{indent}// {body}" if body else f"{indent}//" for indent, body in lines]

    zh = translate_text(tr, text)
    zh_lines = zh.split("\n")
    out: list[str | None] = [None] * len(lines)
    for idx, indent, body in code_lines:
        out[idx] = f"{indent}// {body}" if body else f"{indent}//"
    j = 0
    for i, (indent, body) in enumerate(lines):
        if out[i] is not None:
            continue
        if not body.strip():
            out[i] = f"{indent}//"
            continue
        if j < len(zh_lines):
            out[i] = f"{indent}// {zh_lines[j]}"
            j += 1
        else:
            out[i] = f"{indent}// {body}"
    # distribute remaining zh lines to last non-code slot if multiline mismatch
    while j < len(zh_lines):
        for i in range(len(out) - 1, -1, -1):
            if out[i] is not None and not lines[i][1].startswith("\t"):
                base = out[i] or ""
                out[i] = base + " " + zh_lines[j]
                j += 1
                break
        else:
            break
    return [x if x is not None else f"{lines[i][0]}// {lines[i][1]}" for i, x in enumerate(out)]


def localize_go_source(text: str, tr: ZhTranslator) -> tuple[str, int]:
    src_lines = text.splitlines()
    out: list[str] = []
    i = 0
    count = 0
    while i < len(src_lines):
        line = src_lines[i]
        m = re.match(r"^(\s*)//(.*)$", line)
        if not m:
            out.append(line)
            i += 1
            continue

        indent, body = m.group(1), m.group(2)
        if body.startswith("/"):
            out.append(line)
            i += 1
            continue

        if _is_copyright_line(body):
            out.append(line)
            i += 1
            continue

        # collect consecutive // comments
        block: list[tuple[str, str]] = [(indent, body)]
        j = i + 1
        while j < len(src_lines):
            m2 = re.match(r"^(\s*)//(.*)$", src_lines[j])
            if not m2 or _is_copyright_line(m2.group(2)):
                break
            block.append((m2.group(1), m2.group(2)))
            j += 1

        if len(block) == 1 and not _should_skip_body(body):
            zh = translate_text(tr, body.strip())
            if zh != body.strip():
                count += 1
            out.append(f"{indent}// {zh}")
        else:
            translated = _translate_block(block, tr)
            if translated != [f"{ind}// {b}" if b else f"{ind}//" for ind, b in block]:
                count += len(block)
            out.extend(translated)
        i = j
    return "\n".join(out) + ("\n" if text.endswith("\n") else ""), count


def main() -> int:
    cache = VER / "_reports/translate-cache.json"
    tr = ZhTranslator(cache)
    stats = {"files": 0, "comments": 0, "errors": []}

    for rel in BATCH_FILES:
        path = ANALYZED / rel
        try:
            original = path.read_text(encoding="utf-8")
            updated, n = localize_go_source(original, tr)
            if updated != original:
                path.write_text(updated, encoding="utf-8")
                stats["files"] += 1
                stats["comments"] += n
                print(f"[gin-zh] {rel}: {n} comments", flush=True)
        except Exception as ex:
            stats["errors"].append(f"{rel}: {ex}")
            print(f"[gin-zh] error {rel}: {ex}", flush=True)

    tr.flush()
    print(json.dumps(stats, ensure_ascii=False))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
