package com.neu.edu.assignment2.repository;

import com.neu.edu.assignment2.model.Question;
import com.neu.edu.assignment2.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QuestionRepository extends JpaRepository<Question,String> {

}
