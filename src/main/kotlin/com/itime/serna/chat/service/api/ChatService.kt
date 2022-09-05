package com.itime.serna.chat.service.api

import com.timurisachenko.chatreactive.domain.CommonMessage
import com.timurisachenko.chatreactive.event.NewMessageEvent
import com.timurisachenko.chatreactive.event.WebSocketEvent
import reactor.core.publisher.Mono
import java.util.*

interface ChatService {
    fun handleNewMessageEvent(senderId: UUID, newMessageEvent: NewMessageEvent): Mono<Void>
    fun markPreviousMessagesAsRead(messageId: UUID): Mono<Void>
    fun sendEventToUserId(userId: UUID, webSocketEvent: WebSocketEvent): Mono<Void>
    fun sendMessage(message: CommonMessage): Mono<Void>
    fun broadcastMessage(commonMessage: CommonMessage): Mono<Void>

}
