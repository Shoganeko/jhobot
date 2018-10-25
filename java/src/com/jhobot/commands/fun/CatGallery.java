package com.jhobot.commands.fun;

import com.jhobot.handle.MessageHandler;
import com.jhobot.handle.commands.Category;
import com.jhobot.handle.commands.Command;
import com.jhobot.handle.commands.HelpHandler;
import com.jhobot.handle.commands.PermissionLevels;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class CatGallery implements Command {
    @Override
    public Runnable run(MessageReceivedEvent e, List<String> args) {
        return () -> {
            MessageHandler m = new MessageHandler(e.getChannel());
            if (args.size() == 0)
            {
                File[] files = new File(System.getenv("appdata") + "\\jho\\catpictures\\").listFiles();
                if (files == null)
                {
                    m.sendError("An internal error has occurred!");
                    return;
                }
                m.sendFile(files[new Random().nextInt(files.length)]);
                return;
            }

            m.sendError("Invalid Arguments.");
        };
    }

    @Override
    public Runnable help(MessageReceivedEvent e, List<String> args) {
        HashMap<String, String> st = new HashMap<>();
        st.put("catgallery", "Gives you a random cat picture.");
        return HelpHandler.helpCommand(st, "Cat Gallery", e);
    }

    @Override
    public PermissionLevels level() {
        return PermissionLevels.MEMBER;
    }

    @Override
    public Category category() {
        return Category.FUN;
    }
}
