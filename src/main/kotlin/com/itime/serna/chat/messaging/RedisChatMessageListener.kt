package com.itime.serna.chat.messaging

import com.timurisachenko.chatreactive.domain.CommonMessage
import com.timurisachenko.chatreactive.domain.ImageMessage
import com.timurisachenko.chatreactive.domain.TextMessage
import com.timurisachenko.chatreactive.service.api.ChatService
import com.timurisachenko.chatreactive.utils.ObjectStringConverter
import org.reactivestreams.Publisher
import org.slf4j.Logger
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import org.springframework.data.redis.listener.PatternTopic
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class RedisChatMessageListener(
    private val logger: Logger,
    private val reactiveStringRedisTemplate: ReactiveStringRedisTemplate,
    private val objectStringConverter: ObjectStringConverter,
    private val chatService: ChatService
) {
    fun subscribeOnCommonMessageTopic(): Mono<Void> {
        return reactiveStringRedisTemplate.listenTo(PatternTopic(CommonMessage::class.java.name))
            .map { message -> message.message }
            .doOnNext { logger.info("Receive new message: $it") }
            .flatMap { objectStringConverter.stringToObject(it, CommonMessage::class.java) }
            .flatMap { message ->
                when (message) {
                    is TextMessage -> chatService.sendMessage(message)
                    is ImageMessage -> chatService.sendMessage(message)
                    else -> Mono.error(RuntimeException())
                } as Publisher<out Any>?
            }.then()
    }

}