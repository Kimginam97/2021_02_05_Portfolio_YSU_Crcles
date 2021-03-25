package com.yeonsung.crcles.event;

import com.yeonsung.crcles.account.Account;
import com.yeonsung.crcles.account.UserAccount;
import com.yeonsung.crcles.club.Club;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@NamedEntityGraph(
        name = "Event.withEnrollments",
        attributeNodes = @NamedAttributeNode("enrollments")
)
@Entity
@Getter
@Setter
@EqualsAndHashCode(of = "id")
public class Event {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private Club club;  // 동아리

    @ManyToOne
    private Account createdBy;   // 모임생성자

    @Column(nullable = false)
    private String title;   // 제목

    @Lob
    private String description; // 설명

    @Column(nullable = false)
    private LocalDateTime createdDateTime;  // 모임접수 생성날짜

    @Column(nullable = false)
    private LocalDateTime endEnrollmentDateTime;    // 모임접수 끝난날짜

    @Column(nullable = false)
    private LocalDateTime startDateTime;    // 모임 시작한 날짜

    @Column(nullable = false)
    private LocalDateTime endDateTime;  // 모임 끝난 날짜

    @Column
    private Integer limitOfEnrollments; // 등록 인원수

    @OneToMany(mappedBy = "event")
    private List<Enrollment> enrollments;   // 등록

    @Enumerated(EnumType.STRING)
    private EventType eventType;    // 등록하는 방법


    // 모임을 모집중이고 회원이 다른지
    public boolean isEnrollableFor(UserAccount userAccount) {
        return isNotClosed() && !isAlreadyEnrolled(userAccount);
    }

    // 모임을 모집중이고 회원이 같은지 확인
    public boolean isDisenrollableFor(UserAccount userAccount) {
        return isNotClosed() && isAlreadyEnrolled(userAccount);
    }

    // 모임 모집중
    private boolean isNotClosed() {
        return this.endEnrollmentDateTime.isAfter(LocalDateTime.now());
    }

    // 모임중 회원이 같고 참여중인지?
    public boolean isAttended(UserAccount userAccount) {
        Account account = userAccount.getAccount();
        for (Enrollment e : this.enrollments) {
            if (e.getAccount().equals(account) && e.isAttended()) {
                return true;
            }
        }

        return false;
    }

    // 모임중 회원이 같은지?
    private boolean isAlreadyEnrolled(UserAccount userAccount) {
        Account account = userAccount.getAccount();
        for (Enrollment e : this.enrollments) {
            if (e.getAccount().equals(account)) {
                return true;
            }
        }
        return false;
    }

    // 모집인원수
    public int numberOfRemainSpots() {
        return this.limitOfEnrollments - (int) this.enrollments.stream().filter(Enrollment::isAccepted).count();
    }

    // 참가인원수
    public long getNumberOfAcceptedEnrollments() {
        return this.enrollments.stream().filter(Enrollment::isAccepted).count();
    }

}
