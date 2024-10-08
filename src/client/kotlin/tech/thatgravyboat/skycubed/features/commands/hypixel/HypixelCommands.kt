package tech.thatgravyboat.skycubed.features.commands.hypixel

import com.google.gson.JsonArray
import com.mojang.brigadier.tree.RootCommandNode
import kotlinx.coroutines.runBlocking
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.network.protocol.game.ClientboundCommandsPacket
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.level.PacketReceivedEvent
import tech.thatgravyboat.skyblockapi.api.events.misc.RegisterCommandsEvent
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.utils.json.Json.readJson
import tech.thatgravyboat.skyblockapi.utils.json.Json.toData


object HypixelCommands {

    private val commands: MutableList<LiteralHypixelCommand> = mutableListOf()

    init {
        runBlocking {
            runCatching {
                val file = this.javaClass.getResourceAsStream("/repo/commands.json")?.readJson<JsonArray>() ?: return@runCatching
                file.toData(LiteralHypixelCommand.CODEC.listOf())?.let(commands::addAll)
            }
        }
    }

    fun removeServerCommands(root: RootCommandNode<SharedSuggestionProvider>) {
        commands.forEach { command ->
            if (command.values.none { root.getChild(it) != null }) return@forEach

            command.values.forEach { value ->
                root.children.removeIf { node -> node.name.equals(value, true) }
            }
        }
    }

    @Subscription
    fun onCommandRegistration(event: RegisterCommandsEvent) {
        commands.forEach { command ->
            command.toCommand().forEach {
                event.register(it)
            }
        }
    }
}