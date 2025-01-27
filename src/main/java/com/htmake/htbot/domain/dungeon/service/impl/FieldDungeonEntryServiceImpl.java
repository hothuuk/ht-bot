package com.htmake.htbot.domain.dungeon.service.impl;

import com.htmake.htbot.domain.dungeon.entity.Dungeon;
import com.htmake.htbot.domain.dungeon.exception.DungeonNotFoundException;
import com.htmake.htbot.domain.dungeon.presentation.data.response.DungeonResponse;
import com.htmake.htbot.domain.dungeon.presentation.data.response.MonsterResponse;
import com.htmake.htbot.domain.dungeon.repository.DungeonRepository;
import com.htmake.htbot.domain.dungeon.service.FieldDungeonEntryService;
import com.htmake.htbot.domain.monster.entity.Monster;
import com.htmake.htbot.global.annotation.ReadOnlyService;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@ReadOnlyService
@RequiredArgsConstructor
public class FieldDungeonEntryServiceImpl implements FieldDungeonEntryService {

    private final DungeonRepository dungeonRepository;

    @Override
    public DungeonResponse execute(String dungeonId) {
        Dungeon dungeon = dungeonRepository.findById(dungeonId)
                .orElseThrow(DungeonNotFoundException::new);

        List<Monster> monsterList = dungeon.getMonsterList();

        return DungeonResponse.builder()
                .name(dungeon.getName())
                .monsterList(
                        monsterList.stream()
                                .map(MonsterResponse::toResponse)
                                .collect(Collectors.toList())
                )
                .build();
    }
}
