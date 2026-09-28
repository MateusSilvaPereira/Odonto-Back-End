package com.msp.odonto_pro.repository.estoque;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.msp.odonto_pro.entity.estoque.ItemEstoque;

@Repository
public interface ItemEstoqueRepository extends JpaRepository<ItemEstoque, Long> {

	List<ItemEstoque> findByClinicaId(Long clinicaId);

	boolean existsByIdAndClinicaId(Long id, Long clinicaId);

}
