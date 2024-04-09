package com.htmake.htbot.discord.commands.dungeon.event;

import com.htmake.htbot.discord.commands.dungeon.util.DungeonUtil;
import com.htmake.htbot.domain.dungeon.entity.Monster;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.*;
import java.util.*;
import java.util.List;

@Slf4j
public class DungeonEntryEvent {

    private final DungeonUtil dungeonUtil;

    public DungeonEntryEvent() {
        this.dungeonUtil = new DungeonUtil();
    }

    public void execute(StringSelectInteractionEvent event, List<String> componentList) {
        String dungeonId = componentList.get(1);
        String stage = componentList.get(2);

        HttpResponse<JsonNode> response = dungeonUtil.dungeonResponse(dungeonId);

        if (response.getStatus() == 200) {
            handleSuccessfulResponse(event, response, stage);
        } else {
            handleErrorResponse(event, response);
        }
    }

    private void handleSuccessfulResponse(
            StringSelectInteractionEvent event,
            HttpResponse<JsonNode> response,
            String stage
    ) {
        JSONObject dungeonObject = response.getBody().getObject();

        String dungeonName = dungeonObject.getString("name") + "-" + stage;
        JSONArray monsterArray = dungeonObject.getJSONArray("monsterList");

        ArrayList<Monster> monsterList = dungeonUtil.toMonsterList(monsterArray);

        String playerId = event.getUser().getId();
        JSONObject playerObject = dungeonUtil.playerDataResponse(playerId);
        dungeonUtil.savePlayerStatus(playerId, playerObject);

        if (playerObject == null) {
            handleErrorResponse(event, response);
        }

        Monster monster = dungeonUtil.randomMonster(monsterList, stage);
        dungeonUtil.saveMonsterStatus(playerId, monster);

        MessageEmbed embed = dungeonUtil.buildEmbed(dungeonName, monster, playerObject);

        MessageEmbed.Field field = embed.getFields().get(3);
        dungeonUtil.saveSituation(playerId, field);

        event.editMessageEmbeds(embed)
                .setActionRow(
                        Button.success("attack", "공격"),
                        Button.primary("potion-open", "포션"),
                        Button.danger("run", "후퇴")
                )
                .queue();

    }

    private void handleErrorResponse(StringSelectInteractionEvent event, HttpResponse<JsonNode> response) {
        MessageEmbed embed = new EmbedBuilder()
                .setColor(Color.ORANGE)
                .setTitle(":warning: 던전 입장")
                .setDescription("던전 입장에 실패하였습니다!")
                .build();

        event.getMessage().editMessageComponents(Collections.emptyList()).queue();
        event.editMessageEmbeds(embed).queue();

        log.error(String.valueOf(response.getBody()));
    }
}