package com.htmake.htbot.discord.commands.quest.event;

import com.htmake.htbot.global.unirest.HttpClient;
import com.htmake.htbot.global.unirest.impl.HttpClientImpl;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import kotlin.Pair;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

import java.awt.*;
import java.util.Collections;

public class QuestCompleteButtonEvent {

    private final HttpClient httpClient;

    public QuestCompleteButtonEvent() {
        this.httpClient = new HttpClientImpl();
    }

    public void execute(ButtonInteractionEvent event) {
        HttpResponse<JsonNode> response = request(event.getUser().getId());

        if (response.getStatus() == 200) {
            requestSuccess(event);
        } else {
            requestFail(event);
        }
    }

    private HttpResponse<JsonNode> request(String playerId) {
        String endPoint = "/quest/progress/{player_id}";
        Pair<String, String> routeParam = new Pair<>("player_id", playerId);
        return httpClient.sendGetRequest(endPoint, routeParam);
    }

    public void requestSuccess(ButtonInteractionEvent event) {
        MessageEmbed embed = new EmbedBuilder()
                .setColor(Color.GREEN)
                .setTitle(":tada: 퀘스트를 완료했습니다.")
                .setDescription("다음 퀘스트를 확인해 주세요.")
                .build();

        event.getMessage().editMessageComponents(Collections.emptyList()).queue();

        event.getMessage().editMessageEmbeds(embed).queue();
    }

    public void requestFail(ButtonInteractionEvent event) {
        MessageEmbed embed = new EmbedBuilder()
                .setColor(Color.ORANGE)
                .setTitle(":warning: 퀘스트를 완료하지 못했습니다.")
                .setDescription("목표를 달성하고 다시 시도해 주세요.")
                .build();

        event.getMessage().editMessageComponents(Collections.emptyList()).queue();

        event.getMessage().editMessageEmbeds(embed).queue();
    }
}
