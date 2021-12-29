package com.example.squiz_bot.repository;

import com.example.squiz_bot.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@Transactional(readOnly = true)
public interface JpaUserRepository extends JpaRepository <User, Integer> {
    Optional<User> getByChatId(Long chatId);
}
