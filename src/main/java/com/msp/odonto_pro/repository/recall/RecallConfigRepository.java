package com.msp.odonto_pro.repository.recall;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.msp.odonto_pro.entity.recall.RecallConfig;

@Repository
public interface RecallConfigRepository extends JpaRepository<RecallConfig, Long> {

	Optional<RecallConfig> findByClinicaId(Long clinicaId);

}
