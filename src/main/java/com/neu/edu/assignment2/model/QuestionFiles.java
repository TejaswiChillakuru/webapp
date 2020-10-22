package com.neu.edu.assignment2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
@Entity
@Table(name="QuestionFiles")
public class QuestionFiles {
    @Id
    @GeneratedValue(generator="system-uuid")
    @GenericGenerator(name="system-uuid", strategy = "uuid")
    @ApiModelProperty(readOnly = true)
    @Column(name="fileId", columnDefinition = "VARCHAR(255)", insertable = false, updatable = false, nullable = false)
    private String fileId;

    @Column(name="questionId",nullable=false)
    @ApiModelProperty(required = true)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY,value="questionId")
    private String questionId;

    @Column(name="userId",nullable=false)
    @ApiModelProperty(required = true)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY,value="userId")
    private String userId;

    @Column(name="fileName",nullable=false)
    @JsonProperty("fileName")
    private String fileName;

    @ManyToOne
    @JoinColumn(name="questionId", nullable=false,insertable = false,updatable = false)
    private Question question;

    @Column(name="createdDate",nullable=false)
    @JsonProperty("createdDate")
    private String createdDate;

    @Column(name="s3objectName",nullable=false)
    @JsonProperty("s3objectName")
    private String s3objectName;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY,value="size")
    @Column(name="size",nullable=false)
    private String size;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY,value="mime")
    @Column(name="mime",nullable=false)
    private String mime;


    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public String getS3objectName() {
        return s3objectName;
    }

    public void setS3objectName(String s3objectName) {
        this.s3objectName = s3objectName;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getMime() {
        return mime;
    }

    public void setMime(String mime) {
        this.mime = mime;
    }

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
}