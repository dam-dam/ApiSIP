package com.upiicsa.ApiSIP.Service;

import com.upiicsa.ApiSIP.Dto.User.DataDto;
import com.upiicsa.ApiSIP.Model.UserSIP;
import com.upiicsa.ApiSIP.Repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService {

    private UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public Optional<UserSIP> getUserById(Integer id){
        return userRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public DataDto getData(Integer id) {
        UserSIP user =  userRepository.findById(id).orElse(null);

        return new DataDto(user.getName(), user.getFLastName(), user.getMLastName(),
                user.getEmail(),null);
    }


    @Transactional
    public void updatePassword(UserSIP user, String password) {
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
    }
}
