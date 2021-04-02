package com.yeonsung.crcles.club;

import com.querydsl.jpa.JPQLQuery;
import com.yeonsung.crcles.account.QAccount;
import com.yeonsung.crcles.club.QClub;
import com.yeonsung.crcles.tag.QTag;
import com.yeonsung.crcles.zone.QZone;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import java.util.List;

public class ClubRepositoryExtensionImpl extends QuerydslRepositorySupport implements ClubRepositoryExtension {

    public ClubRepositoryExtensionImpl() {
        super(Club.class);
    }

    @Override
    public List<Club> findByKeyword(String keyword) {
        QClub club = QClub.club;
        JPQLQuery<Club> query = from(club).where(club.published.isTrue()
                .and(club.title.containsIgnoreCase(keyword))
                .or(club.tags.any().title.containsIgnoreCase(keyword))
                .or(club.zones.any().localNameOfCity.containsIgnoreCase(keyword)))
                .leftJoin(club.tags, QTag.tag).fetchJoin()
                .leftJoin(club.zones, QZone.zone).fetchJoin()
                .leftJoin(club.members, QAccount.account).fetchJoin()
                .distinct();
        return query.fetch();
    }
}
