package com.itime.serna.chat.handler

import org.springframework.stereotype.Component
import reactor.core.publisher.Sinks

@Component
class SinkWrapper {
val sinks: Sinks.Many<SendTo> = Sinks.many().multicast().onBackpressureBuffer()
}
