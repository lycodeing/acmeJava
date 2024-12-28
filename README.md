# AcmeClient - ACME Protocol Client

## 简介

`AcmeClient` 是一个 ACME 协议客户端，旨在简化域名证书的自动化管理，支持 Let’s Encrypt 等 CA 提供商。它实现了以下功能：
- 账户创建
- 订单创建
- 挑战验证（如 DNS-01 挑战）
- 证书获取与保存

## 功能概述

1. **账户管理**
    - 创建 ACME 账户，支持绑定邮箱并生成账户密钥对。

2. **订单管理**
    - 创建订单，包含多个域名，生成证书签名请求（CSR）。

3. **挑战验证**
    - 支持 DNS-01 挑战类型的验证。
    - 自动验证挑战状态，并在挑战完成后继续生成证书。

4. **证书获取**
    - 从 ACME 服务获取最终证书，并保存到本地文件。

## 依赖项

- **Java 17** 或更高版本
- **Apache HttpClient**（用于发送 HTTP 请求）
- **Log4j**（用于日志记录）
- **Lombok**（简化代码，自动生成 getter 和 setter）
- **Gson**（用于 JSON 处理）

## 环境配置

### 1. 初始化 ACME 目录

`AcmeClient` 在创建时会自动加载 ACME 目录 URL，作为与 ACME 服务器交互的基础。

```java
private void initDirectories() throws IOException {
    HttpUtil.Response response = HttpUtil.sendHttpGet(DIRECTORY_URL);
    directory = response.getContent(Directory.class);
}
```

### 2. 创建账户

使用 `createAccount` 方法创建 ACME 账户。需要提供一个邮箱地址作为标识。

```java
public void createAccount(String email) throws Exception {
    // 创建账户的代码逻辑
}
```

### 3. 创建订单

创建包含多个域名的证书请求订单，并获得 `finalizeOrderUrl` 和授权 URL。

```java
public void newOrder(List<String> domains) throws Exception {
    // 创建新订单
}
```

### 4. 挑战验证

ACME 协议要求在获得证书前进行挑战验证，支持 DNS-01 挑战。在 `validateOrder` 方法中，会处理挑战验证。

```java
public void validateOrder() throws IOException {
    // 验证挑战
}
```

### 5. 完成订单并获取证书

订单完成后，可以调用 `getOrder` 和 `getCertificate` 方法获取最终的证书并保存到本地文件。

```java
public void getOrder() throws IOException {
    // 获取订单状态
}

public void getCertificate() throws IOException {
    // 获取证书并保存
}
```

## 使用步骤

1. **创建 ACME 客户端实例**  
   实例化 `AcmeClient` 类，自动初始化 ACME 目录。

2. **创建账户**  
   调用 `createAccount` 方法并提供邮箱，完成账户创建。

3. **创建订单**  
   调用 `newOrder` 方法并提供域名列表，创建新的证书请求订单。

4. **验证挑战**  
   调用 `validateOrder` 方法，自动处理 DNS-01 挑战验证。

5. **完成订单并获取证书**  
   调用 `finalizeOrder` 和 `getCertificate` 方法，完成订单并获取证书。

### 示例代码

```java
public class Main {
    public static void main(String[] args) throws Exception {
        AcmeClient acmeClient = new AcmeClient();
        
        // 创建账户
        acmeClient.createAccount("your-email@example.com");

        // 创建订单
        List<String> domains = Arrays.asList("example.com", "www.example.com");
        acmeClient.newOrder(domains);

        // 验证订单挑战
        acmeClient.validateOrder();

        // 完成订单并获取证书
        acmeClient.getOrder();
        acmeClient.getCertificate();
    }
}
```

## 代码结构

- **AcmeClient**：核心类，处理账户管理、订单创建、挑战验证和证书获取。
- **Payload**：封装请求体的数据类。
- **ProtectedHeader**：封装请求头的签名数据。
- **Jws**：封装 JSON Web 签名，用于加密和签名请求数据。
- **Challenge**：表示 ACME 协议中的挑战信息。
- **Utils**：包含各种辅助工具类，如 HTTP 请求处理、加密处理等。

## 配置项

- **DIRECTORY_URL**：ACME 目录 URL（默认为 Let's Encrypt 的测试环境 URL）。
- **证书保存路径**：证书将保存为 `cert.pem`，私钥保存为 `private.key`。

## 错误处理

在过程中，若遇到挑战验证失败或订单状态无效，客户端会抛出 `RuntimeException`，需要根据实际情况进行处理。

## 注意事项

- 在进行 DNS-01 挑战时，请确保您的 DNS 记录已经正确配置并生效。挑战成功后，ACME 服务器将验证您的 DNS 记录是否正确。
- 证书生成后，记得将证书和私钥妥善保存。

## License

该项目使用 **MIT License** 进行授权，详情请参见 [LICENSE](LICENSE) 文件。

---

这个 `README` 文件概述了 `AcmeClient` 的核心功能、使用步骤以及代码结构，帮助开发人员快速上手并使用该 ACME 客户端进行自动化证书管理。