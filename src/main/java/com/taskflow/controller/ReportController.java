package com.taskflow.controller;

import com.taskflow.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Tag(name = "Reports", description = "Reportes: progreso por proyecto")
public class ReportController {

    private final ProjectService projectService;

    public ReportController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @Operation(summary = "Progreso por proyecto",
            description = "Lista el progreso de cada proyecto: total de tareas, terminadas y porcentaje redondeado a un decimal.")
    @GetMapping("/reports/progress")
    public List<com.taskflow.dto.ProjectProgressResponse> progresoPorProyecto() {
        return projectService.progresoPorProyecto();
    }
}
