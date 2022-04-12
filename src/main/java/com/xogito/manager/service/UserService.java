package com.xogito.manager.service;

import com.xogito.manager.model.User;
import com.xogito.manager.model.dto.Paging;
import com.xogito.manager.model.dto.UserDto;
import com.xogito.manager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User createUser(User user) {
        User userDB = userRepository.findByEmail(user.getEmail());
        if (userDB != null) {
            throw new DataIntegrityViolationException(String.format("The email %s is already registered", user.getEmail()));
        }
        return userRepository.save(user);
    }

    public User getUser(Long id) {
        return userRepository.findById(id).orElseThrow(() ->
                new EntityNotFoundException(String.format("User id={%d} was not found", id)));
    }

    public Map<String, Object> getAllUsers(String search, int page, int limit, String[] sort) {
        List<Order> orders = new ArrayList<>();
        if (sort[0].contains(",")) {
            for (String sortOrder : sort) {
                String[] _sort = sortOrder.split(",");
                orders.add(new Order(getSortDirection(_sort[1]), _sort[0]));
            }
        } else {
            orders.add(new Order(getSortDirection(sort[1]), sort[0]));
        }
        Pageable pagingSort = PageRequest.of(page-1, limit, Sort.by(orders));
        Page<User> pageUsers = userRepository.findAll(search, pagingSort);
        Paging paging = new Paging(pageUsers.getNumber()+1, pageUsers.getNumberOfElements(), pageUsers.getTotalPages());
        List<UserDto> users = pageUsers.getContent().stream().map(UserDto::from).collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("data", users);
        response.put("paging", paging);
        return response;
    }

    private Sort.Direction getSortDirection(String direction) {
        return direction.contains("desc")? Sort.Direction.DESC : Sort.Direction.ASC;
    }

    public User deleteUser(Long id) {
        User user = getUser(id);
        userRepository.delete(user);
        return user;
    }

    @Transactional
    public User updateUser(Long id, User user) {
        User userToEdit = getUser(id);
        if (user.getName() != null && !user.getName().isBlank()) {
            userToEdit.setName(user.getName());
        }
        if (user.getEmail() != null && !user.getEmail().isBlank()) {
            userToEdit.setEmail(user.getEmail());
        }
        return userToEdit;
    }
}
