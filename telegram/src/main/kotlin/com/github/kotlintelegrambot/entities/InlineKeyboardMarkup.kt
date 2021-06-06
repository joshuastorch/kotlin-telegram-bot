package com.github.kotlintelegrambot.entities

import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import com.github.kotlintelegrambot.network.serialization.GsonFactory
import com.google.gson.annotations.SerializedName

/**
 * This object represents an inline keyboard that appears right next to the message it belongs to.
 * @param inlineKeyboard Array of button rows, each represented by an Array of [InlineKeyboardButton] objects.
 * @see <https://core.telegram.org/bots/api#inlinekeyboardmarkup>
 */
data class InlineKeyboardMarkup internal constructor(
    @SerializedName("inline_keyboard") val inlineKeyboard: List<List<InlineKeyboardButton>>
) : ReplyMarkup {

    init {
        // Buttons of type [Pay] must always be the first button in the first row.
        val flattenedButtons = inlineKeyboard.flatten()
        val payButtons = flattenedButtons.filterIsInstance<InlineKeyboardButton.Pay>()
        require(payButtons.size <= 1) { "Can't have more than one pay button per inline keyboard" }
        require(payButtons.size != 1 || flattenedButtons.firstOrNull() is InlineKeyboardButton.Pay) {
            "Pay buttons must always be the first button in the first row"
        }
    }

    companion object {
        private val GSON = GsonFactory.createForApiClient()

        fun createSingleButton(button: InlineKeyboardButton) = InlineKeyboardMarkup(listOf(listOf(button)))
        fun createSingleRowKeyboard(buttons: List<InlineKeyboardButton>) = InlineKeyboardMarkup(listOf(buttons))
        fun createSingleRowKeyboard(vararg button: InlineKeyboardButton) = InlineKeyboardMarkup(listOf(button.toList()))
        fun create(buttons: List<List<InlineKeyboardButton>>) = InlineKeyboardMarkup(buttons)
        fun create(vararg buttonsRow: List<InlineKeyboardButton>) = InlineKeyboardMarkup(buttonsRow.toList())
        fun createSingleColumnKeyboard(buttons: List<InlineKeyboardButton>) = InlineKeyboardMarkup(buttons.map { listOf(it) })
        fun createSingleColumnKeyboard(vararg button: InlineKeyboardButton) = InlineKeyboardMarkup(button.map { listOf(it) })
    }

    override fun toString(): String = GSON.toJson(this)
}
