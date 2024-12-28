package cn.lycodeing.test;

import cn.lycodeing.AcmeClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Collections;

@RunWith(JUnit4.class)
public class AcmeTest {

    @Test
    public void createSslTest() throws Exception {
        AcmeClient acmeClient = new AcmeClient();
        // 创建账号
        acmeClient.createAccount("213123@qq.com");
        // 创建订单
        acmeClient.newOrder(Collections.singletonList("oll.lycodeing.cn"));

        // 验证等待
        acmeClient.validateOrder();

        // Allow time for DNS propagation
        Thread.sleep(1000 * 5);

        // 完成订单
        acmeClient.finalizeOrder(acmeClient.getFinalizeOrderUrl());

        // 查询等待状态
        acmeClient.getOrder();

        // 下载证书
        acmeClient.getCertificate();
    }
}
