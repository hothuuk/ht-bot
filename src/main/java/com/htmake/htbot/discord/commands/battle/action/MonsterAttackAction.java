package com.htmake.htbot.discord.commands.battle.action;

import com.htmake.htbot.discord.commands.battle.data.MonsterData;
import com.htmake.htbot.discord.commands.battle.data.MonsterSkillData;
import com.htmake.htbot.discord.commands.battle.data.PlayerData;
import com.htmake.htbot.discord.commands.battle.data.status.extend.MonsterOriginalStatus;
import com.htmake.htbot.discord.commands.battle.data.status.extend.MonsterStatus;
import com.htmake.htbot.discord.commands.battle.data.status.extend.PlayerOriginalStatus;
import com.htmake.htbot.discord.commands.battle.data.status.extend.PlayerStatus;
import com.htmake.htbot.discord.commands.battle.util.BattleUtil;
import com.htmake.htbot.discord.skillAction.condition.Condition;
import com.htmake.htbot.discord.skillAction.condition.extend.damageOverTime.extend.Toxic;
import com.htmake.htbot.discord.skillAction.condition.extend.etc.Faint;
import com.htmake.htbot.discord.skillAction.condition.extend.buff.Buff;
import com.htmake.htbot.discord.skillAction.condition.extend.damageOverTime.DamageOverTime;
import com.htmake.htbot.discord.skillAction.condition.extend.damageOverTime.extend.Poison;
import com.htmake.htbot.discord.skillAction.factory.ConditionFactory;
import com.htmake.htbot.discord.skillAction.type.SkillType;
import com.htmake.htbot.discord.util.RandomUtil;
import kotlin.Pair;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.List;
import java.util.Map;

public class MonsterAttackAction {

    private final BattleUtil battleUtil;

    private final PlayerKillAction playerKillAction;
    private final ConditionAction conditionAction;

    public MonsterAttackAction() {
        this.battleUtil = new BattleUtil();

        this.playerKillAction = new PlayerKillAction();
        this.conditionAction = new ConditionAction();
    }

    public void execute(ButtonInteractionEvent event, PlayerData playerData, MonsterData monsterData) {
        PlayerStatus playerStatus = playerData.getPlayerStatus();
        MonsterStatus monsterStatus = monsterData.getMonsterStatus();

        if (conditionAction(event, playerStatus, monsterStatus)) {
            conditionAction.execute(event);
            return;
        }

        if (RandomUtil.randomPercentage(monsterStatus.getSkillChance())) {
            handleSkillAttack(event, playerData, monsterData);
        } else {
            handleMonsterAttack(event, playerData, monsterData);
        }

        if (playerStatus.getHealth() == 0) {
            playerKillAction.execute(event, playerStatus, monsterStatus);
        } else {
            conditionAction.execute(event);
        }
    }

    private boolean conditionAction(ButtonInteractionEvent event, PlayerStatus playerStatus, MonsterStatus monsterStatus) {
        Map<String, Condition> monsterCondition = monsterStatus.getConditionMap();

        if (monsterCondition.containsKey("faint")) {
            Faint faint = (Faint) monsterCondition.get("faint");

            if (faint.applyEffect()) {
                String message = monsterStatus.getName() + "이/가 기절했다.";
                battleUtil.updateSituation(event.getUser().getId(), message);
                battleUtil.editEmbed(event, playerStatus, monsterStatus, "progress");

                return true;
            }
        }

        return false;
    }

    private void handleMonsterAttack(ButtonInteractionEvent event, PlayerData playerData, MonsterData monsterData) {
        PlayerStatus playerStatus = playerData.getPlayerStatus();
        MonsterStatus monsterStatus = monsterData.getMonsterStatus();

        Pair<Integer, Boolean> damage = monsterAttackDamage(monsterStatus);
        String message = monsterStatus.getName() + "의 ";

        if (damage.getSecond()) {
            message += "치명타 공격!";
        } else {
            message += "공격.";
        }

        battleUtil.updateSituation(event.getUser().getId(), message);
        battleUtil.editEmbed(event, playerStatus, monsterStatus, "progress");

        applyAttack(event, playerData, monsterData, damage.getFirst());
    }

    private Pair<Integer, Boolean> monsterAttackDamage(MonsterStatus monsterStatus) {
        RandomGenerator random = new MersenneTwister();
        int percentage = random.nextInt(100);

        int damage = monsterStatus.getDamage();
        int criticalChance = monsterStatus.getCriticalChance();
        int criticalDamage = monsterStatus.getCriticalDamage();

        if (percentage < criticalChance) {
            double criticalDamageMultiple = (double) criticalDamage / 100;
            damage = (int) (damage * criticalDamageMultiple);

            return new Pair<>(damage, true);
        }

        return new Pair<>(damage, false);
    }

    private void handleSkillAttack(ButtonInteractionEvent event, PlayerData playerData, MonsterData monsterData) {
        List<MonsterSkillData> skillList = monsterData.getMonsterStatus().getSkillList();
        RandomGenerator random = new MersenneTwister();
        int skillChance = 100;

        for (MonsterSkillData skill : skillList) {
            random.setSeed(System.currentTimeMillis() ^ System.nanoTime());
            int chance = random.nextInt(skillChance);

            if (skill.getChance() > chance) {
                skillAction(event, playerData, monsterData, skill);
                return;
            } else {
                skillChance -= skill.getChance();
            }
        }
    }

    private void skillAction(ButtonInteractionEvent event, PlayerData playerData, MonsterData monsterData, MonsterSkillData skill) {
        PlayerStatus playerStatus = playerData.getPlayerStatus();
        PlayerOriginalStatus playerOriginalStatus = playerData.getPlayerOriginalStatus();
        MonsterStatus monsterStatus = monsterData.getMonsterStatus();
        MonsterOriginalStatus monsterOriginalStatus = monsterData.getMonsterOriginalStatus();

        String message = monsterStatus.getName() + "의 " + skill.getName() + ".";
        battleUtil.updateSituation(event.getUser().getId(), message);
        battleUtil.editEmbed(event, playerStatus, monsterStatus, "progress");

        switch (skill.getSkillType()) {
            case ATTACK -> attackSkillAction(event, playerData, monsterData, skill);
            case HEAL -> healSkillAction(event, playerStatus, monsterStatus, monsterOriginalStatus, skill);
            case BUFF -> buffSkillAction(event, playerStatus, monsterStatus, monsterOriginalStatus, skill);
            case DEBUFF -> debuffSkillAction(event, playerStatus, playerOriginalStatus, monsterStatus, skill);
        }

    }

    private void attackSkillAction(
            ButtonInteractionEvent event,
            PlayerData playerData,
            MonsterData monsterData,
            MonsterSkillData skill
    ) {
        PlayerStatus playerStatus = playerData.getPlayerStatus();
        PlayerOriginalStatus playerOriginalStatus = playerData.getPlayerOriginalStatus();
        MonsterStatus monsterStatus = monsterData.getMonsterStatus();
        MonsterOriginalStatus monsterOriginalStatus = monsterData.getMonsterOriginalStatus();

        applyAttack(event, playerData, monsterData, skill.getDamage());

        if (!skill.getEffect().equals("")) {
            List<String> effect = split(skill.getEffect());
            SkillType skillType = SkillType.toSkillType(effect.get(0));

            switch (skillType) {
                case BUFF -> buffSkillAction(event, playerStatus, monsterStatus, monsterOriginalStatus, skill);
                case DEBUFF -> debuffSkillAction(event, playerStatus, playerOriginalStatus, monsterStatus, skill);
            }
        }
    }

    private void healSkillAction(
            ButtonInteractionEvent event,
            PlayerStatus playerStatus,
            MonsterStatus monsterStatus,
            MonsterOriginalStatus monsterOriginalStatus,
            MonsterSkillData skill
    ) {
        int healing = skill.getDamage();
        int maxHealth = monsterOriginalStatus.getHealth();
        monsterStatus.setHealth(Math.min(maxHealth, monsterStatus.getHealth() + healing));

        String message = healing + "의 체력을 회복했다.";
        battleUtil.updateSituation(event.getUser().getId(), message);
        battleUtil.editEmbed(event, playerStatus, monsterStatus, "progress");
    }

    private void buffSkillAction(
            ButtonInteractionEvent event,
            PlayerStatus playerStatus,
            MonsterStatus monsterStatus,
            MonsterOriginalStatus monsterOriginalStatus,
            MonsterSkillData skill
    ) {
        List<String> effect = split(skill.getEffect());
        int chance = Integer.parseInt(effect.get(3));

        if (RandomUtil.randomPercentage(chance)) {
            Map<String, Condition> monsterCondition = monsterStatus.getConditionMap();

            String conditionId = effect.get(1) + "_" + Integer.parseInt(effect.get(2));
            Condition condition = ConditionFactory.getCondition(conditionId);

            if (condition instanceof Buff buff) {
                if (monsterCondition.containsKey(buff.getId())) {
                    buff.unapply(monsterStatus, monsterOriginalStatus);
                }
                buff.apply(monsterStatus, monsterOriginalStatus);
            }

            monsterCondition.put(condition.getId(), condition);

            String message = condition.getName() + " 효과를 얻었다.";
            battleUtil.updateSituation(event.getUser().getId(), message);
            battleUtil.editEmbed(event, playerStatus, monsterStatus, "progress");
        }
    }

    private void debuffSkillAction(
            ButtonInteractionEvent event,
            PlayerStatus playerStatus,
            PlayerOriginalStatus playerOriginalStatus,
            MonsterStatus monsterStatus,
            MonsterSkillData skill
    ) {
        List<String> effect = split(skill.getEffect());
        int chance = Integer.parseInt(effect.get(3));

        if (RandomUtil.randomPercentage(chance)) {
            Map<String, Condition> playerCondition = playerStatus.getConditionMap();

            String conditionId = effect.get(1) + "_" + Integer.parseInt(effect.get(2));
            Condition condition = ConditionFactory.getCondition(conditionId);

            if (condition instanceof Buff buff) {
                if (playerCondition.containsKey(buff.getId())) {
                    buff.unapply(playerStatus, playerOriginalStatus);
                }
                buff.apply(playerStatus, playerOriginalStatus);
            }

            if (condition instanceof DamageOverTime damageOverTime) {
                if (damageOverTime instanceof Poison poison) {
                    poison.setEffectDamage(playerOriginalStatus.getHealth());
                } else if (damageOverTime instanceof Toxic toxic) {
                    toxic.setEffectDamage(playerStatus.getHealth());
                } else {
                    damageOverTime.setEffectDamage(monsterStatus.getDamage());
                }
            }

            playerCondition.put(condition.getId(), condition);

            String message = condition.getName() + " 효과를 입혔다.";
            battleUtil.updateSituation(event.getUser().getId(), message);
            battleUtil.editEmbed(event, playerStatus, monsterStatus, "progress");
        }
    }

    private void applyAttack(
        ButtonInteractionEvent event,
        PlayerData playerData,
        MonsterData monsterData,
        int damage
    ) {
        PlayerStatus playerStatus = playerData.getPlayerStatus();
        MonsterStatus monsterStatus = monsterData.getMonsterStatus();

        Map<String, Condition> playerCondition = playerStatus.getConditionMap();
        int damageReceived = Math.max(0, damage - playerStatus.getDefence());

        damageReceived = conditionAction.invincibleCheck(playerCondition, damageReceived);

        playerStatus.setHealth(Math.max(0, playerStatus.getHealth() - damageReceived));

        String message = damageReceived + "의 데미지를 입혔다.";
        battleUtil.updateSituation(event.getUser().getId(), message);
        battleUtil.editEmbed(event, playerStatus, monsterStatus, "progress");

        conditionAction.effectProcessing(playerData, monsterData, damageReceived, false);
    }

    private List<String> split(String effect) {
        return List.of(effect.split("-"));
    }
}
