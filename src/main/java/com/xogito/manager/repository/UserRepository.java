package com.xogito.manager.repository;

import com.xogito.manager.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<User, Long> {
    @Query("SELECT u FROM User u WHERE LOWER(CONCAT(u.name, u.email)) LIKE LOWER(CONCAT('%', ?1, '%')) ")
    Page<User> findAll(String search, Pageable pageable);
    User findByEmail(String email);
}
