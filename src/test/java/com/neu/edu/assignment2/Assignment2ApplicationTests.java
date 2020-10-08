package com.neu.edu.assignment2;

import com.neu.edu.assignment2.dao.UserDao;
import com.neu.edu.assignment2.model.User;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.annotation.Testable;

import static org.junit.jupiter.api.Assertions.*;

@Testable
public class Assignment2ApplicationTests {


    @Test
    public void passwordValidFalseTest() {
        User u1 = new User();
        u1.setAccountCreated("date1");
        u1.setPassword("34");
        u1.setAccountUpdated("date2");
        u1.setLastName("lastname");
        u1.setFirstName("firstname");
        u1.setUserId("userid");
        u1.setUsername("username");
        System.out.print(u1.getPassword());
        UserDao dao = new UserDao();
        boolean res= dao.validatePassword(u1.getPassword());
        assertFalse(res);
    }

    @Test
    public void passwordValidTrueTest() {
        User u2 = new User();
        u2.setAccountCreated("date1");
        u2.setPassword("Qwerty@123");
        u2.setAccountUpdated("date2");
        u2.setLastName("lastname2");
        u2.setFirstName("firstname2");
        u2.setUserId("userid2");
        u2.setUsername("username2");
        UserDao dao = new UserDao();
        boolean res= dao.validatePassword(u2.getPassword());
        assertTrue(res);
    }
}




