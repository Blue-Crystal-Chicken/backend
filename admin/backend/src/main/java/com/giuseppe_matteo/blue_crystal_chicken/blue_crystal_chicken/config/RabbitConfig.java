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
 * Topologia AMQP per le notifiche-log.
 *
 * exchange "bcc.events" (topic)
 *   └─ binding "event.#" ─► queue "bcc.notifications"
 *
 * Routing key usate: event.offer.*, event.product.*, event.location.*, event.stock.low
 * (Predisposto per la futura fase Manager con una seconda coda "bcc.requests".)
 */
@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "bcc.events";
    public static final String NOTIFICATIONS_QUEUE = "bcc.notifications";
    public static final String ROUTING_PATTERN = "event.#";

    // FASE 2 — Manager: canale richieste/approvazioni
    public static final String REQUESTS_QUEUE = "bcc.requests";              // Manager -> Admin
    public static final String REQUESTS_PATTERN = "request.created";
    public static final String REQUESTS_REPLY_QUEUE = "bcc.requests.reply";  // Admin -> Manager
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

    // Serializzazione JSON dei messaggi (usata sia da RabbitTemplate sia dal listener)
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
