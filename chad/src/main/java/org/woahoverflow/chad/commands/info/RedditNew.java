package org.woahoverflow.chad.commands.info;

import org.json.JSONException;
import org.json.JSONObject;
import org.woahoverflow.chad.framework.Command;
import org.woahoverflow.chad.framework.handle.JSONHandler;
import org.woahoverflow.chad.framework.handle.MessageHandler;
import org.woahoverflow.chad.framework.ui.ChadError;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.util.EmbedBuilder;

import java.util.HashMap;
import java.util.List;

/**
 * @author sho, codebasepw
 * @since 0.6.3 B2
 */
public class RedditNew implements Command.Class{
    @Override
    public final Runnable run(MessageReceivedEvent e, List<String> args) {
        return() -> {
            MessageHandler messageHandler = new MessageHandler(e.getChannel());

            // If there's no arguments
            if (args.isEmpty())
            {
                messageHandler.sendError(MessageHandler.INVALID_ARGUMENTS);
                return;
            }

            if (args.get(0).length() < 3)
            {
                messageHandler.sendError("Invalid Subreddit");
                return;
            }

            // Gets a new post from the selected sub-reddit
            JSONObject post;
            try {
                // Gets post
                post = JSONHandler.handle.read("https://reddit.com/r/" + args.get(0) + "/new.json");

                if (post == null)
                {
                    messageHandler.sendError("Invalid Subreddit");
                    return;
                }

                if (JSONHandler.handle.read("https://reddit.com/r/" + args.get(0) + "/new.json")
                    .getJSONObject("data")
                    .getJSONArray("children").isEmpty())
                {
                    messageHandler.sendError("Invalid Subreddit");
                    return;
                }

                int index = 0;
                post = JSONHandler.handle.read("https://reddit.com/r/" + args.get(0) + "/new.json").getJSONObject("data")
                    .getJSONArray("children")
                    .getJSONObject(index)
                    .getJSONObject("data");


                // Makes sure the post isn't stickied
                while (post.getBoolean("stickied"))
                {
                    index++;
                    post = JSONHandler.handle.read("https://reddit.com/r/" + args.get(0) + "/new.json")
                        .getJSONObject("data")
                        .getJSONArray("children")
                        .getJSONObject(index)
                        .getJSONObject("data");
                }
            } catch (JSONException e1) {
                ChadError
                    .throwError("Error with RedditNew in guild " + e.getGuild().getStringID(), e1);
                return;
            } catch (RuntimeException e1) {
                new MessageHandler(e.getChannel()).sendError("Invalid subreddit.");
                return;
            }

            // If the post is over 18 and the channel isn't Nsfw, deny.
            if (post.getBoolean("over_18") && !e.getChannel().isNSFW())
            {
                messageHandler.sendError(MessageHandler.CHANNEL_NOT_NSFW);
                return;
            }

            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.withTitle(post.getString("title"));
            embedBuilder.withDesc(post.getString("author"));
            embedBuilder.appendField("Score", post.getInt("score") + " (" + post.getInt("ups") + '/'
                + post.getInt("downs") + ')', true);
            embedBuilder.appendField("Comments", Integer.toString(post.getInt("num_comments")), true);
            embedBuilder.withImage(post.getString("url"));
            embedBuilder.withUrl("https://reddit.com" + post.getString("permalink"));
            messageHandler.sendEmbed(embedBuilder);
        };
    }

    @Override
    public final Runnable help(MessageReceivedEvent e) {
        HashMap<String, String> st = new HashMap<>();
        st.put("rnew <subreddit>", "Displays the most recent post from a subreddit.");
        return Command.helpCommand(st, "Reddit New", e);
    }
}
