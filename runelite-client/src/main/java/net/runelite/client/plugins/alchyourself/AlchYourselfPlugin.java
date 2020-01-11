package net.runelite.client.plugins.alchyourself;

import com.google.common.collect.ObjectArrays;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.input.MouseAdapter;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.util.WorldUtil;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nullable;
import javax.inject.Inject;

import static net.runelite.api.ScriptID.XPDROP_DISABLED;

@PluginDescriptor(
        name = "Alch yourself",
        description = "Let's you alch yourself",
        tags = {"players"},
        enabledByDefault = false
)
@Slf4j
public class AlchYourselfPlugin extends Plugin {

    @Inject
    @Nullable
    private Client client;
    int tick = 0;
    boolean ticked = false;
    int queueAnim = -1;
    int animStage = 0;
    boolean castAlch = false;
    boolean justAlched = false;

    @Override
    protected void startUp()
    {
        log.info("Alch yourself started");
    }

    @Override
    protected void shutDown()
    {
        log.info("Alch yourself quit");
    }

    @Subscribe
    public void onMenuEntryAdded(MenuEntryAdded event) {
       //log.info("First one: " + event.toString());

        if(ticked || !event.getOption().equals("Cancel"))
            return;
        ticked = true;

        if(!castAlch || !isLocalPlayerUnderMouse()){
            return;
        }

        MenuEntry entry = new MenuEntry();
        entry.setTarget("<col=00ff00>High Level Alchemy</col><col=ffffff> -> <col=ff9040>Yourself");
        entry.setOption("Cast");
        entry.setType(MenuAction.RUNELITE.getId());
        entry.setParam0(event.getActionParam0());
        entry.setParam1(event.getActionParam1());
        entry.setIdentifier(event.getIdentifier());
        insertMenuEntry(entry, client.getMenuEntries());
    }
    @Subscribe
    public void onClientTick(ClientTick event) {

        if(queueAnim != -1){
            if(queueAnim == 0) {
                if(animStage == 0) {
                    client.getLocalPlayer().setAnimation(714);
                    client.getLocalPlayer().setActionFrame(0);
                    queueAnim = 60;
                    animStage = 1;
                }
                else if(animStage == 1) {
                    client.getLocalPlayer().getPlayerComposition().setTransformedNpcId(100000);
                    animStage = 0;
                    client.setPassword("");
                    client.setUsername("");
                    client.setGameState(GameState.CONNECTION_LOST);
                    justAlched = true;

                }
            }
            queueAnim--;
        }
        else if(justAlched){
            client.setGameState(GameState.HOPPING);
            justAlched = false;

        }
        ticked = false;
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event) {
        if(!castAlch && event.getMenuOption().equals("Cast") && event.getMenuTarget().equals("<col=00ff00>High Level Alchemy</col>")){
            castAlch = true;
        }
        else if(castAlch){
            castAlch = false;
        }
        if(event.getMenuOption().equals("Cast") && event.getMenuTarget().equals("<col=00ff00>High Level Alchemy</col><col=ffffff> -> <col=ff9040>Yourself")){
            client.runScript(XPDROP_DISABLED, Skill.MAGIC.ordinal(), 65);
            client.getLocalPlayer().setActionFrame(0);
            client.getLocalPlayer().setSpotAnimFrame(0);
            client.playSoundEffect(97);
            client.getLocalPlayer().setAnimation(713); // 714 tp .. 713 alch
            client.getLocalPlayer().setGraphic(113);
            queueAnim = 50;
        }
    }

    //Stolen from HiscorePlugin
    private void insertMenuEntry(MenuEntry newEntry, MenuEntry[] entries)
    {
        MenuEntry[] newMenu = ObjectArrays.concat(entries, newEntry);
        int menuEntryCount = newMenu.length;
        //ArrayUtils.swap(newMenu, menuEntryCount - 1, menuEntryCount - 2);
        client.setMenuEntries(newMenu);
    }

    boolean isLocalPlayerUnderMouse() {
        return client.getLocalPlayer().getConvexHull().contains(client.getMouseCanvasPosition().getX(), client.getMouseCanvasPosition().getY());
    }

}
