package com.msp.odonto_pro.repository.parcela;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.msp.odonto_pro.entity.parcela.Parcela;

@Repository
public interface ParcelaRepository extends JpaRepository<Parcela, Long> {

}
