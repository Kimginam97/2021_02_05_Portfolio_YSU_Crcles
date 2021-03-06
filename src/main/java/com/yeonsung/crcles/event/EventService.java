package com.yeonsung.crcles.event;

import com.yeonsung.crcles.account.Account;
import com.yeonsung.crcles.club.Club;
import com.yeonsung.crcles.club.event.ClubUpdateEvent;
import com.yeonsung.crcles.event.event.EnrollmentAcceptedEvent;
import com.yeonsung.crcles.event.event.EnrollmentRejectedEvent;
import com.yeonsung.crcles.event.form.EventForm;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final ModelMapper modelMapper;
    private final EnrollmentRepository enrollmentRepository;
    private final ApplicationEventPublisher eventPublisher;

    /*
    * 모임생성
    * 모임수정
    * 모임삭제
    * */
    public Event createEvent(Event event, Club club, Account account) {
        event.setCreatedBy(account);
        event.setCreatedDateTime(LocalDateTime.now());
        event.setClub(club);
        eventPublisher.publishEvent(new ClubUpdateEvent(event.getClub(),"'"+event.getTitle()+"' 모임을 만들었습니다!"));
        return eventRepository.save(event);
    }

    public void updateEvent(Event event, EventForm eventForm) {
        modelMapper.map(eventForm, event);
        event.acceptWaitingList();
        eventPublisher.publishEvent(new ClubUpdateEvent(event.getClub(),"'"+event.getTitle()+"' 모임정보가 수정되었습니다!"));
    }

    public void deleteEvent(Event event) {
        eventRepository.delete(event);
        eventPublisher.publishEvent(new ClubUpdateEvent(event.getClub(),"'"+event.getTitle()+"' 모임이 삭제 되었습니다!"));
    }

    /*
    * 새로운 모집 참가자 추가
    * 모집 참가자 취소
    * */
    public void newEnrollment(Event event, Account account) {
        if (!enrollmentRepository.existsByEventAndAccount(event, account)) {
            Enrollment enrollment = new Enrollment();
            enrollment.setEnrolledAt(LocalDateTime.now());
            enrollment.setAccepted(event.isAbleToAcceptWaitingEnrollment());
            enrollment.setAccount(account);
            event.addEnrollment(enrollment);
            enrollmentRepository.save(enrollment);
        }
    }

    public void cancelEnrollment(Event event, Account account) {
        Enrollment enrollment = enrollmentRepository.findByEventAndAccount(event, account);
        if (!enrollment.isAttended()) {
            event.removeEnrollment(enrollment);
            enrollmentRepository.delete(enrollment);
            event.acceptNextWaitingEnrollment();
        }
    }

    /*
     * 참가신청확인
     * 참가신청취소
     * 체크인
     * 체크아웃
     * */
    public void acceptEnrollment(Event event, Enrollment enrollment) {
        event.accept(enrollment);
        eventPublisher.publishEvent(new EnrollmentAcceptedEvent(enrollment));
    }

    public void rejectEnrollment(Event event, Enrollment enrollment) {
        event.reject(enrollment);
        eventPublisher.publishEvent(new EnrollmentRejectedEvent(enrollment));
    }

    public void checkInEnrollment(Enrollment enrollment) {
        enrollment.setAttended(true);
    }

    public void cancelCheckInEnrollment(Enrollment enrollment) {
        enrollment.setAttended(false);
    }

}
