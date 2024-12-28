package cn.lycodeing.response;

import cn.lycodeing.enums.ChallengeTypeEnum;
import lombok.Data;

import static cn.lycodeing.utils.CryptoUtils.hash256AndEncodeToBase64;

/**
 * @author lycodeing
 */
@Data
public class Challenge {
    private String type;
    private String url;
    private String status;
    private String token;


    public String getDigest(String thumbprint){
        String digest = String.format("%s.%s", token, thumbprint);
        if(ChallengeTypeEnum.HTTP.getType().equals(type)){
            return digest;
        } else if (ChallengeTypeEnum.DNS.getType().equals(type)) {
            return hash256AndEncodeToBase64(digest);
        }
        throw new RuntimeException("不支持的challenge类型");
    }
}
