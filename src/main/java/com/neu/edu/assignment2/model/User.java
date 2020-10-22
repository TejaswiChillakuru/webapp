package com.neu.edu.assignment2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

import javax.persistence.*;

@Entity
@Table(name= "USERS")
public class User {

    @Id
    @GeneratedValue(generator="system-uuid")
    @GenericGenerator(name="system-uuid", strategy = "org.hibernate.id.UUIDGenerator")
    @ApiModelProperty(readOnly = true)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Column(name="userId", columnDefinition = "VARCHAR(255)", insertable = false, updatable = false, nullable = false)
    private String userId;

    @Column(name="firstName",nullable=false)
    @ApiModelProperty(required = true)
    @JsonProperty("firstName")
    private String firstName;

    @Column(name="lastName",nullable=false)
    @ApiModelProperty(required = true)
    @JsonProperty("lastName")
    private String lastName;

    @Column(name="username",nullable=false,unique = true)
    @ApiModelProperty(required = true)
    @JsonProperty("email")
    private String username;

    @Column(name="password",nullable=false)
    @ApiModelProperty(required = true)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY, value="password")
    private String password;

    @Column(name="accountCreated",nullable=false)
    @ApiModelProperty(required = true)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY, value="account_created")
    private String accountCreated;

    @Column(name="accountUpdated",nullable=false)
    @ApiModelProperty(required = true)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY, value="account_updated")
    private String accountUpdated;

    public User(){

    }
    public User(User user){
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.password = user.getPassword();
        this.username = user.getUsername();
        this.userId = user.getUserId();
        this.accountCreated = user.getAccountCreated();
        this.accountUpdated = user.getAccountUpdated();
    }
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAccountCreated() {
        return accountCreated;
    }

    public void setAccountCreated(String accountCreated) {
        this.accountCreated = accountCreated;
    }

    public String getAccountUpdated() {
        return accountUpdated;
    }

    public void setAccountUpdated(String accountUpdated) {
        this.accountUpdated = accountUpdated;
    }

    //    public void setUserId(int userId) {
//        this.userId = userId;
//    }
}
