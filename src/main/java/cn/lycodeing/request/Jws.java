package cn.lycodeing.request;


import com.google.gson.annotations.SerializedName;
import lombok.Data;

/**
 * Jws
 *
 * @author lycodeing
 */
@Data
public class Jws {


    @SerializedName("protected")
    private String protectedHeader;

    private String payload;

    private String signature;


    public Jws(String protectedHeader, String payload, String signature) {
        this.protectedHeader = protectedHeader;
        this.payload = payload;
        this.signature = signature;
    }
}
