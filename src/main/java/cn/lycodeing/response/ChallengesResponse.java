package cn.lycodeing.response;

import cn.lycodeing.enums.ChallengeTypeEnum;
import cn.lycodeing.request.Identifier;
import lombok.Data;

import java.util.List;

import static cn.lycodeing.utils.CryptoUtils.hash256AndEncodeToBase64;

/**
 * @author lycodeing
 */
@Data
public class ChallengesResponse {
    private String status;

    private String expires;

    private List<Challenge> challenges;

    private Identifier identifier;

}
