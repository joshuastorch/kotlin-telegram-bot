package com.github.kotlintelegrambot.dispatcher

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.dispatcher.handlers.ErrorHandler
import com.github.kotlintelegrambot.dispatcher.handlers.Handler
import com.github.kotlintelegrambot.entities.Update
import com.github.kotlintelegrambot.errors.TelegramError
import com.github.kotlintelegrambot.logging.LogLevel
import com.github.kotlintelegrambot.types.DispatchableObject
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue

class Dispatcher(
    val updatesQueue: BlockingQueue<DispatchableObject> = LinkedBlockingQueue()
) {
    internal lateinit var logLevel: LogLevel
    lateinit var bot: Bot

    private val commandHandlers = mutableListOf<Handler>()
    private val errorHandlers = arrayListOf<ErrorHandler>()
    private var stopped = false

    fun startCheckingUpdates() {
        stopped = false
        checkQueueUpdates()
    }

    private fun checkQueueUpdates() {
        while (!Thread.currentThread().isInterrupted && !stopped) {
            val item = updatesQueue.take()
            when (item) {
                is Update -> handleUpdate(item)
                is TelegramError -> handleError(item)
                else -> Unit
            }
        }
    }

    fun addHandler(handler: Handler) {
        commandHandlers.add(handler)
    }

    fun removeHandler(handler: Handler) {
        commandHandlers.remove(handler)
    }

    fun addErrorHandler(errorHandler: ErrorHandler) {
        errorHandlers.add(errorHandler)
    }

    fun removeErrorHandler(errorHandler: ErrorHandler) {
        errorHandlers.remove(errorHandler)
    }

    private fun handleUpdate(update: Update) {
        commandHandlers
            .forEach {
                if (update.consumed) {
                    return
                }
                if (!it.checkUpdate(update)) {
                    return@forEach
                }
                try {
                    it.handlerCallback(bot, update)
                } catch (exc: Exception) {
                    if (logLevel.shouldLogErrors()) {
                        exc.printStackTrace()
                    }
                }
            }
    }

    private fun handleError(error: TelegramError) {
        errorHandlers.forEach { handleError ->
            try {
                handleError(bot, error)
            } catch (exc: Exception) {
                if (logLevel.shouldLogErrors()) {
                    exc.printStackTrace()
                }
            }
        }
    }

    internal fun stopCheckingUpdates() {
        stopped = true
    }
}
