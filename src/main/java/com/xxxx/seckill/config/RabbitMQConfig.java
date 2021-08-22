package com.xxxx.seckill.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * @Auther:huhao
 * @Date:2021/8/23-2:28
 * @Description:com.xxxx.seckill.config
 * @version:1.0
 */
@Configuration
public class RabbitMQConfig {
    @Bean
    public Queue queue(){
        return new Queue("queue",true);
    }

}
