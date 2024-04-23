package com.htmake.htbot.discord.commands.battle;

import com.htmake.htbot.discord.commands.battle.event.PlayerAttackButtonEvent;
import com.htmake.htbot.discord.commands.battle.event.BattlePotionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BattleCommand extends ListenerAdapter {

    private final PlayerAttackButtonEvent playerAttackButtonEvent;

    public BattleCommand() {
        this.playerAttackButtonEvent = new PlayerAttackButtonEvent();
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        List<String> componentList = List.of(event.getComponentId().split("-"));

        if (componentList.get(0).equals("battle")) {
            if (componentList.get(1).equals("attack")) {
                playerAttackButtonEvent.execute(event);
            } else if (componentList.get(1).equals("potion")) {
                new BattlePotionEvent(event, componentList.get(2));
            }
        }
    }
}