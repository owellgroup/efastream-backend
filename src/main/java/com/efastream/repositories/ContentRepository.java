package com.efastream.repositories;

import com.efastream.models.entity.Content;
import com.efastream.models.enums.ContentStatus;
import com.efastream.models.enums.ContentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContentRepository extends JpaRepository<Content, Long> {

    List<Content> findByPartnerIdOrderByCreatedAtDesc(Long partnerId);

    List<Content> findByPartnerIdAndStatus(Long partnerId, ContentStatus status);

    Page<Content> findByStatus(ContentStatus status, Pageable pageable);

    Page<Content> findByStatusAndContentType(ContentStatus status, ContentType contentType, Pageable pageable);

    @Query("SELECT c FROM Content c WHERE c.status = :status")
    List<Content> findAllApproved(@Param("status") ContentStatus status);
}
