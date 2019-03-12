package org.woahoverflow.chad.commands.fun;

import org.jetbrains.annotations.NotNull;
import org.woahoverflow.chad.framework.handle.JsonHandler;
import org.woahoverflow.chad.framework.handle.MessageHandler;
import org.woahoverflow.chad.framework.obj.Command;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageEvent;
import sx.blah.discord.util.EmbedBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * Gets random cat pictures from an API
 *
 * @author sho
 */
public class CatGallery implements Command.Class  {
    @Override
    public final Runnable run(@NotNull MessageEvent e, @NotNull List<String> args) {
        return () -> {
            MessageHandler messageHandler = new MessageHandler(e.getChannel(), e.getAuthor());

            // The embed builder
            EmbedBuilder embedBuilder = new EmbedBuilder();

            // The API we use for our cat images :)
            String url = "https://api.thecatapi.com/v1/images/search?size=full";

            embedBuilder.withImage(
                Objects.requireNonNull(JsonHandler.INSTANCE.readArray(url)).getJSONObject(0).getString("url")
            );

            messageHandler.credit("thecatapi.com").sendEmbed(embedBuilder);
        };
    }

    @Override
    public final Runnable help(@NotNull MessageEvent e) {
        HashMap<String, String> st = new HashMap<>();
        st.put("catgallery", "Gives you a random cat picture.");
        return Command.helpCommand(st, "Cat Gallery", e);
    }
}
