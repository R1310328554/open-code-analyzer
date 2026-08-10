package org.keycloak.db.compatibility.verifier;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * Liquibase {@code jpa-changelog*.xml} 变更日志解析器。
 * <p>
 * 在类路径 {@code META-INF} 下扫描所有 JPA 变更日志 XML，提取 {@link ChangeSet} 的
 * {@code id}、{@code author} 与来源文件名，供 db-compatibility-verifier 插件做滚动升级兼容性校验。
 */
record ChangeLogXMLParser(ClassLoader classLoader) {

   /** 类路径上存放变更日志 XML 的资源目录。 */
   static final String RESOURCE_DIR = "META-INF";

   /**
    * 发现并汇总类路径上全部 {@link ChangeSet}，并校验无重复项。
    *
    * @return 去重后的变更集集合
    * @throws IOException 扫描类路径资源失败时
    * @throws IllegalStateException 检测到重复 {@link ChangeSet} 时
    */
   Set<ChangeSet> discoverAllChangeSets() throws IOException {
      var changeSets = changeSetXmlFiles()
            .map(this::extractChangeSets)
            .flatMap(List::stream)
            .toList();

      Set<ChangeSet> uniqueSets = new HashSet<>(changeSets.size());
      ChangeSet duplicate = changeSets.stream()
            .filter(item -> !uniqueSets.add(item))
            .findFirst()
            .orElse(null);
      if (duplicate != null) {
         throw new IllegalStateException("Duplicate ChangeSet detected: " + duplicate);
      }
      return uniqueSets;
   }

   /**
    * 从单个变更日志 XML 流式解析出所有 {@code changeSet} 元素。
    *
    * @param filename 类路径上的 XML 资源路径
    * @return 该文件中声明的变更集列表
    */
   List<ChangeSet> extractChangeSets(String filename) {
      XMLInputFactory factory = XMLInputFactory.newInstance();
      // 安全：禁用 DTD，防止 XML 外部实体（XXE）攻击
      factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
      factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
      List<ChangeSet> ids = new ArrayList<>();

      try (InputStream is = classLoader.getResourceAsStream(filename)) {
         XMLStreamReader reader = factory.createXMLStreamReader(is);
         while (reader.hasNext()) {
            int event = reader.next();

            if (event == XMLStreamConstants.START_ELEMENT) {
               String tagName = reader.getLocalName();

               // 1. 跳过根元素 databaseChangeLog
               if (tagName.equals("databaseChangeLog")) {
                  continue;
               }

               // 2. 处理 changeSet 节点，读取 id 与 author 属性
               if (tagName.equals("changeSet")) {
                  String id = reader.getAttributeValue(null, "id");
                  String author = reader.getAttributeValue(null, "author");
                  ids.add(new ChangeSet(id, author, filename));

                  // 跳过 changeSet 内所有子元素直至闭合标签
                  skipUnknownElement(reader);
                  continue;
               }
               // 3. 忽略其余未知元素
               skipUnknownElement(reader);
            }
         }
         return ids;
      } catch (IOException | XMLStreamException e) {
         throw new IllegalStateException(e);
      }
   }

   /** 在类路径上枚举所有 {@code jpa-changelog*.xml} 文件的完整资源路径。 */
   private Stream<String> changeSetXmlFiles() throws IOException {
      List<String> fileNames = new ArrayList<>();
      Enumeration<URL> en = classLoader.getResources(RESOURCE_DIR);

      while (en.hasMoreElements()) {
         URI uri;
         try {
            uri = en.nextElement().toURI();
         } catch (URISyntaxException e) {
            // 正常不应发生
            throw new IllegalStateException(e);
         }

         if (uri.getScheme().equals("jar")) {
            // JAR 包内资源（Maven 构建产物）
            try (FileSystem fs = FileSystems.newFileSystem(uri, Collections.emptyMap())) {
               Path path = fs.getPath(RESOURCE_DIR);
               fileNames.addAll(listFromPath(path));
            }
         } else {
            // 本地文件系统（IDE 开发环境）
            fileNames.addAll(listFromPath(Paths.get(uri)));
         }
      }
      return fileNames.stream()
            .filter(s -> s.startsWith("jpa-changelog") && s.endsWith(".xml"))
            .map(s -> "%s/%s".formatted(RESOURCE_DIR, s));
   }

   /** 列出指定目录下一层深度的常规文件名。 */
   private List<String> listFromPath(Path path) throws IOException {
      try (Stream<Path> walk = Files.walk(path, 1)) {
         return walk.filter(Files::isRegularFile)
               .map(p -> p.getFileName().toString())
               .toList();
      }
   }

   /** 跳过当前 START_ELEMENT 对应的整棵子树，直至匹配 END_ELEMENT。 */
   private static void skipUnknownElement(XMLStreamReader reader) throws XMLStreamException {
      int level = 1;
      while (level > 0 && reader.hasNext()) {
         int event = reader.next();
         if (event == XMLStreamConstants.START_ELEMENT) {
            level++;
         } else if (event == XMLStreamConstants.END_ELEMENT) {
            level--;
         }
      }
   }
}
