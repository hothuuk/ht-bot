package com.htmake.htbot.domain.dungeon.service.impl;

import com.htmake.htbot.domain.dungeon.entity.Dungeon;
import com.htmake.htbot.domain.dungeon.exception.DungeonNotFoundException;
import com.htmake.htbot.domain.dungeon.presentation.data.response.DungeonResponse;
import com.htmake.htbot.domain.dungeon.presentation.data.response.MonsterResponse;
import com.htmake.htbot.domain.dungeon.repository.DungeonRepository;
import com.htmake.htbot.domain.dungeon.service.BossDungeonEntryService;
import com.htmake.htbot.domain.inventory.entity.Inventory;
import com.htmake.htbot.domain.inventory.exception.InventoryItemNotFoundException;
import com.htmake.htbot.domain.inventory.repository.InventoryRepository;
import com.htmake.htbot.domain.monster.entity.Monster;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class BossDungeonEntryServiceImpl implements BossDungeonEntryService {

    private final DungeonRepository dungeonRepository;
    private final InventoryRepository inventoryRepository;

    @Override
    public DungeonResponse execute(String dungeonId, String playerId) {
        Dungeon dungeon = dungeonRepository.findById(dungeonId)
                .orElseThrow(DungeonNotFoundException::new);

        Inventory item = inventoryRepository.findByPlayerIdAndItemId(playerId, dungeon.getKey())
                .orElseThrow(InventoryItemNotFoundException::new);

        if (item.getQuantity() == 1) {
            inventoryRepository.delete(item);
        } else {
            item.setQuantity(item.getQuantity() - 1);
        }

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