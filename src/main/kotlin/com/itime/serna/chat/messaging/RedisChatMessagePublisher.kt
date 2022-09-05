package com.itime.serna.chat.messaging

import com.timurisachenko.chatreactive.domain.CommonMessage
import com.timurisachenko.chatreactive.utils.ObjectStringConverter
import org.slf4j.Logger
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class RedisChatMessagePublisher(
    val logger: Logger,
    val reactiveStringRedisTemplate: ReactiveStringRedisTemplate,
    val objectStringConverter: ObjectStringConverter
) {
    fun broadcastMessage(commonMessage: CommonMessage): Mono<Void> {
        return objectStringConverter.objectToString(commonMessage)
            .flatMap {
                logger.info("Broadcast message $it to channel ${CommonMessage::class.java.name}")
                reactiveStringRedisTemplate.convertAndSend(CommonMessage::class.java.name, it)
            }
            .then()
    }
}