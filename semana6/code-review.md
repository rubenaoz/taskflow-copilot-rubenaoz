## Comentario 1
Archivo: src/main/java/com/taskflow/controller/ReportController.java (línea de @GetMapping("/reports/progress"))
"La nueva ruta está protegida por anyRequest().authenticated(), pero no se añade a SecurityRulesTest... añade /reports/progress al @ValueSource existente."

## Comentario 2
Archivo: src/test/java/com/taskflow/slice/ProgresoProyectosControllerTest.java (líneas 44-46)
"La prueba solo verifica projectId, percentDone y totalTasks; deja sin cubrir projectName, doneTasks y los campos restantes del segundo objeto..."