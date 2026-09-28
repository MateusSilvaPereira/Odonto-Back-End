package com.msp.odonto_pro.repository.notificacao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.msp.odonto_pro.entity.notificacao.NotificacaoConfig;

@Repository
public interface NotificacaoConfigRepository extends JpaRepository<NotificacaoConfig, Long> {

}
