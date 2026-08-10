#!/usr/bin/env python3
"""从 HuggingFace 下载 OSS DeepDoc 所需 ONNX 模型与字典文件。"""

import os
import sys

# HuggingFace 模型仓库 ID
REPO_ID = "InfiniFlow/deepdoc"
# 版面/检测/识别/表格结构 ONNX 及 OCR 字典
FILES = [
    "layout.onnx",
    "det.onnx",
    "rec.onnx",
    "tsr.onnx",
    "ocr.res",
]


def main():
    # 将模型下载到指定目录，已存在则跳过
    target_dir = sys.argv[1] if len(sys.argv) > 1 else "models"
    os.makedirs(target_dir, exist_ok=True)

    try:
        from huggingface_hub import hf_hub_download
    except ImportError:
        print("ERROR: huggingface_hub not installed. Run: pip install huggingface_hub")
        sys.exit(1)

    # 支持 HF 镜像（如 hf-mirror.com）
    hf_endpoint = os.environ.get("HF_ENDPOINT", "https://huggingface.co")

    for filename in FILES:
        local_path = os.path.join(target_dir, filename)
        if os.path.exists(local_path):
            print(f"  SKIP {filename} (already exists)")
            continue
        print(f"  DOWNLOAD {filename} ...")
        hf_hub_download(
            repo_id=REPO_ID,
            filename=filename,
            local_dir=target_dir,
            endpoint=hf_endpoint,
        )
        print(f"  OK {filename}")

    print(f"\nAll models downloaded to {os.path.abspath(target_dir)}")


if __name__ == "__main__":
    main()
