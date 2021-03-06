package dev.shog.chad.commands.info

import org.json.JSONException
import org.json.JSONObject
import dev.shog.chad.core.ChadVar
import dev.shog.chad.framework.handle.GuildHandler
import dev.shog.chad.framework.handle.JsonHandler
import dev.shog.chad.framework.handle.MessageHandler
import dev.shog.chad.framework.obj.Command
import dev.shog.chad.framework.obj.Guild
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageEvent
import sx.blah.discord.util.EmbedBuilder
import java.util.*

/**
 * Steam statistics utilizing steam's API
 *
 * @author sho
 */
class Steam : Command.Class {
    override suspend fun run(e: MessageEvent, args: MutableList<String>) {
        val messageHandler = MessageHandler(e.channel, e.author).credit("Steam API")
        val prefix = GuildHandler.getGuild(e.guild.longID).getObject(Guild.DataType.PREFIX) as String

        // Checks if the arguments are invalid
        if (args.isEmpty() || args.size == 1) {
            messageHandler.sendPresetError(MessageHandler.Messages.INVALID_ARGUMENTS, prefix + "steam profile [steam name]")
            return
        }

        try {
            // builds steam profile

            // The intention of the command
            val intention = args[0]

            // Gets the user's steam id by their username
            val steamUser = Objects.requireNonNull<JSONObject>(JsonHandler
                    .read("https://api.steampowered.com/ISteamUser/ResolveVanityURL/v0001/?key=" + ChadVar.STEAM_API_KEY + "&vanityurl=" + args[1]))
                    .getJSONObject("response")

            // Checks to see if the user actually exists
            if (steamUser.getInt("success") != 1) {
                messageHandler.sendError("Invalid Steam Profile!")
                return
            }

            // Gets the user's steam id
            val steamId = steamUser.getString("steamid")

            // Gets the user's profile
            val steamUserProfile = Objects.requireNonNull<JSONObject>(JsonHandler
                    .read("https://api.steampowered.com/ISteamUser/GetPlayerSummaries/v0002/?key=" + ChadVar.STEAM_API_KEY + "&steamids=" + steamId)).getJSONObject("response").getJSONArray("players").getJSONObject(0)

            // The user's name
            val userName = steamUserProfile.getString("personaname")


            // Builds an embed with the user's steam profile
            when {
                intention.equals("profile", ignoreCase = true) -> {
                    val embedBuilder = EmbedBuilder()
                    embedBuilder.withTitle("Steam Profile : $userName")
                    embedBuilder.withImage(steamUserProfile.getString("avatarfull"))
                    embedBuilder.appendField("SteamID", steamId, true)

                    messageHandler.sendEmbed(embedBuilder)
                }

                intention.equals("csgo", ignoreCase = true) -> {
                    val embedBuilder = EmbedBuilder()
                    embedBuilder.withFooterIcon(steamUserProfile.getString("avatar"))

                    val csgoStats = Objects.requireNonNull<JSONObject>(JsonHandler.read(
                            "https://api.steampowered.com/ISteamUserStats/GetUserStatsForGame/v0002/?appid=730&key="
                                    + ChadVar.STEAM_API_KEY + "&steamid=" + steamId)).getJSONObject("playerstats")
                            .getJSONArray("stats")

                    if (csgoStats == null) {
                        messageHandler.sendPresetError(MessageHandler.Messages.INTERNAL_EXCEPTION)
                        return
                    }

                    // Checks to see if the account is private by catching the NullPointerException
                    try {
                        csgoStats.getJSONObject(0)
                    } catch (ee: NullPointerException) {
                        messageHandler.sendError("Private Steam Profile!")
                        return
                    }


                    if (args.size == 2)
                    /* Regular stats in CSGO */ {
                        embedBuilder.withTitle("CS:GO stats for $userName")
                        embedBuilder.appendField("Total Kills", Integer.toString(csgoStats.getJSONObject(0).getInt("value")), true)
                        embedBuilder.appendField("Total Deaths", Integer.toString(csgoStats.getJSONObject(1).getInt("value")), true)
                        embedBuilder.appendField("Total Time Played", (csgoStats.getJSONObject(2).getInt("value") / 60 / 60).toString() + "hrs", true)
                        embedBuilder.appendField("Total Bombs Planted", Integer.toString(csgoStats.getJSONObject(3).getInt("value")), true)
                        embedBuilder.appendField("Total Bombs Defused", Integer.toString(csgoStats.getJSONObject(4).getInt("value")), true)
                        embedBuilder.appendField("Total Wins", Integer.toString(csgoStats.getJSONObject(5).getInt("value")), true)
                        embedBuilder.appendField("Total Damage Done", Integer.toString(csgoStats.getJSONObject(6).getInt("value")), true)
                        embedBuilder.appendField("Total Matches Won", Integer.toString(csgoStats.getJSONObject(112).getInt("value")), true)
                        embedBuilder.appendField("Total Matches Played", Integer.toString(csgoStats.getJSONObject(113).getInt("value")), true)
                    } else if (args.size == 3 && args[2].equals("kills", ignoreCase = true))
                    /*User's kill stats in CS:GO */ {
                        embedBuilder.withTitle("Map Stats for $userName")
                        embedBuilder.appendField("Knife", Integer.toString(csgoStats.getJSONObject(9).getInt("value")), true)
                        embedBuilder.appendField("Taser", Integer.toString(csgoStats.getJSONObject(166).getInt("value")), true)
                        embedBuilder.appendField("HE Grenade", Integer.toString(csgoStats.getJSONObject(10).getInt("value")), true)
                        embedBuilder.appendField("Glock", Integer.toString(csgoStats.getJSONObject(11).getInt("value")), true)
                        embedBuilder.appendField("Deagle", Integer.toString(csgoStats.getJSONObject(12).getInt("value")), true)
                        embedBuilder.appendField("USP-S", Integer.toString(csgoStats.getJSONObject(133).getInt("value")), true)
                        embedBuilder.appendField("Mac-10", Integer.toString(csgoStats.getJSONObject(16).getInt("value")), true)
                        embedBuilder.appendField("UMP-45", Integer.toString(csgoStats.getJSONObject(17).getInt("value")), true)
                        embedBuilder.appendField("AWP", Integer.toString(csgoStats.getJSONObject(19).getInt("value")), true)
                        embedBuilder.appendField("AK-47", Integer.toString(csgoStats.getJSONObject(20).getInt("value")), true)
                        embedBuilder.appendField("M4", Integer.toString(csgoStats.getJSONObject(162).getInt("value")), true)
                    } else if (args.size == 3 && args[2].equals("maps", ignoreCase = true))
                    /*User's map stats in CS:GO */ {
                        embedBuilder.withTitle("Map Wins for $userName")
                        embedBuilder.appendField("Office", Integer.toString(csgoStats.getJSONObject(28).getInt("value")), true)
                        embedBuilder.appendField("Cobble", Integer.toString(csgoStats.getJSONObject(29).getInt("value")), true)
                        embedBuilder.appendField("Dust 2", Integer.toString(csgoStats.getJSONObject(30).getInt("value")), true)
                        embedBuilder.appendField("Inferno", Integer.toString(csgoStats.getJSONObject(31).getInt("value")), true)
                        embedBuilder.appendField("Train", Integer.toString(csgoStats.getJSONObject(33).getInt("value")), true)
                        embedBuilder.appendField("Nuke", Integer.toString(csgoStats.getJSONObject(32).getInt("value")), true)
                    } else if (args.size == 3 && args[2].equals("lastmatch", ignoreCase = true))
                    /*User's last match stats in CS:GO */ {
                        embedBuilder.withTitle("$userName's last game")
                        embedBuilder.appendField("Round Wins", Integer.toString(csgoStats.getJSONObject(81).getInt("value") + csgoStats.getJSONObject(82).getInt("value")), true)
                        embedBuilder.appendField("Kills", Integer.toString(csgoStats.getJSONObject(85).getInt("value")), true)
                        embedBuilder.appendField("Deaths", Integer.toString(csgoStats.getJSONObject(86).getInt("value")), true)
                        embedBuilder.appendField("MVPs", Integer.toString(csgoStats.getJSONObject(87).getInt("value")), true)

                    } else {
                        // If none of those were selected
                        messageHandler.sendPresetError(MessageHandler.Messages.INVALID_ARGUMENTS, prefix + "steam csgo [steam name] {" +
                                "kills/maps/lastmatch}")
                        return
                    }

                    // Sends message
                    messageHandler.sendEmbed(embedBuilder)
                }
                else -> // If neither the profile or CS:GO were accessed
                    messageHandler.sendPresetError(MessageHandler.Messages.INVALID_ARGUMENTS, prefix + "steam profile [steam name]")
            }
        } catch (e1: JSONException) {
            e1.printStackTrace()
        } catch (ee: RuntimeException) {
            if (ee.message?.contains("429") != null) {
                messageHandler.sendError("Too Many Requests!")
                return
            }

            messageHandler.sendError("There was an error with that request!")
            ee.printStackTrace()
        }
    }

    override suspend fun help(e: MessageEvent) {
        val st = HashMap<String, String>()
        st["steam profile [steam name]"] = "Gets a steam user's profile."
        st["steam csgo [steam name] {kills/maps/lastmatch}"] = "Gets a steam user's CS:GO stats."
        Command.helpCommand(st, "Steam", e)
    }
}
