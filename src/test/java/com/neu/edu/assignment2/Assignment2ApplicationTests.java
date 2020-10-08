package com.neu.edu.assignment2;

import com.neu.edu.assignment2.dao.UserDao;
import com.neu.edu.assignment2.model.User;
import com.neu.edu.assignment2.repository.UserRepository;
import org.junit.Test;
import org.junit.platform.commons.annotation.Testable;
import org.junit.runner.RunWith;
import org.mockito.junit.jupiter.MockitoSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@Testable
public class Assignment2ApplicationTests {

    @Autowired
    private UserDao userDao;

    @MockBean
    private UserRepository userRepository;

    @Test
    public void getUserTest(){

        User u2 = new User();
        u2.setAccountCreated("date1");
        u2.setPassword("password2");
        u2.setAccountUpdated("date2");
        u2.setLastName("lastname2");
        u2.setFirstName("firstname2");
        u2.setUserId("userid2");
        u2.setUsername("username2");
        when(userRepository.findById("userid2")).thenReturn(Optional.of(u2));
        assertEquals("username2",userDao.getUser("userid2").getUsername());
    }
    @Test
    public void saveUserTest(){
        User u1 = new User();
        u1.setAccountCreated("date1");
        u1.setPassword("password");
        u1.setAccountUpdated("date2");
        u1.setLastName("lastname");
        u1.setFirstName("firstname");
        u1.setUserId("userid");
        u1.setUsername("username");
        when(userRepository.save(u1)).thenReturn(u1);
        assertEquals(u1,userDao.addUser(u1));
    }
    @Test
    public void updateUserTest(){
        User u1 = new User();
        u1.setAccountCreated("date1");
        u1.setPassword("password");
        u1.setAccountUpdated("date2");
        u1.setLastName("lastname");
        u1.setFirstName("firstname");
        u1.setUserId("userid");
        u1.setUsername("username");
        User u2 = new User();
        u2.setAccountCreated("date1");
        u2.setPassword("password2");
        u2.setAccountUpdated("date2");
        u2.setLastName("lastname2");
        u2.setFirstName("firstname2");
        u2.setUserId("userid2");
        u2.setUsername("username2");
        when(userRepository.findById("userid")).thenReturn(Optional.of(u1));
        when(userRepository.save(u1)).thenReturn(u1);
        assertEquals("firstname2",userDao.updateUser("userid",u2).getFirstName());
    }


}
