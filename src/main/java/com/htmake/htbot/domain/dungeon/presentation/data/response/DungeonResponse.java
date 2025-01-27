package com.htmake.htbot.domain.dungeon.presentation.data.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DungeonResponse {

    private String name;

    private List<MonsterResponse> monsterList;
}
