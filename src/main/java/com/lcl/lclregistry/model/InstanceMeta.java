package com.lcl.lclregistry.model;

import com.alibaba.fastjson.JSON;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * 服务实例元数据
 * @Author conglongli
 * @date 2024/4/21 20:08
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"schema", "host", "port", "context"})
public class InstanceMeta {
    private String schema;
    private String host;
    private int port;
    private String context;

    private boolean status;
    private Map<String, String> parameters = new HashMap<>();

    public InstanceMeta(String schema, String host, int port, String context) {
        this.schema = schema;
        this.host = host;
        this.port = port;
        this.context = context;
    }

    public String toPath() {
        return String.format("%s_%d", host, port);
    }

    public static InstanceMeta http(String host, int port) {
        return new InstanceMeta("http", host, port, "lclrpc");
    }

    public String toUrl() {
        return String.format("%s://%s:%d/%s", schema, host, port, context);
    }

    public String toMetas() {
        return JSON.toJSONString(this.getParameters());
    }

    public static InstanceMeta from(String url) {
        URI uri = URI.create(url);
        return new InstanceMeta(uri.getScheme(), uri.getHost(), uri.getPort(), uri.getPath().substring(1));
    }
}
