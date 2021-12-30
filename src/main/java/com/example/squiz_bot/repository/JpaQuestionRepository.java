package com.example.squiz_bot.repository;

import com.example.squiz_bot.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;


@Repository
@Transactional(readOnly = true)
public interface JpaQuestionRepository extends JpaRepository<Question, Integer> {
    @Query(nativeQuery = true, value = "SELECT *  FROM java_quiz ORDER BY random() LIMIT 1")
    Question getRandomQuestion();
    @Query(nativeQuery = true, value = "SELECT *  FROM java_quiz WHERE theme = ?1 AND id NOT IN (?2) ORDER BY random() LIMIT 1")
    Question getRandomByTheme(String theme, int[] answered);

    @Query(nativeQuery = true, value = "SELECT * FROM java_quiz  WHERE theme =?1")
    Question[] getAllByTheme(String theme);
}
