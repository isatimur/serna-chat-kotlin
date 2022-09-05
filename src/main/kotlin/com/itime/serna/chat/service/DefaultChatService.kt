package com.itime.serna.chat.service

import com.timurisachenko.chatreactive.domain.ChatMember
import com.timurisachenko.chatreactive.domain.CommonMessage
import com.timurisachenko.chatreactive.domain.TextMessage
import com.timurisachenko.chatreactive.event.ChatMessageEvent
import com.timurisachenko.chatreactive.event.NewMessageEvent
import com.timurisachenko.chatreactive.event.WebSocketEvent
import com.timurisachenko.chatreactive.messaging.RedisChatMessagePublisher
import com.timurisachenko.chatreactive.handler.SendTo
import com.timurisachenko.chatreactive.handler.SinkWrapper
import com.timurisachenko.chatreactive.repository.ChatRepository
import com.timurisachenko.chatreactive.service.api.ChatService
import org.slf4j.Logger
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.Sinks
import java.time.LocalDateTime
import java.util.*

@Service
class DefaultChatService(
    val logger: Logger,
    val sinkWrapper: SinkWrapper,
    val chatRepository: ChatRepository,
    val redisChatMessagePublisher: RedisChatMessagePublisher
) : ChatService {
    override fun handleNewMessageEvent(senderId: UUID, newMessageEvent: NewMessageEvent): Mono<Void> {
        logger.info("Receive NewMessageEvent from $senderId: $newMessageEvent")
        return chatRepository.findById(newMessageEvent.chatId)
            .filter { it.chatMembers.map(ChatMember::userId).contains(senderId) }
            .flatMap { chat ->
                val textMessage = TextMessage(
                    UUID.randomUUID(),
                    chat.chatId,
                    chat.chatMembers.first { it.userId == senderId },
                    newMessageEvent.content,
                    LocalDateTime.now(),
                    false
                )
                chat.lastMessage = textMessage
                return@flatMap Mono.zip(chatRepository.save(chat), Mono.just(textMessage))
            }
            .flatMap { broadcastMessage(it.t2) }
    }

    override fun markPreviousMessagesAsRead(messageId: UUID): Mono<Void> {
        TODO("Not yet implemented")
    }

    override fun sendEventToUserId(userId: UUID, webSocketEvent: WebSocketEvent): Mono<Void> {
        return Mono.fromCallable {
            sinkWrapper.sinks.emitNext(
                SendTo(userId, webSocketEvent),
                Sinks.EmitFailureHandler.FAIL_FAST
            )
        }
            .then()
    }

    override fun sendMessage(message: CommonMessage): Mono<Void> {
        return chatRepository.findById(message.chatId)
            .map { it.chatMembers }
            .flatMapMany { Flux.fromIterable(it) }
            .flatMap { member -> sendEventToUserId(member.userId, ChatMessageEvent(message.chatId, message)) }
            .then()

    }

    override fun broadcastMessage(commonMessage: CommonMessage): Mono<Void> {
        return redisChatMessagePublisher.broadcastMessage(commonMessage)
    }
}