package com.msp.odonto_pro.repository.tratamento;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.msp.odonto_pro.entity.tratamento.Tratamento;

@Repository
public interface TratamentoRepository extends JpaRepository<Tratamento, Long>{

}
