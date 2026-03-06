package com.efastream.repositories;

import com.efastream.models.entity.HeroSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HeroSectionRepository extends JpaRepository<HeroSection, Long> {

    List<HeroSection> findByActiveTrueOrderByIdAsc();
}
