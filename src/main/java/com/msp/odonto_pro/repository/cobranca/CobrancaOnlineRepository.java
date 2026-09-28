package com.msp.odonto_pro.repository.cobranca;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.msp.odonto_pro.entity.cobranca.CobrancaOnline;

@Repository
public interface CobrancaOnlineRepository extends JpaRepository<CobrancaOnline, Long> {

}
