package com.neu.edu.assignment2.repository;

import com.neu.edu.assignment2.model.Answers;
import com.neu.edu.assignment2.model.Categories;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnswerRepository extends JpaRepository<Answers,String> {
}
