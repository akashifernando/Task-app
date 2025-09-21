package com.project.TaskApp.service;

import com.project.TaskApp.dto.TaskRequest;
import com.project.TaskApp.entity.AppUser;
import com.project.TaskApp.entity.Task;
import com.project.TaskApp.repo.TaskRepository;
import com.project.TaskApp.service.impl.TaskServiceImpl;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.time.LocalDate;

public class TaskServiceXssTest {

    @Test
    void createTask_shouldSanitizeDescription() {
        // Arrange
        TaskRepository repo = mock(TaskRepository.class);
        UserService userService = mock(UserService.class);

        AppUser user = new AppUser();
        user.setId(1L);
        user.setUsername("demo");
        when(userService.getCurrentLoggedInUser()).thenReturn(user);

        TaskServiceImpl svc = new TaskServiceImpl(repo, userService);

        TaskRequest req = new TaskRequest();
        req.setTitle("My Task");
        req.setDescription("<script>alert('xss')</script>hello");
        req.setCompleted(false);
        req.setDueDate(LocalDate.now().plusDays(1));

        // stub save to return what it receives
        when(repo.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0, Task.class));

        // Act
        Task saved = svc.createTask(req).getData();

        // Assert: script tag removed, 'hello' remains
        assertThat(saved.getDescription())
                .doesNotContain("<script>")
                .contains("hello");
    }
}
