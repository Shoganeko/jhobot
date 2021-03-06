package dev.shog.chad.framework.handle.coroutine

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import dev.shog.chad.core.ChadVar
import dev.shog.chad.core.getLogger
import dev.shog.chad.framework.handle.GuildHandler
import dev.shog.chad.framework.handle.MessageHandler
import dev.shog.chad.framework.handle.PermissionHandler
import dev.shog.chad.framework.handle.xp.XPHandler
import dev.shog.chad.framework.obj.Command
import dev.shog.chad.framework.obj.Guild
import org.json.JSONArray
import sx.blah.discord.api.events.EventSubscriber
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageEditEvent
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageEvent
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.handle.obj.IUser
import sx.blah.discord.handle.obj.Permissions
import sx.blah.discord.util.RequestBuffer
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.regex.Pattern

class CoroutineManager internal constructor(): CoroutineScope by CoroutineScope(Dispatchers.Unconfined) {
    /**
     * The user's and how many threads they're running.
     */
    val users = ConcurrentHashMap<IUser, Int>()

    /**
     * Handles message edit events.
     */
    @EventSubscriber
    fun messageEditEvent(event: MessageEditEvent) { event(event) }

    /**
     * Handles message received events.
     */
    @EventSubscriber
    fun messageReceivedEvent(event: MessageReceivedEvent) { event(event) }

    /**
     * Dispatches the events.
     */
    private fun event(event: MessageEvent) {
        if (event.guild == null) return

        if (event.author.isBot) return

        val argArray = event.message.content.split(" ")
        if (argArray.isEmpty()) return

        XPHandler.getUserInstance(event.author).registerChat(event.message)

        val guild = GuildHandler.getGuild(event.guild.longID)
        guild.messageSent()

        if (guild.getObject(Guild.DataType.SWEAR_FILTER) as Boolean) {
            // Builds together the message & removes the special characters
            var character = argArray.joinToString("")
            val pt = Pattern.compile("[^a-zA-Z0-9]")
            val match = pt.matcher(character)

            while (match.find()) {
                character = character.replace(("\\" + match.group()).toRegex(), "")
            }

            // Checks if the word contains a swear word
            for (swearWord in ChadVar.swearWords) {
                // Ass is a special case, due to words like `bass`
                if (swearWord.equals("ass", ignoreCase = true) && character.contains("ass", ignoreCase = true)) {
                    // Goes through all of the arguments
                    for (argument in argArray) {
                        // If the argument is just ass
                        if (argument.equals("ass", ignoreCase = true)) {
                            // Delete it
                            RequestBuffer.request { event.message.delete() }
                            return
                        }
                    }
                    continue
                }

                // If it contains any other swear word, delete it
                if (character.toLowerCase().contains(swearWord)) {
                    RequestBuffer.request { event.message.delete() }
                    return
                }
            }
        }

        val prefix: String = guild.getObject(Guild.DataType.PREFIX) as String
        if (!argArray[0].toLowerCase().startsWith(prefix)) return

        val commandString = argArray[0].substring(prefix.length).toLowerCase()

        val args = ArrayList(mutableListOf(*argArray.toTypedArray()))
        args.removeAt(0)

        if (!event.author.canRun()) return

        var command: Command.Data? = null
        var commandName: String? = null

        for (cmd in Command.COMMANDS.keys) {
            val data = Command.COMMANDS[cmd]!!

            if (commandString.equals(cmd, true)) {
                command = data
                commandName = cmd
                break
            }

            if (data.usesAliases()) {
                for (cmdAlias in data.cmdAliases!!) {
                    if (commandString.equals(cmdAlias, true)) {
                        command = data
                        commandName = cmd
                        break
                    }
                }
            }
        }

        if (command == null) return

        if (command.commandCategory == Command.Category.DEVELOPER && !PermissionHandler.isDeveloper(event.author)) {
            MessageHandler(event.channel, event.author).sendPresetError(MessageHandler.Messages.USER_NOT_DEVELOPER)
            return
        }
      
        if (
                JSONArray(guild.getObject(Guild.DataType.DISABLED_CATEGORIES) as String)
                        .contains(command.commandCategory.toString().toLowerCase())
        ) return

        if (!PermissionHandler.hasPermission(commandName!!, event.author, event.guild) && !event.author.getPermissionsForGuild(event.guild).contains(Permissions.ADMINISTRATOR)) {
            MessageHandler(event.channel, event.author).sendPresetError(MessageHandler.Messages.USER_NO_PERMISSION)
            return
        }

        launch {
            getLogger().debug("Starting task for user ${event.author.stringID}: $commandName")
            synchronized(users) {
                users[event.author] = if (users[event.author] != null) users[event.author]!! + 1 else 1
            }

            val successful: Boolean = try {
                if (args.size == 1 && args[0].equals("help", ignoreCase = true))
                    command.commandClass.help(event)
                else command.commandClass.run(event, args)

                true
            } catch (ex: Exception) {
                throw ex
//                request { event.channel.sendMessage("There was an issue running that command!\nError: `${ex.message}`") }
                false
            }

            synchronized(users) {
                users[event.author] = users[event.author]!! - 1
            }

            if (successful) {
                getLogger().debug("Finished task successfully for user ${event.author.name}")
            } else {
                getLogger().debug("Finished task unsuccessfully for user ${event.author.name}")
            }
        }
    }

    /**
     * If an IUser can run a coroutine.
     */
    private fun IUser.canRun(): Boolean = !(users.containsKey(this) && users[this]!! >= 3)

    companion object {
        /**
         * The main instance of the Coroutine Manager.
         */
        @JvmStatic
        val instance = CoroutineManager()
    }
}