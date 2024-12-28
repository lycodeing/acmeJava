package cn.lycodeing.request;

import cn.lycodeing.utils.AcmeUtils;
import cn.lycodeing.utils.GsonUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import static cn.lycodeing.utils.AcmeUtils.makeUrlSafe;

/**
 * @author lycodeing
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProtectedHeader {

    private String alg;

    private Jwk jwk;

    private String kid;

    private String nonce;

    private String url;


    public String base64UrlEncode() {
        return makeUrlSafe(AcmeUtils.base64UrlEncode(GsonUtils.toJson(this)));
    }
}
