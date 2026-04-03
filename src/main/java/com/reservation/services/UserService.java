package com.reservation.services;

import com.reservation.models.User;
import com.reservation.repositories.UserRepository;
import com.reservation.exceptions.ConflictException;
import com.reservation.exceptions.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User create(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new ConflictException("Email déjà utilisé : " + user.getEmail());
        }
        return userRepository.save(user);
    }

    public List<User> getAll() {
        return userRepository.findAll();
    }

    public User getById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User introuvable : " + id));
    }

    public User update(UUID id, User updatedUser) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User introuvable : " + id));

        // Vérifier email unique (seulement si modifié)
        if (!user.getEmail().equals(updatedUser.getEmail())
                && userRepository.existsByEmail(updatedUser.getEmail())) {
            throw new ConflictException("Email déjà utilisé : " + updatedUser.getEmail());
        }

        user.setName(updatedUser.getName());
        user.setEmail(updatedUser.getEmail());

        return userRepository.save(user);
    }

    public void delete(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User introuvable : " + id);
        }
        userRepository.deleteById(id);
    }
}