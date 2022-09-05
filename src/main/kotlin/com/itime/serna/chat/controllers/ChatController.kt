package com.itime.serna.chat.controllers

import com.pusher.rest.Pusher
import com.timurisachenko.chatreactive.dtos.MessageDTO
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/api")
class ChatController {

    @PostMapping("messages")
    fun message(@RequestBody body: MessageDTO): ResponseEntity<Any> {

        val pusher = Pusher("1399711", "1f100328f42b3d014ca2", "18aea7feec90ddce7057")
        pusher.setCluster("eu")
        pusher.setEncrypted(true)

        pusher.trigger("chat", "message", body)

        return ResponseEntity.ok(emptyArray<String>())

    }
}