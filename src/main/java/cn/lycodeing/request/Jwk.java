package cn.lycodeing.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * Jwk
 *
 * @author lycodeing
 */
@Data
@Builder
@AllArgsConstructor
public class Jwk {

    private String e;

    private String kty;

    private String n;
}
