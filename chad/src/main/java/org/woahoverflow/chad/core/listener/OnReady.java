package org.woahoverflow.chad.core.listener;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;
import org.woahoverflow.chad.core.ChadBot;
import org.woahoverflow.chad.core.ChadVar;
import org.woahoverflow.chad.framework.Chad;
import org.woahoverflow.chad.framework.ui.UIHandler;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.obj.ActivityType;

import sx.blah.discord.util.RequestBuffer;

/**
 * The on ready event from Discord
 *
 * @author sho, codebasepw
 * @since 0.6.3 B2
 */
public final class OnReady
{

    /**
     * Discord Ready Event
     *
     * @param event Discord Ready Event
     */
    @EventSubscriber
    @SuppressWarnings("unused")
    public void onReadyEvent(ReadyEvent event)
    {
        // Presence Rotations
        Chad.runThread(() -> {

            // If the presence rotation is disabled, return
            if (!ChadVar.rotatePresence)
                return;

            // Rotation Values
            Object[] ar = ChadVar.presenceRotation.toArray();

            // Sets the new status
            ChadVar.currentStatus = (String) ar[new SecureRandom().nextInt(ar.length)];

            // Changes the discord presence
            RequestBuffer.request(() -> event.getClient().changePresence(ChadVar.statusType, ActivityType.PLAYING, ChadVar.currentStatus));

            // The time between the while loop
            int time = 0;

            while (true)
            {
                // Adds 10 seconds
                time += 10;

                // Waits 10 seconds
                try {
                    TimeUnit.SECONDS.sleep(10);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }

                // If it's been the amount of seconds in the rotation time
                if (time == ChadVar.rotationInteger)
                {
                    // If presence rotation is disabled
                    if (!ChadVar.rotatePresence)
                        return;

                    // Sets the new status
                    ChadVar.currentStatus = (String) ar[new SecureRandom().nextInt(ar.length)];

                    // Changes the discord presence
                    RequestBuffer.request(() -> event.getClient().changePresence(ChadVar.statusType, ActivityType.PLAYING, ChadVar.currentStatus));

                    // Resets time to 0
                    time = 0;
                }
            }
        }, Chad.getInternalConsumer());

        // UI Begin
        if (ChadVar.launchOptions.get("-denyui"))
        {
            ChadBot.getLogger().info("Bot started with {} guilds!", event.getClient().getGuilds().size());
        }
        else {
            UIHandler.handle.addLog("Bot started with " + event.getClient().getGuilds().size() + " guilds!", UIHandler.LogLevel.INFO);
            UIHandler.handle.update();
        }
    }
}
