package com.yeonsung.crcles.account;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface AccountRepository extends JpaRepository<Account,Long>, QuerydslPredicateExecutor<Account> {

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    Account findByNickname(String nickname);

    Account findByEmail(String email);

    @EntityGraph(attributePaths = {"tags", "zones"})
    Account findAccountWithTagsAndZonesById(Long id);   //태그와 지역정보를 갖는 객체

}
