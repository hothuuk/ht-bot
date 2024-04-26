package com.htmake.htbot.discord.commands.battle.event;

import com.htmake.htbot.discord.commands.battle.action.MonsterAttackAction;
import com.htmake.htbot.discord.commands.battle.action.MonsterKillAction;
import com.htmake.htbot.global.cache.CacheFactory;
import com.htmake.htbot.discord.commands.battle.cache.MonsterStatusCache;
import com.htmake.htbot.discord.commands.battle.cache.PlayerStatusCache;
import com.htmake.htbot.discord.commands.battle.data.MonsterStatus;
import com.htmake.htbot.discord.commands.battle.data.PlayerStatus;
import com.htmake.htbot.discord.commands.battle.util.BattleUtil;
import kotlin.Pair;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

public class PlayerAttackButtonEvent {

    private final BattleUtil battleUtil;
    private final MonsterKillAction monsterKillAction;
    private final MonsterAttackAction monsterAttackAction;

    private final PlayerStatusCache playerStatusCache;
    private final MonsterStatusCache monsterStatusCache;

    public PlayerAttackButtonEvent() {
        this.battleUtil = new BattleUtil();
        this.monsterKillAction = new MonsterKillAction();
        this.monsterAttackAction = new MonsterAttackAction();

        this.playerStatusCache = CacheFactory.playerStatusCache;
        this.monsterStatusCache = CacheFactory.monsterStatusCache;
    }

    public void execute(ButtonInteractionEvent event) {
        String playerId = event.getUser().getId();
        PlayerStatus playerStatus = playerStatusCache.get(playerId);
        MonsterStatus monsterStatus = monsterStatusCache.get(playerId);

        playerTurn(event, playerStatus, monsterStatus);

        if (monsterStatus.getHealth() == 0) {
            monsterKillAction.execute(event, playerStatus, monsterStatus);
        } else {
            monsterAttackAction.execute(event, playerStatus, monsterStatus);
        }
    }

    private void playerTurn(ButtonInteractionEvent event, PlayerStatus playerStatus, MonsterStatus monsterStatus) {
        Pair<Integer, Boolean> damage = playerAttackDamage(playerStatus, monsterStatus);

        User user = event.getUser();
        String playerId = user.getId();
        String name = user.getName();

        String message = name + "의 ";

        if (damage.getSecond()) {
            message += "치명타 공격!";
        } else {
            message += "공격.";
        }

        battleUtil.updateSituation(playerId, message);

        monsterStatus.setHealth(Math.max(0, (monsterStatus.getHealth() - damage.getFirst())));

        message = damage.getFirst() + "의 데미지를 입혔다!";
        battleUtil.updateSituation(playerId, message);
    }

    private Pair<Integer, Boolean> playerAttackDamage(PlayerStatus playerStatus, MonsterStatus monsterStatus) {
        RandomGenerator random = new MersenneTwister();
        int randomNum = random.nextInt(100);

        int damage = playerStatus.getDamage();
        int criticalChance = playerStatus.getCriticalChance();
        int criticalDamage = playerStatus.getCriticalDamage();

        int monsterDefence = monsterStatus.getDefence();

        if (randomNum < criticalChance) {
            double criticalDamageMultiple = (double) criticalDamage / 100;
            damage = (int) (damage * criticalDamageMultiple) - monsterDefence;

            return new Pair<>(Math.max(damage, 0), true);
        }

        damage -= monsterDefence;
        return new Pair<>(Math.max(damage, 0), false);
    }
}
