package org.woahoverflow.chad.commands.punishments;

import org.jetbrains.annotations.NotNull;
import org.woahoverflow.chad.framework.handle.GuildHandler;
import org.woahoverflow.chad.framework.handle.MessageHandler;
import org.woahoverflow.chad.framework.obj.Command;
import org.woahoverflow.chad.framework.obj.Guild;
import org.woahoverflow.chad.framework.obj.Guild.DataType;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageEvent;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.Permissions;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.MessageBuilder;
import sx.blah.discord.util.PermissionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Moderator tool to ban a user
 *
 * @author sho, codebasepw
 */
public class Ban implements Command.Class {

    // Patterns for the message forming
    private static final Pattern GUILD_PATTERN = Pattern.compile("&guild&");
    private static final Pattern USER_PATTERN = Pattern.compile("&user&");
    private static final Pattern REASON_PATTERN = Pattern.compile("&reason&");

    @Override
    public final Runnable run(@NotNull MessageEvent e, @NotNull List<String> args) {
        return () -> {
            MessageHandler messageHandler = new MessageHandler(e.getChannel(), e.getAuthor());

            Guild guild = GuildHandler.getGuild(e.getGuild().getLongID());

            // Checks if the bot has permission to ban
            if (!e.getClient().getOurUser().getPermissionsForGuild(e.getGuild()).contains(Permissions.BAN)) {
                messageHandler.sendPresetError(MessageHandler.Messages.BOT_NO_PERMISSION);
                return;
            }

            // Forms user from author's mentions
            IUser user;
            List<String> reason;
            if (!e.getMessage().getMentions().isEmpty() && args.get(0).contains(e.getMessage().getMentions().get(0).getStringID())) {
                user = e.getMessage().getMentions().get(0);
                args.remove(0);
                reason = args;
            }
            else {
                messageHandler.sendPresetError(MessageHandler.Messages.INVALID_USER);
                return;
            }

            // Checks if the user action upon has administrator
            if (user.getPermissionsForGuild(e.getGuild()).contains(Permissions.ADMINISTRATOR)) {
                messageHandler.sendPresetError(MessageHandler.Messages.BOT_NO_PERMISSION);
                return;
            }

            // Checks if bot has hierarchical permissions
            if (!PermissionUtils.hasHierarchicalPermissions(e.getChannel(), e.getClient().getOurUser(), user, Permissions.BAN)) {
                messageHandler.sendPresetError(MessageHandler.Messages.BOT_NO_PERMISSION);
                return;
            }

            // Checks if user has hierarchical permissions
            if (!PermissionUtils.hasHierarchicalPermissions(e.getChannel(), e.getClient().getOurUser(), user, Permissions.BAN)) {
                messageHandler.sendPresetError(MessageHandler.Messages.USER_NO_PERMISSION);
                return;
            }

            // Builds reason
            StringBuilder builtReason = new StringBuilder();
            if (!reason.isEmpty())
                for (String s : reason)
                    builtReason.append(s).append(' ');
            else
                builtReason.append("no reason");

            // Checks if ban message is enabled
            if ((boolean) guild.getObject(DataType.BAN_MESSAGE_ON)) {
                // Gets the message from the cache
                String message = (String) guild.getObject(Guild.DataType.BAN_MESSAGE);

                // If the message isn't null, continue
                String formattedMessage = GUILD_PATTERN.matcher(message).replaceAll(e.getGuild().getName()); // replaces &guild& with guild's name
                formattedMessage = USER_PATTERN.matcher(formattedMessage).replaceAll(user.getName()); // replaces &user& with user's name
                formattedMessage = REASON_PATTERN.matcher(formattedMessage).replaceAll(builtReason.toString().trim()); // replaces &reason& with the reason

                // If the user isn't bot, send the message.
                if (!user.isBot())
                    new MessageBuilder(e.getClient()).withChannel(e.getClient().getOrCreatePMChannel(user)).withContent(formattedMessage).build();
            }

            // If there's no reason, continue with "no reason"
            if (reason.isEmpty()) {
                e.getGuild().banUser(user);
                reason.add("None");
                messageHandler.sendEmbed(new EmbedBuilder().appendDesc("Successfully banned " + user.getName() + " for no reason."));
                MessageHandler.Companion.sendPunishLog("Ban", user, e.getAuthor(), e.getGuild(), reason);
                return;
            }

            // Bans the user.
            e.getGuild().banUser(user);
            messageHandler.sendEmbed(new EmbedBuilder().withDesc("Successfully banned " + user.getName() + " for " + builtReason.toString().trim() + '.'));
            MessageHandler.Companion.sendPunishLog("Ban", user, e.getAuthor(), e.getGuild(), reason);
        };
    }

    @Override
    public final Runnable help(@NotNull MessageEvent e) {
        HashMap<String, String> st = new HashMap<>();
        st.put("ban <@user>", "Bans a user with no reason.");
        st.put("ban <@user> <reason>", "Bans a user with a specified reason.");
        return Command.helpCommand(st, "User Info", e);
    }
}
