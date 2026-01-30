package com.base.demo.controllers;

import com.base.demo.dtos.common.ApiResponse;
import com.base.demo.dtos.user.CreateUserRequest;
import com.base.demo.dtos.user.GetUserResponse;
import com.base.demo.dtos.user.UpdateUserRequest;
import com.base.demo.services.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

}
