package cn.lycodeing.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author lycodeing
 */
@Getter
@AllArgsConstructor
public enum ChallengeTypeEnum {

    DNS("dns-01"),
    HTTP("http-01");

    private final String type;
}
