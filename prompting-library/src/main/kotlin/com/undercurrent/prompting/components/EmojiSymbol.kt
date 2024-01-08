package com.undercurrent.prompting.components

import com.vdurmont.emoji.EmojiParser

/**
 * Unicode lookup for emojis
 */
enum class EmojiSymbol(val tag: String, val header: String) {
    STOP_SIGN("🛑", header = "ERROR"),
    CONSTRUCTION("🚧", header = "IN PROGRESS"),
    AMBULANCE("🚑", header = "IN PROGRESS"),
    FIRE("🔥", header = "IN PROGRESS"),
    CRYSTAL_BALL("🔮", header = "IN PROGRESS"),
    LOCK("🔒", header = "IN PROGRESS"),
    BELL("🔔", header = "IN PROGRESS"),
    UNLOCK("🔓", header = "IN PROGRESS"),
    KEY("🔑", header = "IN PROGRESS"),
    CLOSED_LOCK_WITH_KEY("🔐", header = "IN PROGRESS"),
    SATELLITE("📡", header = "IN PROGRESS"),
    CLIPBOARD("📋", header = "IN PROGRESS"),
    FLOPPY_DISK("💾", header = "IN PROGRESS"),
    CURRENCY_EXCHANGE("💱", header = "IN PROGRESS"),
    MONEYBAG("💰", header = "IN PROGRESS"),
    HEART("❤️", header = "IN PROGRESS"),
    PENCIL2("✏️", header = "IN PROGRESS"),
    CLOCK130("🕜", header = "IN PROGRESS"),
    ROTATING_LIGHT("🚨", header = "IN PROGRESS"),
    HOTSPRINGS("♨️", header = "IN PROGRESS"),
    EXCLAMATION("❗️", header = "IN PROGRESS"),
    GREY_QUESTION("❔", header = "IN PROGRESS"),
    WARNING("⚠️", header = "IN PROGRESS"),
    GREEN_CHECK_MARK("✅", header = "IN PROGRESS"),
    SIGNAL_STRENGTH("📶", header = "IN PROGRESS"),
    LOUDSPEAKER("📢", header = "IN PROGRESS"),
    MEGA("📣", header = "IN PROGRESS"),
    WAIT(":hourglass:", "In progress"),
    ALERT(":loudspeaker:", "In progress"),
    SUCCESS(":white_check_mark:", "success");

    fun parse(): String {
        return EmojiParser.parseToUnicode(this.tag)
    }

    fun prefix(): String {
        return "${parse()}\t[${header.uppercase()}]\n\n"
    }


    /**
     * :construction:
     * :ambulance:
     * :fire:
     * :crystal_ball:
     * :lock:
     * :bell:
     * :unlock:
     * :key:
     * :closed_lock_with_key:
     * :satellite:  // satellite antenna
     * :clipboard:  // perhaps for pasting
     * :floppy_disk:  // for saving
     * :currency_exchange:
     * :moneybag:
     * :heart:
     * :pencil2:  // pencil (to indicate editing)
     * :clock130:  // clock face 1:30
     * :rotating_light: // alert (police strobe)
     * :hotsprings:
     * :exclamation:
     * :grey_question:
     * :warning:
     * :white_check_mark:
     * :signal_strength:
     * :loudspeaker:
     * :mega:
     */
}