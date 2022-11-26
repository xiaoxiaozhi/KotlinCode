package com.kotlincode.myCoroutine

import java.net.ResponseCache
import java.net.Socket

open class WebSocketListener {
    /**
     * Invoked when a web socket has been accepted by the remote peer and may begin transmitting
     * messages.
     */
    open fun onOpen(webSocket: Socket, response: ResponseCache) {
    }

    /** Invoked when a text (type `0x1`) message has been received. */
    open fun onMessage(webSocket: Socket, text: String) {
    }
}