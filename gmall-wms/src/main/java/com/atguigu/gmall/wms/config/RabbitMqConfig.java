package com.atguigu.gmall.wms.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMqConfig {

    @Bean("ttl-queue")
    public Queue ttlQueue(){
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange", "ORDER-EXCHANGE");
        arguments.put("x-dead-letter-routing-key", "wms.dead");
        arguments.put("x-message-ttl", 90000);// 单位是毫秒
        return new Queue("WMS-TTL-QUEUE", true, false, false, arguments);
    }


    @Bean("ttl-binding")
    public Binding ttlBinding(){

        return new Binding("WMS-TTL-QUEUE", Binding.DestinationType.QUEUE, "ORDER-EXCHANGE", "wms.ttl", null);
    }

//    @Bean
//    public Queue deadQueue(){
//        return new Queue("WMS-DEAD-QUEUE",true,false,false,null);
//    }
//
//    @Bean
//    public Binding deadBinding(){
//        return new Binding("WMS-DEAD-QUEUE",Binding.DestinationType.QUEUE,"ORDER-EXCHANGE","wms.dead",null);
//    }
}
