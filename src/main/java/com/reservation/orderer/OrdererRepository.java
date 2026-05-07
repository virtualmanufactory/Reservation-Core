package com.reservation.orderer;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface OrdererRepository extends CrudRepository<Orderer, Long> {
    //TODO Zrobić metodę do pobierania wszsytkich użytkowników, którzy maja ten adres email
     <Orderer> Optional findByEmail(String email);
}
