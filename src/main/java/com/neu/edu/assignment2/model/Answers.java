package com.neu.edu.assignment2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Entity
@Table(name="answers")
public class Answers {
    @Id
    @GeneratedValue(generator="system-uuid")
    @GenericGenerator(name="system-uuid", strategy = "uuid")
    @ApiModelProperty(readOnly = true)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Column(name="answerId", columnDefinition = "VARCHAR(255)", insertable = false, updatable = false, nullable = false)
    private String answerId;


    @Column(name="questionId",nullable=false)
    @ApiModelProperty(required = true)
    @JsonProperty("questionId")
    private String questionId;

    @ManyToOne
    @JoinColumn(name="questionId", nullable=false,insertable = false,updatable = false)
    private Question question;

    @Column(name="createdTimestamp",nullable=false)
    @ApiModelProperty(required = true)
    @JsonProperty("createdTimestamp")
    private String createdTimestamp;

    @Column(name="updatedTimestamp",nullable=false)
    @ApiModelProperty(required = true)
    @JsonProperty("updatedTimestamp")
    private String updatedTimestamp;

    @Column(name="userId",nullable=false)
    @ApiModelProperty(required = true)
    @JsonProperty("userId")
    private String userId;

    @Column(name="answerText",nullable=false)
    @ApiModelProperty(required = true)
    @JsonProperty("answer_text")
    private String answerText;

    public String getAnswerId() {
        return answerId;
    }

    public void setAnswerId(String answerId) {
        this.answerId = answerId;
    }

    public String getQuestionId() {
        return questionId;
    }

    public void setQuestionId(String questionId) {
        this.questionId = questionId;
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAnswerText() {
        return answerText;
    }

    public void setAnswerText(String answerText) {
        this.answerText = answerText;
    }
}
