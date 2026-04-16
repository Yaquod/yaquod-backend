package com.yaquodorg.yaquod.repository;

import com.yaquodorg.yaquod.entity.SavedCard;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SavedCardRepository extends JpaRepository<SavedCard, Long> {
    List<SavedCard> findByUserId(Long userId);

    boolean existsByUserIdAndToken(Long userId, String token);
}
