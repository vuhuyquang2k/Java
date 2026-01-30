package com.base.demo.services.user;

import com.base.demo.dtos.user.CreateUserRequest;
import com.base.demo.dtos.user.GetUserResponse;
import com.base.demo.dtos.user.UpdateUserRequest;
import com.base.demo.entities.User;
import com.base.demo.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
}
