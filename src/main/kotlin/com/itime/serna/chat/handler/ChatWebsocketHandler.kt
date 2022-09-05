package com.itime.serna.chat.handler

import com.fasterxml.jackson.databind.ObjectMapper
import com.timurisachenko.chatreactive.event.MarkMessageAsRead
import com.timurisachenko.chatreactive.event.NewMessageEvent
import com.timurisachenko.chatreactive.event.WebSocketEvent
import com.timurisachenko.chatreactive.service.api.ChatService
import com.timurisachenko.chatreactive.utils.ObjectStringConverter
import io.jsonwebtoken.Claims
import org.slf4j.Logger
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketMessage
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Mono
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@Component
class ChatWebsocketHandler(
    val objectMapper: ObjectMapper,
    val logger: Logger,
    val chatService: ChatService,
    val objectStringConverter: ObjectStringConverter,
    val sinkWrapper: SinkWrapper
) : WebSocketHandler {

    private val userIdToSession: MutableMap<UUID, LinkedList<WebSocketSession>> = ConcurrentHashMap()

    override fun handle(session: WebSocketSession): Mono<Void> {
        return ReactiveSecurityContextHolder.getContext().flatMap { ctx ->
            val userId = UUID.fromString((ctx.authentication.details as Claims)["id"].toString())
            val sender = getSenderStream(session, userId)
            val receiver = getReceiverStream(session, userId)

            return@flatMap Mono.zip(sender, receiver).then()
        }
    }

    private fun getReceiverStream(session: WebSocketSession, userId: UUID): Mono<Void> {
        return session.receive().filter { it.type == WebSocketMessage.Type.TEXT }
            .map(WebSocketMessage::getPayloadAsText).flatMap {
                objectStringConverter.stringToObject(it, WebSocketEvent::class.java)
            }.flatMap { convertedEvent ->
                when (convertedEvent) {
                    is NewMessageEvent -> chatService.handleNewMessageEvent(userId, convertedEvent)
                    is MarkMessageAsRead -> chatService.markPreviousMessagesAsRead(convertedEvent.messageId)
                    else -> Mono.error(RuntimeException())
                }
            }.onErrorContinue { t, _ -> logger.error("Error occured with receiver stream", t) }.doOnSubscribe {
                val userSession = userIdToSession[userId]
                if (userSession == null) {
                    val newUserSessions = LinkedList<WebSocketSession>()
                    userIdToSession[userId] = newUserSessions
                }
                userIdToSession[userId]?.add(session)
            }.doFinally {
                val userSessions = userIdToSession[userId]
            }.then()
    }

    private fun getSenderStream(session: WebSocketSession, userId: UUID?): Mono<Void> {
        val sendMessage = sinkWrapper.sinks.asFlux().filter { sendTo -> sendTo.userId == userId }
            .map { sendTo -> objectMapper.writeValueAsString(sendTo.event) }
            .map { stringObject -> session.textMessage(stringObject) }.doOnError { logger.error("", it) }
        return session.send(sendMessage)
    }
}