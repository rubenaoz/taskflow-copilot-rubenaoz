package com.taskflow.dto;

/**
 * ProjectProgressResponse — DTO con el progreso de un proyecto (total, terminadas y porcentaje).
 */
public record ProjectProgressResponse(Long projectId, String projectName, long totalTasks, long doneTasks, double percentDone) {
}
