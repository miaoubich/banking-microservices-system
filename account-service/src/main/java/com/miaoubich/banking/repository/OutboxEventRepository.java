package com.miaoubich.banking.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.miaoubich.banking.domain.OutboxEvent;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

}
