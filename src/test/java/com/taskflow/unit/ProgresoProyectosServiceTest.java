package com.taskflow.unit;

import com.taskflow.model.Project;
import com.taskflow.model.Task;
import com.taskflow.model.TaskStatus;
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
class ProgresoProyectosServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ProjectService projectService;

    @Test
    void progreso_por_proyecto_calculaTotalesYPorcentajesYOrdenaPorId() {
        // proyectos devueltos en orden 3,1,2
        Project p3 = new Project(3L, "P3", "d", 1L, null);
        Project p1 = new Project(1L, "P1", "d", 1L, null);
        Project p2 = new Project(2L, "P2", "d", 1L, null);
        when(projectRepository.findAll()).thenReturn(List.of(p3, p1, p2));

        LocalDate hoy = LocalDate.now();
        try {
            // p1: 5 tareas, 1 DONE -> 20.0
            Task t1 = new Task(11L, "Tarea11", "d", TaskStatus.DONE, null, 1L, 1L, hoy);
            Task t2 = new Task(12L, "Tarea12", "d", TaskStatus.TODO, null, 1L, 1L, hoy.plusDays(1));
            Task t3 = new Task(13L, "Tarea13", "d", TaskStatus.TODO, null, 1L, 1L, hoy.plusDays(2));
            Task t4 = new Task(14L, "Tarea14", "d", TaskStatus.TODO, null, 1L, 1L, hoy.plusDays(3));
            Task t5 = new Task(15L, "Tarea15", "d", TaskStatus.TODO, null, 1L, 1L, hoy.plusDays(4));
            when(taskRepository.findByProjectId(1L)).thenReturn(List.of(t1, t2, t3, t4, t5));

            // p2: 3 tareas, 1 DONE -> 33.3
            Task a1 = new Task(21L, "Tarea21", "d", TaskStatus.DONE, null, 2L, 1L, hoy);
            Task a2 = new Task(22L, "Tarea22", "d", TaskStatus.TODO, null, 2L, 1L, hoy.plusDays(1));
            Task a3 = new Task(23L, "Tarea23", "d", TaskStatus.TODO, null, 2L, 1L, hoy.plusDays(2));
            when(taskRepository.findByProjectId(2L)).thenReturn(List.of(a1, a2, a3));

            // p3: sin tareas
            when(taskRepository.findByProjectId(3L)).thenReturn(List.of());

            var resultado = projectService.progresoPorProyecto();

            // Debe salir en orden 1,2,3
            assertEquals(1L, resultado.get(0).projectId());
            assertEquals(2L, resultado.get(1).projectId());
            assertEquals(3L, resultado.get(2).projectId());

            assertEquals(5L, resultado.get(0).totalTasks());
            assertEquals(1L, resultado.get(0).doneTasks());
            assertEquals(20.0, resultado.get(0).percentDone());

            assertEquals(3L, resultado.get(1).totalTasks());
            assertEquals(1L, resultado.get(1).doneTasks());
            assertEquals(33.3, resultado.get(1).percentDone());

            assertEquals(0L, resultado.get(2).totalTasks());
            assertEquals(0L, resultado.get(2).doneTasks());
            assertEquals(0.0, resultado.get(2).percentDone());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
