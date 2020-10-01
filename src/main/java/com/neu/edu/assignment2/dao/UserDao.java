package com.neu.edu.assignment2.dao;

import com.neu.edu.assignment2.model.User;
import com.neu.edu.assignment2.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserDao {
    @Autowired
    UserRepository userRepository;
    @Autowired
    private PasswordEncoder bcryptEncoder;

    public User addUser(User user){
        user.setPassword(bcryptEncoder.encode(user.getPassword()));
        String date = String.valueOf(java.time.LocalDateTime.now());
        user.setAccountUpdated(date);
        user.setAccountCreated(date);
        return userRepository.save(user);
    }

    public User getUser(String userId){
        return userRepository.findById(userId).get();
    }
    public User updateUser(String userId, User user){

        User u = getUser(userId);
        if(u==null)
            return null;
        if(user.getFirstName()!=null)
        u.setFirstName(user.getFirstName());
        if(user.getLastName()!=null)
        u.setLastName(user.getLastName());
        if(user.getPassword()!=null)
        u.setPassword(bcryptEncoder.encode(user.getPassword()));
        String date = String.valueOf(java.time.LocalDateTime.now());
        u.setAccountUpdated(date);
        return userRepository.save(u);
    }

}
