package com.neu.edu.assignment2.controller;


import com.neu.edu.assignment2.dao.UserDao;
import com.neu.edu.assignment2.model.User;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping(value = "/v1/user")
public class UserController 
    @Autowired
    private UserDao userDao;

    @PostMapping
    @ApiOperation(value="Create a user")
    public Object addUser(@RequestBody User user){
        try{
            String regex = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";
            String password = "^([a-zA-Z0-9@*#]{8,15})$";
            System.out.println(user.getUsername().matches(regex));
            System.out.println(user.getPassword().matches(password));
            if((user.getUsername()!=null&&!user.getUsername().matches(regex))|| (!user.getPassword().matches(password))){
                return  ResponseEntity.badRequest();
            }
            return userDao.addUser(user);
        }catch(Exception ex){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bad Request",ex);
        }
    }

    @GetMapping(value="/self")
    @ApiOperation(value="Get User Information")
    public User getUser(@AuthenticationPrincipal User user){
//        if (user!=null&&user.getPassword()!=null) {
//            // Authorization: Basic base64credentials
//            String base64Credentials = authorization.substring("Basic".length()).trim();
//            byte[] credDecoded = Base64.getDecoder().decode(base64Credentials);
//            String credentials = new String(credDecoded, StandardCharsets.UTF_8);
//            // credentials = username:password
//            final String[] values = credentials.split(":", 2);
//        }
        System.out.println("password:"+user.getPassword());
        User u = userDao.getUser(user.getUserId());
        try {
            if (!u.getPassword().equalsIgnoreCase(user.getPassword())) {
                throw new Exception();
            }
        }catch(Exception ex){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid Credentials",ex);
        }
        return u;
    }
    @PutMapping(value="/self")
    @ApiOperation(value="Update User Information")
    public Object updateUser(@AuthenticationPrincipal User loggedUser , @RequestBody User user){
        try {
            System.out.println(loggedUser);
            System.out.println(user);
            if (user.getUsername() != null || user.getAccountCreated() != null || user.getAccountUpdated() != null) {
                System.out.println("inside");
                throw new Exception();
            }
            return (User) userDao.updateUser(loggedUser.getUserId(), user);
        }catch(Exception e){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bad Request",e);
        }
    }

}
