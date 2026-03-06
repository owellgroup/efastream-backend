package com.efastream.repositories;

import com.efastream.models.entity.ContentView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContentViewRepository extends JpaRepository<ContentView, Long> {

    List<ContentView> findByUserIdOrderByViewedAtDesc(Long userId, Pageable pageable);

    boolean existsByUserIdAndContentId(Long userId, Long contentId);
}
