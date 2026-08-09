"""Chinese JavaDoc replacements for springframework wave29b XML provider interfaces."""

XML_PROVIDER_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "XmlBinaryStreamProvider.java": [
        (
            "/**\n * Interface defining handling involved with providing {@code OutputStream}\n * data for XML input.\n *\n * @author Thomas Risberg\n * @since 2.5.5\n * @see java.io.OutputStream\n * @deprecated as of 6.2, in favor of direct {@link java.sql.SQLXML} usage\n */",
            "/**\n * 通过 {@code OutputStream} 提供 XML 输入数据的回调接口。\n *\n * @author Thomas Risberg\n * @since 2.5.5\n * @see java.io.OutputStream\n * @deprecated 自 6.2 起弃用，推荐直接使用 {@link java.sql.SQLXML}\n */",
        ),
        (
            "\t/**\n\t * Implementations must implement this method to provide the XML content\n\t * for the {@code OutputStream}.\n\t * @param outputStream the {@code OutputStream} object being used to provide the XML input\n\t * @throws IOException if an I/O error occurs while providing the XML\n\t */",
            "\t/**\n\t * 实现类须向给定 {@code OutputStream} 写入 XML 内容。\n\t * @param outputStream 用于提供 XML 输入的 {@code OutputStream}\n\t * @throws IOException 写入 XML 时发生 I/O 错误\n\t */",
        ),
    ],
    "XmlCharacterStreamProvider.java": [
        (
            "/**\n * Interface defining handling involved with providing {@code Writer}\n * data for XML input.\n *\n * @author Thomas Risberg\n * @since 2.5.5\n * @see java.io.Writer\n * @deprecated as of 6.2, in favor of direct {@link java.sql.SQLXML} usage\n */",
            "/**\n * 通过 {@code Writer} 提供 XML 输入数据的回调接口。\n *\n * @author Thomas Risberg\n * @since 2.5.5\n * @see java.io.Writer\n * @deprecated 自 6.2 起弃用，推荐直接使用 {@link java.sql.SQLXML}\n */",
        ),
        (
            "\t/**\n\t * Implementations must implement this method to provide the XML content\n\t * for the {@code Writer}.\n\t * @param writer the {@code Writer} object being used to provide the XML input\n\t * @throws IOException if an I/O error occurs while providing the XML\n\t */",
            "\t/**\n\t * 实现类须向给定 {@code Writer} 写入 XML 内容。\n\t * @param writer 用于提供 XML 输入的 {@code Writer}\n\t * @throws IOException 写入 XML 时发生 I/O 错误\n\t */",
        ),
    ],
    "XmlResultProvider.java": [
        (
            "/**\n * Interface defining handling involved with providing {@code Result}\n * data for XML input.\n *\n * @author Thomas Risberg\n * @since 2.5.5\n * @see javax.xml.transform.Result\n * @deprecated as of 6.2, in favor of direct {@link java.sql.SQLXML} usage\n */",
            "/**\n * 通过 {@code Result} 提供 XML 输入数据的回调接口。\n *\n * @author Thomas Risberg\n * @since 2.5.5\n * @see javax.xml.transform.Result\n * @deprecated 自 6.2 起弃用，推荐直接使用 {@link java.sql.SQLXML}\n */",
        ),
        (
            "\t/**\n\t * Implementations must implement this method to provide the XML content\n\t * for the {@code Result}. Implementations will vary depending on\n\t * the {@code Result} implementation used.\n\t * @param result the {@code Result} object being used to provide the XML input\n\t */",
            "\t/**\n\t * 实现类须向给定 {@code Result} 写入 XML 内容；具体写法取决于所用的 {@code Result} 实现。\n\t * @param result 用于提供 XML 输入的 {@code Result}\n\t */",
        ),
    ],
}
