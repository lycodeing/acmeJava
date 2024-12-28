package cn.lycodeing.response;

import cn.lycodeing.request.Identifier;
import lombok.Data;

import java.util.List;

/**
 * @author lycodeing
 */
@Data
public class OrderResponse {

    private String status;

    private String expires;

    private List<Identifier> identifiers;

    private List<String> authorizations;

    private String finalize;

    /**
     * 证书下载地址
     */
    private String certificate;
}
