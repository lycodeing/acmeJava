package cn.lycodeing;

import cn.lycodeing.consts.AcmeConsts;
import cn.lycodeing.enums.ChallengeStatusEnum;
import cn.lycodeing.enums.ChallengeTypeEnum;
import cn.lycodeing.request.*;
import cn.lycodeing.response.Challenge;
import cn.lycodeing.response.ChallengesResponse;
import cn.lycodeing.response.OrderResponse;
import cn.lycodeing.utils.*;
import lombok.Getter;
import lombok.Setter;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import static cn.lycodeing.utils.AcmeUtils.generateThumbprint;

/**
 * AcmeClient
 * <p>
 * Handles ACME protocol operations, such as account creation, order creation, challenge validation, and certificate retrieval.
 * </p>
 *
 * @author lycodeing
 */
@Setter
@Getter
public class AcmeClient {
    private static final Logger logger = LogManager.getLogger(AcmeClient.class);
    private static final String DIRECTORY_URL = "https://acme-staging-v02.api.letsencrypt.org/directory";

    private final CloseableHttpClient httpClient = HttpClients.createDefault();
    // ACME directory data (URLs)
    private Directory directory;
    // Account key pair
    private KeyPair keyPair;
    // Domain key pair for CSR
    private KeyPair domainKeyPair;
    // JWK for account key pair
    private Jwk jwk;
    // Key Identifier (Account ID)
    private String kid;
    // Thumbprint for account
    private String thumbprint;
    // Authorization URLs
    private List<String> authorizations;
    // Finalize URL for order
    private String finalizeOrderUrl;
    // Retrieved certificate
    private String certificate;

    public AcmeClient() {
        try {
            initDirectories();
        } catch (Exception e) {
            logger.error("Failed to initialize directories", e);
        }
    }

    // ========================== Account Management ==========================

    /**
     * 创建ACME账号
     */
    public void createAccount(String email) throws Exception {
        logger.info("Creating ACME account, email:{}", email);
        keyPair = CryptoUtils.generateKeyPair();
        jwk = AcmeUtils.publicKeyToJwk(keyPair.getPublic());
        Payload payload = new Payload(email, true);
        ProtectedHeader protectedHeader = ProtectedHeader.builder()
                .alg("RS256")
                .jwk(jwk)
                .kid(kid)
                .nonce(getNonce())
                .url(directory.getNewAccount())
                .build();
        String payloadBase64 = payload.base64UrlEncode();
        String protectedHeaderBase64 = protectedHeader.base64UrlEncode();
        Jws jws = AcmeUtils.generateSignatureJws(keyPair.getPrivate(), protectedHeaderBase64, payloadBase64);
        HttpUtil.Response response = HttpUtil.sendHttpPost(directory.getNewAccount(), jws, AcmeConsts.ACME_HEADER);
        this.kid = response.getHeader(AcmeConsts.LOCATION);
    }

    // ========================== Order Management ==========================

    /**
     * 创建新的订单
     *
     * @param domains List of domain names to be included in the order
     */
    public void newOrder(List<String> domains) throws Exception {
        logger.info("Creating new order, domains: {}", domains);
        List<Identifier> identifiers = domains.stream()
                .map(domain -> new Identifier("dns", domain))
                .collect(Collectors.toList());

        Payload payload = new Payload(identifiers);
        ProtectedHeader protectedHeader = ProtectedHeader.builder()
                .alg("RS256")
                .kid(kid)
                .nonce(getNonce())
                .url(directory.getNewOrder())
                .build();

        String payloadBase64 = payload.base64UrlEncode();
        String protectedHeaderBase64 = protectedHeader.base64UrlEncode();
        Jws jws = AcmeUtils.generateSignatureJws(keyPair.getPrivate(), protectedHeaderBase64, payloadBase64);
        HttpUtil.Response response = HttpUtil.sendHttpPost(directory.getNewOrder(), jws, AcmeConsts.ACME_HEADER);
        OrderResponse orderResponse = response.getContent(OrderResponse.class);
        this.finalizeOrderUrl = orderResponse.getFinalize();
        this.authorizations = orderResponse.getAuthorizations();
    }

    // ========================== Challenge Validation ==========================

    /**
     * 验证订单挑战
     */
    public void validateOrder() throws IOException {
        for (String authorization : this.getAuthorizations()) {
            HttpUtil.Response response = HttpUtil.sendHttpGet(authorization);
            ChallengesResponse challengesResponse = response.getContent(ChallengesResponse.class);

            for (Challenge challenge : challengesResponse.getChallenges()) {
                if (ChallengeTypeEnum.DNS.getType().equals(challenge.getType())) {
                    handleDnsChallenge(challenge, challengesResponse);
                }
            }
            logger.info("Challenge validation completed");
        }
    }

    /**
     * 处理 DNS-01 挑战
     */
    private void handleDnsChallenge(Challenge challenge, ChallengesResponse challengesResponse) {
        try {
            logger.info("Validating DNS-01 challenge for domain: {}" , challengesResponse.getIdentifier().getValue());
            logger.info("Challenge URL: {}", challenge.getUrl());
            logger.info("DNS TXT Record Value: {}", challenge.getDigest(generateThumbprint(jwk)));
            logger.info("请绑定该dns记录");
            // Wait for DNS propagation
            Thread.sleep(1000 * 20);
            validateChallenges(challenge.getUrl());
        } catch (Exception e) {
            throw new RuntimeException("DNS Challenge validation failed", e);
        }
    }

    /**
     * 递归验证挑战状态
     */
    public void validateChallenges(String url) {
        try {
            Payload payload = new Payload();
            String payloadBase64 = payload.base64UrlEncode();
            ProtectedHeader protectedHeader = ProtectedHeader.builder()
                    .alg("RS256")
                    .kid(kid)
                    .nonce(getNonce())
                    .url(url)
                    .build();
            String protectedHeaderBase64 = protectedHeader.base64UrlEncode();
            Jws jws = AcmeUtils.generateSignatureJws(keyPair.getPrivate(), protectedHeaderBase64, payloadBase64);
            HttpUtil.Response response = HttpUtil.sendHttpPost(url, jws, AcmeConsts.ACME_HEADER);
            Challenge content = response.getContent(Challenge.class);
            if (ChallengeStatusEnum.VALID.getStatus().equalsIgnoreCase(content.getStatus())) {
                logger.info("Challenge validation successful");
                return;
            }
            if (ChallengeStatusEnum.INVALID.getStatus().equalsIgnoreCase(content.getStatus())) {
                throw new RuntimeException("Challenge validation failed");
            }
            logger.info("Challenge validation pending ..........");
            AcmeUtils.sleep(3);
            validateChallenges(url);
        } catch (Exception e) {
            throw new RuntimeException("Challenge validation error", e);
        }
    }

    // ========================== Finalizing Order ==========================

    /**
     * 完成订单并生成证书请求
     */
    public void finalizeOrder(String finalizeUrl) throws Exception {
        domainKeyPair = CryptoUtils.generateKeyPair();
        String csr = CryptoUtils.generateCsr("oll.lycodeing.cn", domainKeyPair);
        Payload payload = new Payload(csr);
        String payloadBase64Encoded = payload.base64UrlEncode();
        ProtectedHeader protectedHeader = ProtectedHeader.builder()
                .alg("RS256")
                .kid(kid)
                .nonce(getNonce())
                .url(finalizeUrl)
                .build();
        String protectedHeaderBase64 = protectedHeader.base64UrlEncode();
        Jws jws = AcmeUtils.generateSignatureJws(keyPair.getPrivate(), protectedHeaderBase64, payloadBase64Encoded);
        finalizeOrder(finalizeUrl, jws);
    }

    /**
     * 完成订单并获取 finalize URL
     */
    private void finalizeOrder(String url, Jws jws) throws IOException {
        HttpUtil.Response response = HttpUtil.sendHttpPost(url, jws, AcmeConsts.ACME_HEADER);
        logger.info(GsonUtils.toJson(response.getHeader(AcmeConsts.LOCATION)));
        this.finalizeOrderUrl = response.getHeader(AcmeConsts.LOCATION);
    }

    // ========================== Certificate Retrieval ==========================

    /**
     * 获取订单状态
     */
    public void getOrder() throws IOException {
        HttpUtil.Response response = HttpUtil.sendHttpGet(finalizeOrderUrl);
        OrderResponse orderResponse = response.getContent(OrderResponse.class);
        if (ChallengeStatusEnum.VALID.getStatus().equalsIgnoreCase(orderResponse.getStatus())) {
            this.certificate = orderResponse.getCertificate();
            return;
        }
        if (ChallengeStatusEnum.INVALID.getStatus().equalsIgnoreCase(orderResponse.getStatus())) {
            throw new RuntimeException("Order validation failed");
        }
        AcmeUtils.sleep(3);
        getOrder();
    }

    /**
     * 获取证书并保存到文件
     */
    public void getCertificate() throws IOException {
        HttpUtil.Response response = HttpUtil.sendHttpGet(certificate);
        logger.info("Certificate: {}", response.getContent());
        FileUtil.writeByteArrayToFile("cert.pem", response.getContent().getBytes(StandardCharsets.UTF_8));

        savePrivateKeyToFile(domainKeyPair.getPrivate(), "private.key");
    }

    /**
     * 保存私钥到文件
     */
    private void savePrivateKeyToFile(PrivateKey privateKey, String filePath) throws IOException {
        byte[] keyBytes = privateKey.getEncoded();
        String pemEncoded = "-----BEGIN PRIVATE KEY-----\n" +
                Base64.getMimeEncoder().encodeToString(keyBytes) +
                "\n-----END PRIVATE KEY-----";
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(filePath), StandardCharsets.UTF_8)) {
            writer.write(pemEncoded);
        }
    }

    /**
     * 获取最新的 Nonce
     */
    public String getNonce() throws IOException {
        HttpUtil.Response response = HttpUtil.sendHttpHead(DIRECTORY_URL);
        return response.getHeader(AcmeConsts.NONCE);
    }

    /**
     * 初始化 ACME 目录
     */
    private void initDirectories() throws IOException {
        HttpUtil.Response response = HttpUtil.sendHttpGet(DIRECTORY_URL);
        directory = response.getContent(Directory.class);
    }
}
