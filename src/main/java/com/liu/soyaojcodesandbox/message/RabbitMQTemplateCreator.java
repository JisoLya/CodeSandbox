package com.liu.soyaojcodesandbox.message;

import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

public class RabbitMQTemplateCreator {
    public static RabbitTemplate createTemplateForExisting(
            String host,
            int port,
            String username,
            String password) {
        // 创建连接工厂
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();

        // 配置连接参数
        connectionFactory.setHost(host);
        connectionFactory.setPort(port);
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);

        // 创建RabbitTemplate
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);

        // 使用JSON消息转换器
//        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());

        return rabbitTemplate;
    }
}