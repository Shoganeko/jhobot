package org.woahoverflow.chad.commands.`fun`

import org.woahoverflow.chad.framework.handle.MessageHandler
import org.woahoverflow.chad.framework.handle.PostType
import org.woahoverflow.chad.framework.handle.getPost
import org.woahoverflow.chad.framework.obj.Command
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageEvent
import sx.blah.discord.util.EmbedBuilder
import java.util.*

/**
 * Gets a meme from a randomly selected subreddit
 * (don't get offended by any of the subreddits or their contents, as it's not my material)
 *
 * @author sho
 */
class Meme : Command.Class {
    override fun help(e: MessageEvent): Runnable {
        val st = HashMap<String, String>()
        st["meme"] = "Get a meme from random subreddits."
        return Command.helpCommand(st, "Meme", e)
    }

    override fun run(e: MessageEvent, args: MutableList<String>): Runnable {
        return Runnable {
            // Picks a subreddit out of the list, and sends it
            val subreddits = arrayListOf("blackpeopletwitter", "memes", "dankmemes", "me_irl", "2meirl4meirl", "cursedimages", "wholesomememes", "pewdiepiesubmissions", "terriblefacebookmemes", "memeeconomy")

            val post = getPost(subreddits, PostType.HOT)!!.getJSONObject("data")

            val embedBuilder = EmbedBuilder()

            embedBuilder.withUrl("https://reddit.com" + post.getString("permalink"))
            embedBuilder.withTitle(post.getString("title"))
            embedBuilder.withDesc("**Vote**: ${post.getLong("ups")} / **Comments**: ${post.getLong("num_comments")}")
            embedBuilder.withImage(post.getString("url"))

            MessageHandler(e.channel, e.author).credit(post.getString("subreddit_name_prefixed")).sendEmbed(embedBuilder)
        }
    }
}