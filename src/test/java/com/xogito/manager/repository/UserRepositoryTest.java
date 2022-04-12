package com.xogito.manager.repository;

import com.xogito.manager.fixtures.UserFixture;
import com.xogito.manager.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.TestPropertySource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    void findAll() {
    }

    @Test
    void itShouldCheckIfUserExistsByEmail() {
        String email = "jesus@gmail.com";
        User user = new User(1L, "Jesus Duarte", email);
        userRepository.save(user);

        Boolean existsEmail = userRepository.existsByEmail(email);
        assertThat(existsEmail).isTrue();
    }

    @Test
    void givenASearchTextAndPageableShouldReturnUsersWhichNameOrEmailContainsIt() {
        String search = "jesus";
        int page = 0;
        int limit = 10;

        List<User> usersToReturn = UserFixture.generateUsers("jesus", limit);
        List<User> usersToSkip = UserFixture.generateUsers("carlos", limit);

        List<User> allUsers = Stream.of(usersToReturn, usersToSkip)
                                    .flatMap(Collection::stream)
                                    .collect(Collectors.toList());
        userRepository.saveAll(allUsers);

        List<Sort.Order> orders = new ArrayList<>();
        orders.add(new Sort.Order(Sort.Direction.DESC, "id"));
        Pageable pagingSort = PageRequest.of(page, limit, Sort.by(orders));
        Page<User> pageUsers = userRepository.findAllBySearchPageable(search, pagingSort);
        List<User> expectedUsers = pageUsers.getContent();

        assertThat(expectedUsers.size()).isEqualTo(usersToReturn.size());
        assertThat(expectedUsers.get(0).getName()).contains(search);

    }
}