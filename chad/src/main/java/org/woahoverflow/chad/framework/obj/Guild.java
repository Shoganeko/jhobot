package org.woahoverflow.chad.framework.obj;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import org.woahoverflow.chad.core.ChadBot;
import org.woahoverflow.chad.core.ChadVar;
import org.woahoverflow.chad.framework.handle.database.DatabaseManager;

public class Guild
{

    /**
     * The guild's ID
     */
    private final long guild;

    /**
     * The types of data you can request from a guild.
     */
    public enum DataType
    {
        // Main
        PREFIX,

        // Logging
        LOGGING, LOGGING_CHANNEL,

        // Messages
        JOIN_MESSAGE_ON, LEAVE_MESSAGE_ON, JOIN_MESSAGE_CHANNEL, LEAVE_MESSAGE_CHANNEL, BAN_MESSAGE_ON, KICK_MESSAGE_ON,
        BAN_MESSAGE, KICK_MESSAGE, JOIN_MESSAGE, LEAVE_MESSAGE,

        // Join Role
        ROLE_ON_JOIN, JOIN_ROLE,

        // Swearing
        SWEAR_FILTER, SWEAR_FILTER_MESSAGE,

        // Statistics
        MESSAGES_SENT, COMMANDS_SENT,

        // Other
        ALLOW_COMMUNITY_FEATURES
    }

    /**
     * The guild's full set of data
     */
    private ConcurrentHashMap<DataType, Object> guildData = new ConcurrentHashMap<>();

    /**
     * The guild's full set of permission data
     */
    private final ConcurrentHashMap<Long, ArrayList<String>> permissionData = new ConcurrentHashMap<>();

    /**
     * The amount of messages sent within the guild
     */
    private long messagesSent;

    /**
     * The amount of commands sent within the guild
     */
    private long commandsSent;

    /**
     * Default Constructor, sets it with default data.
     */
    public Guild(long guild)
    {
        this.guild = guild;

        // Main
        guildData.put(DataType.PREFIX, "c!");

        // Logging
        guildData.put(DataType.LOGGING, false);
        guildData.put(DataType.LOGGING_CHANNEL, "none");

        // Statistics
        guildData.put(DataType.MESSAGES_SENT, 0L);
        guildData.put(DataType.COMMANDS_SENT, 0L);

        messagesSent = 0L;
        commandsSent = 0L;

        // Messages
        guildData.put(DataType.JOIN_MESSAGE, "`&user&` has joined the server!");
        guildData.put(DataType.JOIN_MESSAGE_ON, false);
        guildData.put(DataType.JOIN_MESSAGE_CHANNEL, "none");

        guildData.put(DataType.LEAVE_MESSAGE, "`&user` has left the server!");
        guildData.put(DataType.LEAVE_MESSAGE_ON, false);
        guildData.put(DataType.LEAVE_MESSAGE_CHANNEL, "none");

        guildData.put(DataType.BAN_MESSAGE, "`&user& has been banned for `&reason&`!");
        guildData.put(DataType.KICK_MESSAGE, "`&user& has been kicked for `&reason&`!");

        guildData.put(DataType.BAN_MESSAGE_ON, true);
        guildData.put(DataType.KICK_MESSAGE_ON, true);

        // Join Role
        guildData.put(DataType.JOIN_ROLE, "none");
        guildData.put(DataType.ROLE_ON_JOIN, false);

        // Swearing
        guildData.put(DataType.SWEAR_FILTER, false);
        guildData.put(DataType.SWEAR_FILTER_MESSAGE, "Stop swearing, `&user&`!");

        // Other
        guildData.put(DataType.ALLOW_COMMUNITY_FEATURES, true);
    }

    /**
     * Sets a guild with existing values
     *
     * @param guildData The preset values
     */
    public Guild(ConcurrentHashMap<DataType, Object> guildData, long guild)
    {
        this.guildData = guildData;
        this.guild = guild;

        messagesSent = (long) guildData.get(DataType.MESSAGES_SENT);
        commandsSent = (long) guildData.get(DataType.COMMANDS_SENT);
    }

    /**
     * Sets a data type's value.
     *
     * @param dataType The data type to set
     * @param value The value to set it to
     */
    public void setObject(DataType dataType, Object value)
    {
        guildData.put(dataType, value);

        DatabaseManager.GUILD_DATA.setObject(guild, dataType.toString().toLowerCase(), value);
    }

    /**
     * Gets data from a data type.
     *
     * @param dataType The data type to retrieve
     * @return The retrieved data
     */
    public Object getObject(DataType dataType)
    {
        return guildData.get(dataType);
    }

    /**
     * Gets data from a string, directly from the database.
     *
     * @param dataType The data's key
     * @return The retrieved data
     */
    public Object getObject(String dataType)
    {
        return DatabaseManager.USER_DATA.getObject(guild, dataType);
    }

    /**
     * Gets the guild's ID
     *
     * @return The guild's ID
     */
    public long getGuildID()
    {
        return guild;
    }

    /**
     * Gets the permissions for a role
     *
     * @param role The role to get permissions for
     * @return The role's permissions
     */
    @SuppressWarnings("unchecked")
    public ArrayList<String> getRolePermissions(long role)
    {
        // If the data's in the permission hash-map, return that
        if (permissionData.containsKey(role))
            return permissionData.get(role);

        // Get the permissions from the database
        Object permissions = DatabaseManager.GUILD_DATA.getObject(guild, Long.toString(role));

        // If it doesn't exist
        if (permissions == null)
        {
            ArrayList<String> permissionSet = new ArrayList<>();

            // Put it in local storage
            permissionData.put(role, permissionSet);

            // Put it in the database
            DatabaseManager.GUILD_DATA.setObject(guild, Long.toString(role), permissionSet);

            return permissionSet;
        }

        // The permission set
        ArrayList<String> permissionSet;

        // Try to cast it
        try {
            permissionSet = (ArrayList<String>) permissions;
        } catch (ClassCastException castException)
        {
            // If it for some reason doesn't cast properly
            ChadBot.getLogger().error("Permission set failed to cast to an array-list!", castException);
            return new ArrayList<>();
        }

        // Add it to the local storage
        permissionData.put(role, permissionSet);

        // Return the retrieved set
        return permissionSet;
    }

    /**
     * Adds a permission to a role
     *
     * @param role The role to add to
     * @param command The command to add
     * @return The error/success code
     */
    public int addPermissionToRole(long role, String command)
    {
        // Get the role's permissions
        ArrayList<String> permissionSet = getRolePermissions(role);

        // Make sure the command is an actual command
        if (!ChadVar.COMMANDS.containsKey(command))
            return 3;

        // Make sure it doesn't already have that permission
        if (permissionSet.contains(command))
            return 1;

        // Add it
        permissionSet.add(command);

        // Re-add to hashmap
        permissionData.put(role, permissionSet);

        // Re-add to database
        DatabaseManager.GUILD_DATA.setObject(guild, Long.toString(role), permissionSet);

        return 0;
    }

    /**
     * Removes a permission to role
     *
     * @param role The role to remove from
     * @param command The command to remove
     * @return The error/success code
     */
    public int removePermissionFromRole(long role, String command)
    {
        // Get the role's permissions
        ArrayList<String> permissionSet = getRolePermissions(role);

        // Make sure the command is an actual command
        if (!ChadVar.COMMANDS.containsKey(command))
            return 3;

        // If there's no commands at all
        if (permissionSet.isEmpty())
            return 4;

        // Makes sure the role actually has the command
        if (!permissionSet.contains(command))
            return 1;

        // Remove it
        permissionSet.remove(command);

        // Re-add to hashmap
        permissionData.put(role, permissionSet);

        // Re-add to database
        DatabaseManager.GUILD_DATA.setObject(guild, Long.toString(role), permissionSet);

        return 0;
    }

    /**
     * Updates message sent statistics
     */
    public void messageSent()
    {
        messagesSent++;
        guildData.put(DataType.MESSAGES_SENT, messagesSent);
    }

    /**
     * Updates command sent statistics
     */
    public void commandSent()
    {
        commandsSent++;
        guildData.put(DataType.COMMANDS_SENT, commandsSent);
    }

    /**
     * Clears the guild's statistics
     */
    public void clearStatistics()
    {
        messagesSent = 0L;
        commandsSent = 0L;
        updateStatistics();
    }

    /**
     * Updates the statistics into the database
     */
    public void updateStatistics()
    {
        setObject(DataType.COMMANDS_SENT, commandsSent);
        setObject(DataType.MESSAGES_SENT, messagesSent);
    }
}