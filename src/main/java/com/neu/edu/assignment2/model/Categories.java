package com.neu.edu.assignment2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name="Categories")
public class Categories {
    @Id
    @GeneratedValue(generator="system-uuid")
    @GenericGenerator(name="system-uuid", strategy = "uuid")
    @ApiModelProperty(readOnly = true)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Column(name="categoryId", columnDefinition = "VARCHAR(255)", insertable = false, updatable = false, nullable = false)
    private String categoryId;

    @Column(name="category",nullable=false,unique = true)
    @JsonProperty("category")
    private String category;


//    @Column(name="questionId",nullable=false)
//    @JsonProperty("questionId")
//    private String questionId;

//    @ManyToMany(cascade = { CascadeType.MERGE,CascadeType.PERSIST})
//    @JoinColumn(name="questionId", nullable=false,insertable = false,updatable = false)
//    private List<Question> question;

//    @ManyToOne
//    @JoinColumn(name="questionId", nullable=false,insertable = false,updatable = false)
//    private Question question;

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

//    public String getQuestionId() {
//        return questionId;
//    }
//
//    public void setQuestionId(String questionId) {
//        this.questionId = questionId;
//    }
}
