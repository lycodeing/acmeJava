package cn.lycodeing.response;

import lombok.Data;

@Data
public class AuthorizationResponse {

    private String status;

    private String url;

    private String token;

    private String type;

}
