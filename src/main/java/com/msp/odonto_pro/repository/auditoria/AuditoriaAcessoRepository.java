package com.msp.odonto_pro.repository.auditoria;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.msp.odonto_pro.entity.auditoria.AuditoriaAcesso;

@Repository
public interface AuditoriaAcessoRepository
		extends JpaRepository<AuditoriaAcesso, Long>, JpaSpecificationExecutor<AuditoriaAcesso> {

}
