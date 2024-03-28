package com.loot.server;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @SuppressWarnings("null")
	/**
	 * EnableSimpleBroker = Any messages sent to the prefix '/topic' will be routed to subscribers to that destination
	 * Set Application Destination Prefixes = Sets the prefix for messages that are sent from clients to the server
	 * any messages sent by clients to this prefix are routed to the message mapping controllers
	 */
	@Override
	public void configureMessageBroker(MessageBrokerRegistry config) {
		config.enableSimpleBroker("/topic/", "/queue/");
		config.setApplicationDestinationPrefixes("/app");
	}

	@SuppressWarnings("null")
	/**
	 * Add Endpoint = the endpoint that clients use to connect to the websocket
	 * @param registry -> stomp endpoint registry bean
	 */
    @Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint("/game-websocket");
	}

}
