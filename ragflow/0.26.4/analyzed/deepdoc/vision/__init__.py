#
#  Copyright 2025 The InfiniFlow Authors. All Rights Reserved.
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
#
"""
DeepDoc 视觉子包：OCR、版面识别、表格结构识别及 CLI 输入输出工具。
"""


import io
import sys
import threading

import pdfplumber

from .ocr import OCR
from .recognizer import Recognizer
from .layout_recognizer import AscendLayoutRecognizer
from .layout_recognizer import LayoutRecognizer4YOLOv10 as LayoutRecognizer
from .table_structure_recognizer import TableStructureRecognizer

# pdfplumber 全局锁，避免多线程并发打开 PDF
LOCK_KEY_pdfplumber = "global_shared_lock_pdfplumber"
if LOCK_KEY_pdfplumber not in sys.modules:
    sys.modules[LOCK_KEY_pdfplumber] = threading.Lock()


def init_in_out(args):
    # 从目录或单文件加载 PDF/图像，返回 PIL 列表与输出路径
    import os
    import traceback

    from PIL import Image

    from common.file_utils import traversal_files

    images = []
    outputs = []

    if not os.path.exists(args.output_dir):
        os.mkdir(args.output_dir)

    def pdf_pages(fnm, zoomin=3):
        # PDF 每页渲染为 RGB 图像，zoomin 控制分辨率
        nonlocal outputs, images
        with sys.modules[LOCK_KEY_pdfplumber]:
            pdf = pdfplumber.open(fnm)
            images = [p.to_image(resolution=72 * zoomin).annotated for i, p in enumerate(pdf.pages)]

        for i, page in enumerate(images):
            outputs.append(os.path.split(fnm)[-1] + f"_{i}.jpg")
        pdf.close()

    def images_and_outputs(fnm):
        nonlocal outputs, images
        if fnm.split(".")[-1].lower() == "pdf":
            pdf_pages(fnm)
            return
        try:
            with open(fnm, "rb") as fp:
                binary = fp.read()
            images.append(Image.open(io.BytesIO(binary)).convert("RGB"))
            outputs.append(os.path.split(fnm)[-1])
        except Exception:
            traceback.print_exc()

    if os.path.isdir(args.inputs):
        for fnm in traversal_files(args.inputs):
            images_and_outputs(fnm)
    else:
        images_and_outputs(args.inputs)

    for i in range(len(outputs)):
        outputs[i] = os.path.join(args.output_dir, outputs[i])

    return images, outputs


# 对外导出的识别器与工具函数
__all__ = [
    "OCR",
    "Recognizer",
    "LayoutRecognizer",
    "AscendLayoutRecognizer",
    "TableStructureRecognizer",
    "init_in_out",
]
