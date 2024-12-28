package cn.lycodeing.utils;

import cn.lycodeing.request.Jwk;
import cn.lycodeing.request.Jws;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;
import java.util.TreeMap;

import static cn.lycodeing.utils.CryptoUtils.generateKeyPair;
import static cn.lycodeing.utils.CryptoUtils.hash256AndEncodeToBase64;

/**
 * 证书工具类
 *
 * @author lycodeing
 */
public class AcmeUtils {
    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private static final String ALGORITHM = "SHA256withRSA";


    /**
     * 生成CSR
     *
     * @param domain 域名
     * @return
     * @throws Exception
     */
    public static String generateCsr(String domain) throws Exception {
        // 构建主题信息
        X500Name subject = new X500Name(String.format("CN=%s, OU=MyUnit, O=MyOrg, L=City, ST=State, C=US", domain));

        // 创建CSR请求
        PKCS10CertificationRequest csr = createCsr(subject);

        return base64UrlEncode(csr.getEncoded());
    }


    public static String base64UrlEncode(byte[] data) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(data);
    }


    public static String base64UrlEncode(String data) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(data.getBytes(StandardCharsets.UTF_8));
    }


    private static PKCS10CertificationRequest createCsr(X500Name subject) throws Exception {
        KeyPair keyPair = generateKeyPair();

        JcaPKCS10CertificationRequestBuilder csrBuilder = new JcaPKCS10CertificationRequestBuilder(subject, keyPair.getPublic());

        ContentSigner contentSigner = new JcaContentSignerBuilder("SHA256WithRSAEncryption").build(keyPair.getPrivate());


        return csrBuilder.build(contentSigner);
    }

    public static Jws generateSignatureJws(PrivateKey privateKey, String protectedHeader, String payload) throws Exception {
        // 生成 signature
        String dataToSign = protectedHeader + "." + payload;
        Signature instance = Signature.getInstance(ALGORITHM);
        instance.initSign(privateKey);
        instance.update(dataToSign.getBytes(StandardCharsets.UTF_8));
        byte[] sign = instance.sign();
        String signatureBase64 = base64UrlEncode(sign);
        return new Jws(protectedHeader, payload, signatureBase64);
    }


    public static Jwk publicKeyToJwk(PublicKey publicKey) throws Exception {
        byte[] encoded = publicKey.getEncoded();
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encoded);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        RSAPublicKey rsaPublicKey = (RSAPublicKey) keyFactory.generatePublic(keySpec);

        BigInteger modulus = rsaPublicKey.getModulus();
        BigInteger publicExponent = rsaPublicKey.getPublicExponent();

        // Remove leading zero byte if present
        byte[] modulusBytes = modulus.toByteArray();
        if (modulusBytes[0] == 0) {
            modulusBytes = Arrays.copyOfRange(modulusBytes, 1, modulusBytes.length);
        }

        byte[] exponentBytes = publicExponent.toByteArray();
        if (exponentBytes[0] == 0) {
            exponentBytes = Arrays.copyOfRange(exponentBytes, 1, exponentBytes.length);
        }

        String n = base64UrlEncode(modulusBytes);
        String e = base64UrlEncode(exponentBytes);
        return new Jwk(e, "RSA", n);
    }

    public static String generateThumbprint(Jwk jwk) throws Exception {
        // 创建Gson实例以处理JSON
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        // 将JWK对象转换为TreeMap以确保键按字母顺序排列
        Map<String, Object> sortedJwk = new TreeMap<>();
        sortedJwk.put("e", jwk.getE());
        sortedJwk.put("kty", jwk.getKty());
        sortedJwk.put("n", jwk.getN());

        // 序列化为JSON字符串并移除所有空白字符
        String jwkJson = gson.toJson(sortedJwk).replaceAll("\\s", "");

        return hash256AndEncodeToBase64(jwkJson);
    }


    public static String makeUrlSafe(String encoded) {
        return encoded.replace('+', '-').replace('/', '_').replace("=", "");
    }


    public static void sleep(long seconds) {
        try {
            Thread.sleep(1000 * seconds);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
