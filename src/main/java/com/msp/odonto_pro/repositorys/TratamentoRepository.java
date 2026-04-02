package com.msp.odonto_pro.repositorys;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.msp.odonto_pro.entity.Tratamento;

@Repository
public interface TratamentoRepository extends JpaRepository<Tratamento, Long>{

}
