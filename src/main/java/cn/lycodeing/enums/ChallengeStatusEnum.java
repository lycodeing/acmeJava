package cn.lycodeing.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author lycodeing
 */

@Getter
@AllArgsConstructor
public enum ChallengeStatusEnum {

    PENDING("pending"),

    PROCESSING("processing"),

    VALID("valid"),

    INVALID("invalid");

    private final String status;

}
