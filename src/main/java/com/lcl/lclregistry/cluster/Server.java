package com.lcl.lclregistry.cluster;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 服务实例
 * @Author conglongli
 * @date 2024/4/22 21:28
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"url"})
public class Server {
    private String url;
    private boolean status;
    private boolean leader;
    private long version;
}
