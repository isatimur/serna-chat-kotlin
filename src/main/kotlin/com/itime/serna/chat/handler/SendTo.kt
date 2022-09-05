package com.itime.serna.chat.handler

import com.timurisachenko.chatreactive.event.WebSocketEvent
import java.util.UUID

data class SendTo(val userId: UUID, val event: WebSocketEvent)