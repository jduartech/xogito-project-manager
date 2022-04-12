package com.xogito.manager.repository;

import com.xogito.manager.fixtures.ProjectFixture;
import com.xogito.manager.model.Project;
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
class ProjectRepositoryTest {

    @Autowired
    private ProjectRepository projectRepository;

    @AfterEach
    void tearDown() {
        projectRepository.deleteAll();
    }

    @Test
    void givenASearchTextAndPageableShouldReturnProjectsWhichNameContainsIt() {
        String search = "accept";
        int page = 0;
        int limit = 10;

        List<Project> projectsToReturn = ProjectFixture.generateProjects("Project accepted", limit);
        List<Project> projectsToSkip = ProjectFixture.generateProjects("Project Skipped", limit);

        List<Project> allProjects = Stream.of(projectsToReturn, projectsToSkip)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        projectRepository.saveAll(allProjects);

        List<Sort.Order> orders = new ArrayList<>();
        orders.add(new Sort.Order(Sort.Direction.DESC, "id"));
        Pageable pagingSort = PageRequest.of(page, limit, Sort.by(orders));
        Page<Project> pageUsers = projectRepository.findAllBySearchPageable(search, pagingSort);
        List<Project> expectedUsers = pageUsers.getContent();

        assertThat(expectedUsers.size()).isEqualTo(projectsToReturn.size());
        assertThat(expectedUsers.get(0).getName()).contains(search);
    }
}