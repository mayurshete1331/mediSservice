package com.example.demo.service;// src/main/java/com/example/demo/service/impl/UserServiceImpl.java

import com.example.demo.model.UserDetail;
import com.example.demo.repository.UserDetailRepository;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserDetailRepository userDetailRepository;

    @Autowired
    public UserServiceImpl(UserDetailRepository userDetailRepository) {
        this.userDetailRepository = userDetailRepository;
    }

    @Override
    public List<UserDetail> getAllUsers() {
        return userDetailRepository.findAll();
    }

    @Override
    public UserDetail createUser(UserDetail user) {
        return userDetailRepository.save(user);
    }
}
