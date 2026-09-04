package com.weeklyreport.backend.repository;

import com.weeklyreport.backend.domain.User;
import com.weeklyreport.backend.domain.UserStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    List<User> findByStatus(UserStatus status, Sort sort);
}
