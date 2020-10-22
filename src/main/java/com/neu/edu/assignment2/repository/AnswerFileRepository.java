package com.neu.edu.assignment2.repository;

import com.neu.edu.assignment2.model.AnswerFiles;
import com.neu.edu.assignment2.model.QuestionFiles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnswerFileRepository extends JpaRepository<AnswerFiles,String> {
}
