"""
检索查询基类：中文判定、特殊字符转义、问句归一化及英中分词间距。
"""

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
检索查询基类：中文判定、特殊字符转义、问句归一化及英中分词间距。
"""


import re
from abc import ABC, abstractmethod


class QueryBase(ABC):
    # 抽象查询构建器，子类实现 question() 生成具体检索表达式
    @staticmethod
    def is_chinese(line):
        # 按非纯英文 token 占比判断是否为中文问句
        arr = re.split(r"[ \t]+", line)
        if len(arr) <= 3:
            return True
        e = 0
        for t in arr:
            if not re.match(r"[a-zA-Z]+$", t):
                e += 1
        return e * 1.0 / len(arr) >= 0.7

    @staticmethod
    def sub_special_char(line):
        # 去除单引号并转义 Infinity/Lucene 检索特殊字符
        # Strip single quotes first to avoid Infinity's lexer treating them as string delimiters,
        # then escape remaining Infinity/Lucene special characters.
        return re.sub(r"([:\{\}/\[\]\-\*\?\"\(\)\|\+~\^])", r"\\\1", line.replace("'", "")).strip()

    @staticmethod
    def rmWWW(txt):
        # 剥离中英文疑问词与停用词，保留核心检索词
        patts = [
            (
                r"是*(怎么办|什么样的|哪家|一下|那家|请问|啥样|咋样了|什么时候|何时|何地|何人|是否|是不是|多少|哪里|怎么|哪儿|怎么样|如何|哪些|是啥|啥是|啊|吗|呢|吧|咋|什么|有没有|呀|谁|哪位|哪个)是*",
                "",
            ),
            (r"(^| )(what|who|how|which|where|why)('re|'s)? ", " "),
            (
                r"(^| )('s|'re|is|are|were|was|do|does|did|don't|doesn't|didn't|has|have|be|there|you|me|your|my|mine|just|please|may|i|should|would|wouldn't|will|won't|done|go|for|with|so|the|a|an|by|i'm|it's|he's|she's|they|they're|you're|as|by|on|in|at|up|out|down|of|to|or|and|if) ",
                " ",
            ),
        ]
        otxt = txt
        for r, p in patts:
            txt = re.sub(r, p, txt, flags=re.IGNORECASE)
        if not txt:
            txt = otxt
        return txt

    @staticmethod
    def add_space_between_eng_zh(txt):
        # 在中英文边界插入空格，便于分词器切分
        # (ENG/ENG+NUM) + ZH
        txt = re.sub(r"([A-Za-z]+[0-9]+)([\u4e00-\u9fa5]+)", r"\1 \2", txt)
        # ENG + ZH
        txt = re.sub(r"([A-Za-z])([\u4e00-\u9fa5]+)", r"\1 \2", txt)
        # ZH + (ENG/ENG+NUM)
        txt = re.sub(r"([\u4e00-\u9fa5]+)([A-Za-z]+[0-9]+)", r"\1 \2", txt)
        txt = re.sub(r"([\u4e00-\u9fa5]+)([A-Za-z])", r"\1 \2", txt)
        return txt

    @abstractmethod
    def question(self, text, tbl, min_match):
        """
        根据用户问句、表名与最小匹配度构建检索查询对象。

        Returns a query object based on the input text, table, and minimum match criteria.
        """
        raise NotImplementedError("Not implemented")
