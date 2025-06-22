package com.example.demo.service;

import com.example.demo.model.UserDetail;
import java.util.List;

public interface UserService {
    List<UserDetail> getAllUsers();
    UserDetail createUser(UserDetail user);
}
