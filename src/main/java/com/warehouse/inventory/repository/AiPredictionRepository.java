package com.warehouse.inventory.repository;

import com.warehouse.inventory.entity.AiPrediction;
import com.warehouse.inventory.enums.PredictionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AiPredictionRepository extends JpaRepository<AiPrediction, Long> {

    List<AiPrediction> findByProductIdAndPredictionTypeOrderByCreatedAtDesc(Long productId,
                                                                            PredictionType predictionType);

    List<AiPrediction> findByValidUntilAfter(LocalDateTime dateTime);
}
