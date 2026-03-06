package com.efastream.repositories;

import com.efastream.models.entity.ContentDownload;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContentDownloadRepository extends JpaRepository<ContentDownload, Long> {

    List<ContentDownload> findByUserIdOrderByDownloadedAtDesc(Long userId, Pageable pageable);
}
