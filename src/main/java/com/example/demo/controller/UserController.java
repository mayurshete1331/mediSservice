package com.example.demo.controller;

import com.example.demo.model.UserDetail;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<UserDetail> getAllUsers() {
        return userService.getAllUsers();
    }

    @PostMapping
    public UserDetail createUser(@RequestBody UserDetail user) {
        return userService.createUser(user);
    }
}
