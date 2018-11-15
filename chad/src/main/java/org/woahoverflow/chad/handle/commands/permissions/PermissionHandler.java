package org.woahoverflow.chad.handle.commands.permissions;

import org.woahoverflow.chad.core.ChadVar;
import org.woahoverflow.chad.handle.commands.Category;
import org.woahoverflow.chad.handle.commands.CommandData;
import org.woahoverflow.chad.handle.commands.Command;
import org.bson.Document;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;

import java.util.*;

@SuppressWarnings("all")
public class PermissionHandler
{
    private ArrayList<String> CMD; // arraylists are simpler, shut up
    public PermissionHandler()
    {
        //ASD POKASDKOIPASDPOKASDKJPIOOIJASD JOI
    }

    // check if the user is in the list of developers
    public boolean userIsDeveloper(IUser user) {
        return ChadVar.GLOBAL_PERMISSIONS.get(user.getStringID()) == PermissionLevels.SYSTEM_ADMINISTRATOR;
    }

    // check if the user has permission for the specified command
    public boolean userHasPermission(String command, IUser user, IGuild g)
    {
        Command cmd = ChadVar.COMMANDS.get(command).commandClass;

        if (cmd == null)
        {
            return false; // return false if the command doesnt exist
        }

        CommandData meta = ChadVar.COMMANDS.get(command);
        // developers should always have permission for developer commands
        if (meta.isDevOnly && userIsDeveloper(user))
            return true;

        // all users should have access to commands in the fun and info category
        if (meta.category == Category.FUN || meta.category == Category.INFO)
            return true;

        // loop through the users roles, if the role has permission for the command, return true
        for (IRole r : user.getRolesForGuild(g))
        {
            if (ChadVar.DATABASE_HANDLER.getArray(g, r.getStringID()) != null)
            {
                if (ChadVar.DATABASE_HANDLER.getArray(g, r.getStringID()).contains(command))
                    return true;
            }
        }
        // return false if none of the users roles have permission for the command
        return false;
    }

    // grants the specified role access to the specified command in the guild the role belongs to
    public int addCommandToRole(IRole role, String command) throws IndexOutOfBoundsException
    {
        if (!parseCommand(command))
            return 0;

        Document get = ChadVar.CACHE_DEVICE.getGuild(role.getGuild()).getDoc();
        if (get == null)
            return 1;
        ArrayList<String> arr = (ArrayList<String>)get.get(role.getStringID());
        if (arr == null || arr.isEmpty())
        {
            ArrayList<String> ar = new ArrayList<>();
            ar.add(command);
            ChadVar.DATABASE_HANDLER.getCollection().updateOne(get, new Document("$set", new Document(role.getStringID(), ar)));
            ChadVar.CACHE_DEVICE.cacheGuild(role.getGuild());
            return 6;
        }
        else {
            if (arr.contains(command))
                return 2;
            ArrayList<String> ar = arr;
            ar.add(command);
            ChadVar.DATABASE_HANDLER.getCollection().updateOne(get, new Document("$set", new Document(role.getStringID(), ar)));
            ChadVar.CACHE_DEVICE.cacheGuild(role.getGuild());
            return 6;
        }
    }

    // wadya get if you turn #addCommandToRole() upside down?
    public int removeCommandFromRole(IRole role, String command)
    {
        if (!parseCommand(command))
            return 0;

        if (ChadVar.DATABASE_HANDLER.getArray(role.getGuild(), role.getStringID()) == null)
            return 4;
        else {
            Document get = ChadVar.DATABASE_HANDLER.getCollection().find(new Document("guildid", role.getGuild().getStringID())).first();

            if (get == null)
                return 1;

            ArrayList<String> ar = ChadVar.DATABASE_HANDLER.getArray(role.getGuild(), role.getStringID());
            ar.remove(command);
            ChadVar.DATABASE_HANDLER.getCollection().updateOne(get, new Document("$set", new Document(role.getStringID(), ar)));
            return 6;
        }

    }

    // this method is poorly named as it doesnt actually parse the command. i think it checks to see if its a valid command /shrug
    private boolean parseCommand(String arg)
    {
        return ChadVar.COMMANDS.containsKey(arg.toLowerCase());
    }

    // this is only used for a select few commands, but it has its moments.
    public String parseErrorCode(int i)
    {
        if (i == 1)
        {
            return ChadVar.getString("error.internal");
        }
        else if (i == 2)
        {
            return "Command is already entered!";
        }
        else if (i == 0)
        {
            return "Invalid Command!";
        }
        else if (i == 4)
        {
            return "There's nothing to remove!";
        }
        return "An internal error has occurred!";
    }
}
