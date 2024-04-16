package com.htmake.htbot.domain.skill.service.impl;

import com.htmake.htbot.domain.player.entity.Player;
import com.htmake.htbot.domain.player.exception.NotFoundPlayerException;
import com.htmake.htbot.domain.player.repository.PlayerRepository;
import com.htmake.htbot.domain.skill.entity.PlayerSkill;
import com.htmake.htbot.domain.skill.entity.Skill;
import com.htmake.htbot.domain.skill.exception.SkillNotFoundException;
import com.htmake.htbot.domain.skill.presentation.data.request.RegisterSkillRequest;
import com.htmake.htbot.domain.skill.repository.PlayerSkillRepository;
import com.htmake.htbot.domain.skill.repository.SkillRepository;
import com.htmake.htbot.domain.skill.service.RegisterSkillService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class RegisterSkillServiceImpl implements RegisterSkillService {

    private final PlayerRepository playerRepository;
    private final SkillRepository skillRepository;
    private final PlayerSkillRepository playerSkillRepository;

    @Override
    public void execute(String playerId, RegisterSkillRequest request) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(NotFoundPlayerException::new);

        Skill skill = skillRepository.findById(request.getId())
                .orElseThrow(SkillNotFoundException::new);

        int number = request.getNumber();

        PlayerSkill existsPlayerSkill = playerSkillRepository.findByNumberAndPlayer(number, player);

        if (existsPlayerSkill != null) {
            existsPlayerSkill.setSkill(skill);
            playerSkillRepository.save(existsPlayerSkill);
        } else {
            PlayerSkill playerSkill = PlayerSkill.builder()
                    .number(number)
                    .player(player)
                    .skill(skill)
                    .build();

            playerSkillRepository.save(playerSkill);
        }
    }
}
