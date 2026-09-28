package com.msp.odonto_pro.repository.convenioprocedimento;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.msp.odonto_pro.entity.convenioprocedimento.ConvenioProcedimento;

@Repository
public interface ConvenioProcedimentoRepository extends JpaRepository<ConvenioProcedimento, Long> {

	List<ConvenioProcedimento> findByConvenioId(Long convenioId);

	boolean existsByIdAndConvenioId(Long id, Long convenioId);

}
