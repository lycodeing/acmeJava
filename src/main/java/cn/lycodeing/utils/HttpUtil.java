package cn.lycodeing.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author lycodeing
 */
public class HttpUtil {

    private static final CloseableHttpClient HTTP_CLIENT = HttpClients.createDefault();

    /**
     * 发送HTTP GET请求并返回响应内容和响应头
     *
     * @param url 请求URL
     * @return 包含响应内容和响应头的Response对象
     * @throws IOException 如果发生I/O错误
     */
    public static Response sendHttpGet(String url) throws IOException {
        if (url == null || url.isEmpty()) {
            throw new IllegalArgumentException("URL不能为空");
        }

        HttpGet request = new HttpGet(url);
        try (CloseableHttpResponse response = HTTP_CLIENT.execute(request)) {
            String content = null;
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                try {
                    content = EntityUtils.toString(entity, StandardCharsets.UTF_8);
                } catch (IOException e) {
                    // 确保即使在EntityUtils.toString抛出异常时，response对象也能被正确关闭
                    throw new IOException("读取响应内容失败", e);
                }
            }
            Map<String, String> headers = getHeadersFromResponse(response);
            return new Response(content, headers);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("无效的URL: " + url, e);
        } catch (Exception e) {
            throw new IOException("发送HTTP GET请求失败", e);
        }
    }

    /**
     * 发送HTTP POST请求并返回响应内容和响应头
     *
     * @param url  请求URL
     * @param body 请求体内容
     * @return 包含响应内容和响应头的Response对象
     * @throws IOException 如果发生I/O错误
     */
    public static Response sendHttpPost(String url, Object body, Map<String, String> headers) throws IOException {
        HttpPost request = new HttpPost(url);
        StringEntity entity = new StringEntity(GsonUtils.toJson(body), StandardCharsets.UTF_8);
        request.setEntity(entity);
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            request.setHeader(entry.getKey(), entry.getValue());
        }

        try (CloseableHttpResponse response = HTTP_CLIENT.execute(request)) {
            String content = null;
            HttpEntity responseEntity = response.getEntity();
            if (responseEntity != null) {
                content = EntityUtils.toString(responseEntity, StandardCharsets.UTF_8);
            }
            Map<String, String> responseHeaders = getHeadersFromResponse(response);
            return new Response(content, responseHeaders);
        }
    }

    /**
     * 发送HTTP HEAD请求并返回响应头
     *
     * @param url 请求URL
     * @return 响应头信息
     * @throws IOException 如果发生I/O错误
     */
    public static Response sendHttpHead(String url) throws IOException {
        HttpHead request = new HttpHead(url);
        try (CloseableHttpResponse response = HTTP_CLIENT.execute(request)) {
            return new Response(null, getHeadersFromResponse(response));
        }
    }

    /**
     * 从响应中提取响应头信息
     *
     * @param response 响应对象
     * @return 响应头信息的Map
     */
    private static Map<String, String> getHeadersFromResponse(CloseableHttpResponse response) {
        Map<String, String> headers = new HashMap<>();
        Arrays.stream(response.getAllHeaders()).forEach(header -> headers.put(header.getName(), header.getValue()));
        return headers;
    }

    /**
     * 响应对象，包含响应内容和响应头
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Response {
        private String content;
        private Map<String, String> headers;


        public String getHeader(String name) {
            return headers.get(name);
        }

        public <T> T getContent(Class<T> clazz) {
            return GsonUtils.fromJson(content, clazz);
        }
    }
}



