package com.undercurrent.legacy.commands.registry

interface UserCommand : BaseCommand {
    val commandGroup: Set<TopCommand>
}