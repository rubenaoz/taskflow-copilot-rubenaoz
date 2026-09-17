package com.taskflow.unit;

import com.taskflow.dto.ProjectSummaryResponse;
import com.taskflow.exception.TaskValidationException;
import com.taskflow.model.*;
import com.taskflow.repository.ProjectRepository;
import com.taskflow.repository.TaskRepository;
import com.taskflow.repository.UserRepository;
import com.taskflow.service.ProjectService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ProjectService projectService;

    @Test
    void resumenDe_proyectoConTareas_cuentaCorrectamente() throws TaskValidationException {
        Project p = new Project(2L, "App Móvil", "d", 2L, null);
        Task t1 = new Task(5L, "T-1", "d", TaskStatus.TODO, Priority.MED, 2L, 1L, null);
        Task t2 = new Task(6L, "T-2", "d", TaskStatus.IN_PROGRESS, Priority.MED, 2L, 1L, null);
        Task t3 = new Task(7L, "T-3", "d", TaskStatus.IN_PROGRESS, Priority.MED, 2L, 1L, LocalDate.now().minusDays(1)); // vencida
        Task t4 = new Task(8L, "T-4", "d", TaskStatus.DONE, Priority.MED, 2L, 1L, LocalDate.now().minusDays(5));

        when(taskRepository.findByProjectId(2L)).thenReturn(List.of(t1, t2, t3, t4));

        ProjectSummaryResponse summary = projectService.resumenDe(p);

        assertEquals(4, summary.totalTasks());
        assertEquals(1, summary.byStatus().get("TODO"));
        assertEquals(2, summary.byStatus().get("IN_PROGRESS"));
        assertEquals(1, summary.byStatus().get("DONE"));
        assertEquals(1, summary.overdue());
    }

    @Test
    void resumenDe_proyectoSinTareas_devuelveCeros() {
        Project p = new Project(3L, "Empty", "d", 2L, null);
        when(taskRepository.findByProjectId(3L)).thenReturn(List.of());

        ProjectSummaryResponse summary = projectService.resumenDe(p);

        assertEquals(0, summary.totalTasks());
        assertEquals(0, summary.byStatus().get("TODO"));
        assertEquals(0, summary.byStatus().get("IN_PROGRESS"));
        assertEquals(0, summary.byStatus().get("DONE"));
        assertEquals(0, summary.overdue());
    }
}
