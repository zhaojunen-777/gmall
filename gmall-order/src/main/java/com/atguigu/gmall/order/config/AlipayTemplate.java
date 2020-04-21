package com.atguigu.gmall.order.config;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.atguigu.gmall.order.vo.PayVo;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "alipay")
@Component
@Data
public class AlipayTemplate {

    //在支付宝创建的应用的id
    private   String app_id = "2016102400748057";

    // 商户私钥，您的PKCS8格式RSA2私钥
    private  String merchant_private_key = "MIIEvwIBADANBgkqhkiG9w0BAQEFAASCBKkwggSlAgEAAoIBAQCvu5Ezd7a3s4Q6uGkV+FRniSjDWlSZHRdQhyaiKdFlhgrhOftQbX5iKg6xezx3tyxZoMhRgq/bqgYkfHCFTcTqqYvH6V4lXQjdASyAl5qgdLQc1ZJeRkCQkEPexeBhLmAJY5ENPiTYOnzxwTzfDDsZeOm5bbd0niTYJMMM/1CdvnG7enXlCTn1DWzT2Frw6mjjJ5THm10XqNXvN8XfU4hjQ2ViJjhTDzwSeBXrUDqnaJi+Sskj1oofiuepfyAYup8pzfb9ha2j7CUoP24Xm0meiqzW3337bn748BohMnhngyKMk4BK8O4CNMMEQUW37H4pfwHmtP38A7I/tUC4uN0XAgMBAAECggEAEBt6IKSNa+MZcKRshWYAnojU4qsJ7ArdkzUAFocMuWiC4MgGwAV5OxgtxpjMHxD8EK8LDWWUJuc7ruZ1DdLmBLhLpqQ+S+BuKour/5Fly+VGiQoOY3O/AUEn52htu5qblOC6b+NNT0B1KVeT88HUPO53RdbHpPnF2MXt1ugkBIFEW4U2aOFtRA/sxe15HYG9tQyEO27G0uD3I1R2YMFnT2rS+KiEf+0ZzgUnBdhxjiDdxHHTWrmSn0/a4FiBED/GGAxZ5ZwKYZCx8LmHrM3KR0WhKrOH5xoPvOBf6NBvjxZyYIeI4jfd551wOWCXiUogIH1OZT259E9SqKrDXZ57wQKBgQDUZPvfdzKBlLTGn04dqOBuwrjGUbQ7z+/VBlMYAAQvUvX9qpn9/CjJhSuxyx6cnQP/tcRDKhKvXhDmyybAQEVrZCe6ZqGTFPPvYsU3bAKSqQA0zC7VdXlwyYWnIC2xKFdpr8Lf3MQ++oUoDPBfPK+M0LEw07djaL17VXa8WxpRdwKBgQDTz7ckrjKCyTftKzJGs1rFCPD9QZGhJUwS7JNLE7ILw6D13dbtvOZJ2PzonzcOJDNsGg7y4Y/N7tfhaJ75JjK0hoPDDCzvvshUZSSfJx+NG02trrFmT+uk1rmkAWMzmW7ow9hdiERJy1NjnVEd+UVSD6ejBkkc2o+EWbZP2Ge5YQKBgQDE7rVTqBPebmGH4OHvQkyGpmnpCIzTAKRhorGdCTbYIlYP1AVgqFOxNpcjDTDfCf9RM9o8ZGBa1h9Zv3e4vl8figgEH3ElDUJ47d3q220vSx/z1HaAWaI2X8RbB80V/E/AoMVSCEK2GlcooYam53/mUwJYQZZCyDUYjE/Bqb8nVQKBgQCeRj0If9LmwA22f+zVv/g3+/J4jKKR1BkAmx+FnnYRLGH+14JfrCQ5UpfKDA9L1elHAkHhZIPc0nkmytLgQpbpJwsWmOaLT/jKd7nh445EFv74pe2SEm53gqy9zPuf9ytVa+MmIUlGC1WIvml7CGWwaFpQC8ZedUOBFriQ+ZiogQKBgQDDP+1pdR7+Euaq310goUyBd1KnGai5jff3n++ivlGb00eneOpNavqCojzZUCgvrA/h/WD5YhAqPepZ+TKNTSDjv8Fr0V3XoDrolDmlYFi5UoXOdgWR+LysOesT9G3zAV3R/0Fhjo5wHD8jTJ9mHNczx6/pFEAZmeT1DANE3DvFBw==";
    // 支付宝公钥,查看地址：https://openhome.alipay.com/platform/keyManage.htm 对应APPID下的支付宝公钥。
    private  String alipay_public_key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEApEvZED3Eh+LalopFS5GlO4384WY1OFyArR3ScPEsp2+h9o/uY8zTuX7dwgx6cMgoIz6qoFSoX5XcD9hpuynoBxktcibz2wUen2pGpgrKFpRSAINXMBafi9hfOXfjjuofsapRy98LDl70SkDCZuHcx+TiogbGzwufiNDfWzXZWfE+D4R9vqMAUTZySShoQNmE+/Jk/KwUVx6Lx3MQZ1+cFTrMcM9DGsyansQNy5mhfD6qENm9DMkM9PrlKawOK4l7D+psEhatB5/IBI3y9FqLRE9Mq40zNR2ikiwwyWdQTyRdIt1W9Bl9mq9xnaxzVuNkSmtVqWoMhl+6ToOpoSRpxwIDAQAB";
    // 服务器[异步通知]页面路径  需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    // 支付宝会悄悄的给我们发送一个请求，告诉我们支付成功的信息
    private  String notify_url;

    // 页面跳转同步通知页面路径 需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    //同步通知，支付成功，一般跳转到成功页
    private  String return_url;

    // 签名方式
    private  String sign_type = "RSA2";

    // 字符编码格式
    private  String charset = "utf-8";

    // 支付宝网关； https://openapi.alipaydev.com/gateway.do
    private  String gatewayUrl = "https://openapi.alipaydev.com/gateway.do";

    public  String pay(PayVo vo) throws AlipayApiException {

        //AlipayClient alipayClient = new DefaultAlipayClient(AlipayTemplate.gatewayUrl, AlipayTemplate.app_id, AlipayTemplate.merchant_private_key, "json", AlipayTemplate.charset, AlipayTemplate.alipay_public_key, AlipayTemplate.sign_type);
        //1、根据支付宝的配置生成一个支付客户端
        AlipayClient alipayClient = new DefaultAlipayClient(gatewayUrl,
                app_id, merchant_private_key, "json",
                charset, alipay_public_key, sign_type);

        //2、创建一个支付请求 //设置请求参数
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        alipayRequest.setReturnUrl(return_url);
        alipayRequest.setNotifyUrl(notify_url);

        //商户订单号，商户网站订单系统中唯一订单号，必填
        String out_trade_no = vo.getOut_trade_no();
        //付款金额，必填
        String total_amount = vo.getTotal_amount();
        //订单名称，必填
        String subject = vo.getSubject();
        //商品描述，可空
        String body = vo.getBody();

        alipayRequest.setBizContent("{\"out_trade_no\":\""+ out_trade_no +"\","
                + "\"total_amount\":\""+ total_amount +"\","
                + "\"subject\":\""+ subject +"\","
                + "\"body\":\""+ body +"\","
                + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");

        String result = alipayClient.pageExecute(alipayRequest).getBody();

        //会收到支付宝的响应，响应的是一个页面，只要浏览器显示这个页面，就会自动来到支付宝的收银台页面
        System.out.println("支付宝的响应："+result);

        return result;

    }
}
