package cn.lycodeing.request;

import cn.lycodeing.utils.AcmeUtils;
import cn.lycodeing.utils.GsonUtils;
import lombok.Data;

import java.util.List;

import static cn.lycodeing.utils.AcmeUtils.makeUrlSafe;

/**
 * @author lycodeing
 */
@Data
public class Payload {

    private String email;

    private List<String> contact;

    private Boolean termsOfServiceAgreed;


    private List<Identifier> identifiers;

    private String csr;

    public Payload() {
    }

    public Payload(String email, Boolean termsOfServiceAgreed) {
        this.email = email;
        this.termsOfServiceAgreed = termsOfServiceAgreed;
    }

    public Payload(List<String> contact, Boolean termsOfServiceAgreed) {
        this.contact = contact;
        this.termsOfServiceAgreed = termsOfServiceAgreed;
    }

    public Payload(List<Identifier> identifiers) {
        this.identifiers = identifiers;
    }

    public Payload(String csr) {
        this.csr = csr;
    }

    public String base64UrlEncode() {
        return makeUrlSafe(AcmeUtils.base64UrlEncode(GsonUtils.toJson(this)));
    }

}
