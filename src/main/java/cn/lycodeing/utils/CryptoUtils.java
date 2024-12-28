package cn.lycodeing.utils;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.Attribute;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.util.Base64;

/**
 * @author lycodeing
 */
public class CryptoUtils {


    static {
        Security.addProvider(new BouncyCastleProvider());
    }


    /**
     * 生成密钥对
     */
    public static KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        return generator.generateKeyPair();
    }


    /**
     * sha256+base64
     */
    public static String hash256AndEncodeToBase64(String input) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return base64UrlEncode(hash);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 生成CSR
     *
     * @param domain 域名
     * @return
     * @throws Exception
     */
    public static String generateCsr(String domain, KeyPair keyPair) throws Exception {
        // 构建主题信息
        X500Name subject = new X500Name(String.format("CN=%s, OU=MyUnit, O=MyOrg, L=City, ST=State, C=US", domain));

        // 创建CSR请求
        PKCS10CertificationRequest csr = createCsr(subject , keyPair);

        return base64UrlEncode(csr.getEncoded());
    }


    public static String base64UrlEncode(byte[] data) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(data);
    }


    public static String base64UrlEncode(String data) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(data.getBytes(StandardCharsets.UTF_8));
    }


    private static PKCS10CertificationRequest createCsr(X500Name subject,KeyPair keyPair) throws Exception {

        JcaPKCS10CertificationRequestBuilder csrBuilder = new JcaPKCS10CertificationRequestBuilder(subject, keyPair.getPublic());

        ContentSigner contentSigner = new JcaContentSignerBuilder("SHA256WithRSAEncryption").build(keyPair.getPrivate());


        return csrBuilder.build(contentSigner);
    }
}
