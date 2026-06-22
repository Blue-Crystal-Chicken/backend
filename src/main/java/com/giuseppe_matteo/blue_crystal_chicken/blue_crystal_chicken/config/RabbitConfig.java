package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Topologia AMQP.
 *
 * exchange "bcc.events" (topic)
 *   ├─ "event.#"        ─► queue "bcc.notifications"     (log eventi)
 *   ├─ "request.created"─► queue "bcc.requests"          (Manager -> Admin)
 *   └─ "request.reply.#"─► queue "bcc.requests.reply"    (Admin -> Manager)
 */
@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "bcc.events";
    public static final String NOTIFICATIONS_QUEUE = "bcc.notifications";
    public static final String ROUTING_PATTERN = "event.#";

    public static final String REQUESTS_QUEUE = "bcc.requests";
    public static final String REQUESTS_PATTERN = "request.created";
    public static final String REQUESTS_REPLY_QUEUE = "bcc.requests.reply";
    public static final String REQUESTS_REPLY_PATTERN = "request.reply.#";

    @Bean
    public TopicExchange eventsExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue notificationsQueue() {
        return QueueBuilder.durable(NOTIFICATIONS_QUEUE).build();
    }

    @Bean
    public Binding notificationsBinding(Queue notificationsQueue, TopicExchange eventsExchange) {
        return BindingBuilder.bind(notificationsQueue).to(eventsExchange).with(ROUTING_PATTERN);
    }

    @Bean
    public Queue requestsQueue() {
        return QueueBuilder.durable(REQUESTS_QUEUE).build();
    }

    @Bean
    public Binding requestsBinding(Queue requestsQueue, TopicExchange eventsExchange) {
        return BindingBuilder.bind(requestsQueue).to(eventsExchange).with(REQUESTS_PATTERN);
    }

    @Bean
    public Queue requestsReplyQueue() {
        return QueueBuilder.durable(REQUESTS_REPLY_QUEUE).build();
    }

    @Bean
    public Binding requestsReplyBinding(Queue requestsReplyQueue, TopicExchange eventsExchange) {
        return BindingBuilder.bind(requestsReplyQueue).to(eventsExchange).with(REQUESTS_REPLY_PATTERN);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
