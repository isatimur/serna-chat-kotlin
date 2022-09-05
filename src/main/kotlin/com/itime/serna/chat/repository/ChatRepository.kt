package com.itime.serna.chat.repository

import com.timurisachenko.chatreactive.domain.Chat
import reactor.core.publisher.Mono
import java.util.*

interface ChatRepository  {
    fun findById(chatId: UUID): Mono<Chat>
    fun save(chat: Chat): Mono<Chat>
}