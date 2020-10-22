package com.neu.edu.assignment2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.List;
import java.util.Set;

@Entity
@Table(name="question")
public class Question {
    @Id
    @GeneratedValue(generator="system-uuid")
    @GenericGenerator(name="system-uuid", strategy = "uuid")
    @ApiModelProperty(readOnly = true)
    @Column(name="questionId", columnDefinition = "VARCHAR(255)", insertable = false, updatable = false, nullable = false)
    private String questionId;

    @Column(name="userId",nullable=false)
    @JsonProperty("userId")
    private String userId;

    @Column(name="questionText",nullable=false)
    @ApiModelProperty(required = true)
    @JsonProperty("question_text")
    private String questionText;

    @Column(name="createdTimestamp",nullable=false)
    @ApiModelProperty(required = true)
    @JsonProperty("createdTimestamp")
    private String createdTimestamp;

    @Column(name="updatedTimestamp",nullable=false)
    @ApiModelProperty(required = true)
    private String updatedTimestamp;


    @Column(name="answers",nullable=false)
    @OneToMany(mappedBy="question")
    private List<Answers> answers;


    @Column(name="categories",nullable=false)
    @ManyToMany (cascade={CascadeType.MERGE,CascadeType.PERSIST})
    private List<Categories> categories;

    @Column(name="QuestionFiles",nullable=false)
    @OneToMany (mappedBy = "question")
    private List<QuestionFiles> files;

    public String getQuestionId() {
        return questionId;
    }

    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public String getCreatedTimestamp() {
        return createdTimestamp;
    }

    public void setCreatedTimestamp(String createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }

    public String getUpdatedTimestamp() {
        return updatedTimestamp;
    }

    public void setUpdatedTimestamp(String updatedTimestamp) {
        this.updatedTimestamp = updatedTimestamp;
    }

    public List<Answers> getAnswers() {
        return answers;
    }

    public void setAnswers(List<Answers> answers) {
        this.answers = answers;
    }

    public List<Categories> getCategories() {
        return categories;
    }

    public void setCategories(List<Categories> categories) {
        this.categories = categories;
    }

    public List<QuestionFiles> getFiles() {
        return files;
    }

    public void setFiles(List<QuestionFiles> files) {
        this.files = files;
    }
}
