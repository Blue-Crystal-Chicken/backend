package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.UserEntity;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.exception.EmailAlreadyExistsException;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.exception.EmailNotFoundException;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.exception.UserNotFoundException;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.repository.UserRepository;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.utils.PasswordHasher;

@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;

    @Transactional
    public UserEntity registerUser(UserEntity user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new EmailAlreadyExistsException();
        }
        UserEntity save = user;
        save.setPassword(PasswordHasher.hash(save.getPassword()));
        return userRepository.save(save);
    }

    @Transactional(readOnly = true)
    public UserEntity findByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new EmailNotFoundException());
    }

    @Transactional
    public UserEntity updateUser(Long id, UserEntity user) {
        UserEntity existingUser = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException());
        existingUser.setEmail(user.getEmail());
        existingUser.setPassword(user.getPassword());
        return userRepository.save(existingUser);
    }

    @Transactional
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<UserEntity> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public UserEntity getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new UserNotFoundException());
    }

    @Transactional(readOnly = true)
    public UserEntity login(UserEntity user){
        if (!userRepository.existsByEmail(user.getEmail())) {
            throw new EmailNotFoundException();
        }else if(!PasswordHasher.check(user.getPassword(), userRepository.findByEmail(user.getEmail()).get().getPassword())){
            throw new UserNotFoundException();
        }
        return userRepository.findByEmail(user.getEmail()).get();
    }
    
}
