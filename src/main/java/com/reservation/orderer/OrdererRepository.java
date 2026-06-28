package com.reservation.orderer;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface OrdererRepository extends CrudRepository<Orderer, Long> {
    Optional<Orderer> findByEmail(@Param("email") String email);
}
