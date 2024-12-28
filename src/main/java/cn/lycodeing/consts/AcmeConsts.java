package cn.lycodeing.consts;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class AcmeConsts {

    public static final String NONCE = "Replay-Nonce";


    public static final String NEW_NONCE = "new-nonce";


    public static final String LOCATION = "Location";

    public static final Map<String,String> ACME_HEADER = Collections.singletonMap("Content-Type", "application/jose+json");
}
