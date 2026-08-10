package org.keycloak.admin.client.spi;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyReader;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.ObjectCodec;
import org.jboss.resteasy.plugins.providers.sse.EventInput;
import org.jboss.resteasy.specimpl.ResponseBuilderImpl;

/**
 * 提供 {@link Stream} 的增量式 JSON 数组反序列化能力，仅适用于 RESTEasy Classic。
 * <p>
 * 通过 Jackson 逐元素解析响应体，避免一次性加载大型数组；返回的 Stream 同时标记为
 * {@link EventInput}，以防止 RESTEasy 过早关闭底层输入流。
 */
public class StreamMessageBodyReader implements MessageBodyReader<Stream<?>> {

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return type == Stream.class;
    }

    @Override
    public Stream<?> readFrom(Class<Stream<?>> type, Type genericType, Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
            throws IOException, WebApplicationException {

        // 获取 ObjectCodec 的变通方案——理想情况下应直接使用 Mapper，但此处无法可靠推断其来源
        ObjectCodec codec = new ResponseBuilderImpl()
                .entity(new ByteArrayInputStream("[]".getBytes(StandardCharsets.UTF_8))).status(200).type(mediaType)
                .build().readEntity(JsonParser.class).getCodec();

        JsonParser parser = codec.getFactory().createParser(entityStream);
        JsonToken token = parser.nextToken();
        if (token == null) {
            return null;
        }
        if (token != JsonToken.START_ARRAY) {
            entityStream.close();
            throw new IOException("Expected Array");
        }
        token = parser.nextToken();
        if (token == JsonToken.END_ARRAY) {
            return Stream.of();
        }
        if (token != JsonToken.START_OBJECT) {
            entityStream.close();
            throw new IOException("Expected Object");
        }

        Class<?> elementType = Object.class;
        if (genericType instanceof ParameterizedType) {
            Type[] typeArguments = ((ParameterizedType) genericType).getActualTypeArguments();
            if (typeArguments.length > 0) {
                if (typeArguments[0] instanceof WildcardType) {
                    typeArguments = ((WildcardType)typeArguments[0]).getUpperBounds();
                }
                if (typeArguments.length > 0 && typeArguments[0] instanceof Class<?>) {
                    elementType = (Class<?>) typeArguments[0];
                }
            }
        }

        Iterator<?> iter = codec.readValues(parser, elementType);
        Stream<?> targetStream = StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(iter, Spliterator.ORDERED),
                false).onClose(() -> {
            try {
                entityStream.close();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
        // 返回同时实现 EventInput 的 Stream 代理，防止 RESTEasy 提前关闭结果流
        return (Stream<?>) Proxy.newProxyInstance(EventInput.class.getClassLoader(), new Class<?>[] {Stream.class, EventInput.class}, (proxy, method, args) -> {
            try {
                return method.invoke(targetStream, args);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        });
    }

}
