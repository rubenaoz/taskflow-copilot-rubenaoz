package com.taskflow.slice;

import com.taskflow.controller.ProjectController;
import com.taskflow.dto.ProjectSummaryResponse;
import com.taskflow.model.Project;
import com.taskflow.security.JwtAuthenticationFilter;
import com.taskflow.service.ProjectService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProjectController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProjectSummaryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProjectService projectService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void getResumen_proyectoExistente_devuelve200YJson() throws Exception {
        Project p = new Project(2L, "App Móvil", "d", 2L, null);
        ProjectSummaryResponse dto = new ProjectSummaryResponse(2L, "App Móvil", 4,
                Map.of("TODO", 1, "IN_PROGRESS", 2, "DONE", 1), 1);

        when(projectService.buscarPorId(2L)).thenReturn(Optional.of(p));
        when(projectService.resumenDe(p)).thenReturn(dto);

        mockMvc.perform(get("/projects/2/summary").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectId").value(2))
                .andExpect(jsonPath("$.projectName").value("App Móvil"))
                .andExpect(jsonPath("$.totalTasks").value(4))
                .andExpect(jsonPath("$.byStatus.TODO").value(1))
                .andExpect(jsonPath("$.byStatus.IN_PROGRESS").value(2))
                .andExpect(jsonPath("$.byStatus.DONE").value(1))
                .andExpect(jsonPath("$.overdue").value(1));
    }

    @Test
    void getResumen_proyectoInexistente_devuelve404() throws Exception {
        when(projectService.buscarPorId(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/projects/99/summary").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void getResumen_proyecto1_semilla_valoresEsperados() throws Exception {
        // según la semilla: project 1 tiene 5 tareas, TODO 3, IN_PROGRESS 1, DONE 1, overdue 0
        Project p = new Project(1L, "Proyecto 1", "d", 1L, null);
        ProjectSummaryResponse dto = new ProjectSummaryResponse(1L, "Proyecto 1", 5,
                Map.of("TODO", 3, "IN_PROGRESS", 1, "DONE", 1), 0);

        when(projectService.buscarPorId(1L)).thenReturn(Optional.of(p));
        when(projectService.resumenDe(p)).thenReturn(dto);

        mockMvc.perform(get("/projects/1/summary").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectId").value(1))
                .andExpect(jsonPath("$.totalTasks").value(5))
                .andExpect(jsonPath("$.byStatus.TODO").value(3))
                .andExpect(jsonPath("$.byStatus.IN_PROGRESS").value(1))
                .andExpect(jsonPath("$.byStatus.DONE").value(1))
                .andExpect(jsonPath("$.overdue").value(0));
    }

    @Test
    void getResumen_proyecto3_semilla_vacio() throws Exception {
        // según la semilla: project 3 no tiene tareas
        Project p = new Project(3L, "Proyecto 3", "d", 2L, null);
        ProjectSummaryResponse dto = new ProjectSummaryResponse(3L, "Proyecto 3", 0,
                Map.of("TODO", 0, "IN_PROGRESS", 0, "DONE", 0), 0);

        when(projectService.buscarPorId(3L)).thenReturn(Optional.of(p));
        when(projectService.resumenDe(p)).thenReturn(dto);

        mockMvc.perform(get("/projects/3/summary").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectId").value(3))
                .andExpect(jsonPath("$.totalTasks").value(0))
                .andExpect(jsonPath("$.byStatus.TODO").value(0))
                .andExpect(jsonPath("$.byStatus.IN_PROGRESS").value(0))
                .andExpect(jsonPath("$.byStatus.DONE").value(0))
                .andExpect(jsonPath("$.overdue").value(0));
    }
}
