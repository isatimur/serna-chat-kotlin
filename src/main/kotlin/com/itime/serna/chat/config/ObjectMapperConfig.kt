package com.itime.serna.chat.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.jsontype.NamedType
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule
import com.timurisachenko.chatreactive.domain.ImageMessage
import com.timurisachenko.chatreactive.domain.TextMessage
import com.timurisachenko.chatreactive.event.MarkMessageAsRead
import com.timurisachenko.chatreactive.event.NewMessageEvent
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ObjectMapperConfig {

    @Bean
    fun objectMapper(): ObjectMapper = ObjectMapper()
        .registerModule(JavaTimeModule())
        .registerModule(Jdk8Module())
        .registerModule(ParameterNamesModule())
        .registerModule(KotlinModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .apply {
            registerSubtypes(
                NamedType(NewMessageEvent::class.java, "NewMessageEvent"),
                NamedType(MarkMessageAsRead::class.java, "NewMessageEvent"),
                NamedType(TextMessage::class.java, "NewMessageEvent"),
                NamedType(ImageMessage::class.java, "ImageMessage")

            )
        }
}