package com.example.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.entity.Notice;
import com.example.repository.NoticeRepository;

import lombok.Data;

@Data
@Service
public class NoticeService {
    final NoticeRepository noticeRepository;

    // 공지 생성
    public Notice createNotice(Notice notice) {
        return noticeRepository.save(notice);
    }

    // 공지사항 조회
    public List<Notice> getActiveNotice() {
        return noticeRepository.findByIsActiveTrueOrderByCreatedAtDesc();
    }

    /// 특정 공지사항 조회 메소드
    // 수정 (update) 이나 삭제 (delete) 작업 전 해당 ID의 공지사항이 실제로 존재하는지 검토하기 위한 존재
    //　詰まり、検証段階という事
    public java.util.Optional<Notice> getNoticeById(Integer noticeId) {
        return noticeRepository.findById(noticeId);

    }

    // 공지 수정
    public Notice updateNotice(Notice notice) {
        return noticeRepository.save(notice);
    }
}
