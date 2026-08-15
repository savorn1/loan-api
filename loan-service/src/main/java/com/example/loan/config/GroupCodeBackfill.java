package com.example.loan.config;

import com.example.loan.entity.Group;
import com.example.loan.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// One-time backfill for groups left with a null/blank code — rows created before this
// column existed, or through some path that skipped GroupServiceImpl.create()'s
// auto-generation branch. Idempotent: only rows still blank get touched, so this is a
// no-op on every startup after the first. Unlike Application.applicationNo / Loan.loanNo,
// Group.code isn't updatable=false, so a normal save() here is enough — no bulk update needed.
@Component
@RequiredArgsConstructor
@Slf4j
public class GroupCodeBackfill implements CommandLineRunner {

    private final GroupRepository groupRepository;

    @Override
    @Transactional
    public void run(String... args) {
        List<Group> missing = groupRepository.findWithBlankCode();
        if (missing.isEmpty()) {
            return;
        }
        for (Group group : missing) {
            group.setCode("GRP" + String.format("%06d", group.getId()));
        }
        groupRepository.saveAll(missing);
        log.info("Backfilled code for {} group(s)", missing.size());
    }
}
