package com.htmake.htbot.domain.dungeon.service.impl;

import com.htmake.htbot.domain.dungeon.entity.Monster;
import com.htmake.htbot.domain.dungeon.presentation.data.response.MonsterLootResponse;
import com.htmake.htbot.domain.dungeon.repository.MonsterRepository;
import com.htmake.htbot.domain.dungeon.service.MonsterLootService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MonsterLootServiceImpl implements MonsterLootService {

    private final MonsterRepository monsterRepository;

    @Override
    public MonsterLootResponse execute(String monsterName) {

        String decodedName = URLDecoder.decode(monsterName, StandardCharsets.UTF_8).replaceFirst(" ", "");

        Monster monster = monsterRepository.findByName(decodedName)
                .orElseThrow();

        String exp = String.valueOf(monster.getExp());
        String gold = String.valueOf(monster.getGold());

        return MonsterLootResponse.builder()
                .exp(exp)
                .gold(gold)
                .build();
    }
}
