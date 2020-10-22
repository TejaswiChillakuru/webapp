package com.neu.edu.assignment2.repository;

import com.neu.edu.assignment2.model.Question;
import com.neu.edu.assignment2.model.QuestionFiles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.io.File;

@Repository
public interface FileRepository extends JpaRepository<QuestionFiles,String> {

}
