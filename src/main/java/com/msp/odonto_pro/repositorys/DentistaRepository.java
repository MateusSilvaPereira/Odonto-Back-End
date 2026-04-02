package com.msp.odonto_pro.repositorys;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.msp.odonto_pro.entity.Dentista;

@Repository
public interface DentistaRepository extends JpaRepository<Dentista, Long>{

}
