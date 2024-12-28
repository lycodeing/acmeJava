package cn.lycodeing.request;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author lycodeing
 */
@Data
@AllArgsConstructor
public class Identifier {
    private String type;

    private String value;
}
