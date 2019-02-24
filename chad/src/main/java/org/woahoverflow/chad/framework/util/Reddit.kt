package org.woahoverflow.chad.framework.util

import org.json.JSONException
import org.json.JSONObject
import org.woahoverflow.chad.framework.handle.JsonHandler
import org.woahoverflow.chad.framework.handle.MessageHandler
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageEvent
import sx.blah.discord.util.EmbedBuilder
import kotlin.random.Random

/**
 * This class is all of the utility related to Reddit
 * We, woahoverflow, are not responsible for any content retrieved for woahoverflow. They may be offensive or innapropriate.
 *
 * @author sho
 */
class Reddit {
    fun sendHotPost(e: MessageEvent, subreddit: String) {
        val messageHandler = MessageHandler(e.channel, e.author)
        val subredditJson: JSONObject?
        var post: JSONObject?
        try {
            // Gets post
            subredditJson = JsonHandler.handle.read("https://reddit.com/r/$subreddit/hot.json")

            // If it's not found
            if (subredditJson == null) {
                messageHandler.sendError("Invalid Subreddit")
                return
            }

            // If there's no posts in the subreddit
            if (subredditJson.getJSONObject("data").getJSONArray("children").isEmpty) {
                messageHandler.sendError("Invalid Subreddit")
                return
            }

            // Gets a random post in the subreddit
            var index = Random.nextInt(subredditJson.getJSONObject("data").getJSONArray("children").length())

            // The amount of posts parsed through
            var parsed = 0

            // Gets the subreddit
            parsed++
            post = subredditJson.getJSONObject("data")
                    .getJSONArray("children")
                    .getJSONObject(index)
                    .getJSONObject("data")


            // Makes sure the post isn't stickied or if the post is NSFW
            while (post!!.getBoolean("stickied") || (post.getBoolean("over_18") && !e.channel.isNSFW)) {

                if (subredditJson.getJSONObject("data").getJSONArray("children").length() == parsed) {
                    messageHandler.sendError("Failed to find a post!")
                    return
                }

                index = Random.nextInt(subredditJson.getJSONObject("data").getJSONArray("children").length())
                parsed++
                post = subredditJson
                        .getJSONObject("data")
                        .getJSONArray("children")
                        .getJSONObject(index)
                        .getJSONObject("data")
            }
        } catch (e1: JSONException) {
            e1.printStackTrace()
            messageHandler.sendError("Invalid Subreddit")
            return
        } catch (e1: RuntimeException) {
            messageHandler.sendError("Invalid Subreddit")
            return
        }

        val embedBuilder = EmbedBuilder()
        embedBuilder.withTitle(post.getString("title"))
        embedBuilder.withDesc(post.getString("author"))
        embedBuilder.appendField("Score", post.getInt("score").toString(), true)
        embedBuilder.appendField("Comments", Integer.toString(post.getInt("num_comments")), true)
        embedBuilder.withImage(post.getString("url"))
        embedBuilder.withUrl("https://reddit.com" + post.getString("permalink"))
        messageHandler.credit("r/$subreddit").sendEmbed(embedBuilder)
    }

    fun sendNewPost(e: MessageEvent, subreddit: String) {
        val messageHandler = MessageHandler(e.channel, e.author)
        val subredditJson: JSONObject?
        var post: JSONObject?
        try {
            // Gets post
            subredditJson = JsonHandler.handle.read("https://reddit.com/r/$subreddit/new.json")

            // If it's not found
            if (subredditJson == null) {
                messageHandler.sendError("Invalid Subreddit")
                return
            }

            // If there's no posts in the subreddit
            if (subredditJson.getJSONObject("data").getJSONArray("children").isEmpty) {
                messageHandler.sendError("Invalid Subreddit")
                return
            }

            // Gets a random post in the subreddit
            var index = Random.nextInt(subredditJson.getJSONObject("data").getJSONArray("children").length())

            // The amount of posts parsed through
            var parsed = 0

            // Gets the subreddit
            parsed++
            post = subredditJson.getJSONObject("data")
                    .getJSONArray("children")
                    .getJSONObject(index)
                    .getJSONObject("data")


            // Makes sure the post isn't stickied or if the post is NSFW
            while (post!!.getBoolean("stickied") || (post.getBoolean("over_18") && !e.channel.isNSFW)) {

                if (subredditJson.getJSONObject("data").getJSONArray("children").length() == parsed) {
                    messageHandler.sendError("Failed to find a post!")
                    return
                }

                index = Random.nextInt(subredditJson.getJSONObject("data").getJSONArray("children").length())
                parsed++
                post = subredditJson
                        .getJSONObject("data")
                        .getJSONArray("children")
                        .getJSONObject(index)
                        .getJSONObject("data")
            }
        } catch (e1: JSONException) {
            e1.printStackTrace()
            messageHandler.sendError("Invalid Subreddit")
            return
        } catch (e1: RuntimeException) {
            messageHandler.sendError("Invalid Subreddit")
            return
        }

        val embedBuilder = EmbedBuilder()
        embedBuilder.withTitle(post.getString("title"))
        embedBuilder.withDesc(post.getString("author"))
        embedBuilder.appendField("Score", post.getInt("score").toString(), true)
        embedBuilder.appendField("Comments", Integer.toString(post.getInt("num_comments")), true)
        embedBuilder.withImage(post.getString("url"))
        embedBuilder.withUrl("https://reddit.com" + post.getString("permalink"))
        messageHandler.credit("r/$subreddit").sendEmbed(embedBuilder)
    }
}