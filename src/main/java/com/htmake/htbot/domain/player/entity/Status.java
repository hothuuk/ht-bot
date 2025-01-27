package com.htmake.htbot.domain.player.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Status {

    @Id
    @Column(name = "player_id")
    private String id;

    @Column(name = "player_damage", nullable = false)
    private int damage;

    @Column(name = "player_health", nullable = false)
    private int health;

    @Column(name = "player_defence", nullable = false)
    private int defence;

    @Column(name = "player_mana", nullable = false)
    private int mana;

    @Column(name = "player_criticalChance", nullable = false)
    private int criticalChance;

    @Column(name = "player_criticalDamage", nullable = false)
    private int criticalDamage;

    public void updateStatus(
            int damage,
            int health,
            int defence,
            int mana,
            int criticalChance,
            int criticalDamage
    ) {
        this.damage = damage;
        this.health = health;
        this.defence = defence;
        this.mana = mana;
        this.criticalChance = criticalChance;
        this.criticalDamage = criticalDamage;
    }

    public void addDamage(int damage) {
        this.damage += damage;
    }

    public void addHealth(int health) {
        this.health += health;
    }

    public void addMana(int mana) {
        this.mana += mana;
    }

    public void addCriticalChance(int criticalChance) {
        this.criticalChance += criticalChance;
    }
}
