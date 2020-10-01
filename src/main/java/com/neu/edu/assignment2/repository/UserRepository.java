package com.neu.edu.assignment2.repository;

import com.neu.edu.assignment2.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,String> {
    //Optional<User> findByUsernameAndPassword(String username, String password);
    Optional<User> findByUsername(String username);
//    @Query("SELECT u FROM User u WHERE LOWER(u.username) = LOWER(:username) and u.password = :password")
//    public User findByUsernameAndPassword(@Param("username") String username, @Param("password") String password);
}

