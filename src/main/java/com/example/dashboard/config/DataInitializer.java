package com.example.dashboard.config;

import com.example.dashboard.model.Project;
import com.example.dashboard.model.Task;
import com.example.dashboard.model.TaskStatus;
import com.example.dashboard.repository.ProjectRepository;
import com.example.dashboard.repository.TaskRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
@Profile({"dev", "default"})
public class DataInitializer implements CommandLineRunner {

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;

    public DataInitializer(ProjectRepository projectRepository, TaskRepository taskRepository) {
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
    }

    @Override
    public void run(String... args) {
        // Guard: skip if data already seeded (checks both to handle partial-seed dev restarts)
        if (projectRepository.count() > 0 || taskRepository.count() > 0) {
            return;
        }

        // Seed 2 Projects
        Project metroProject = new Project(
                "Downtown Metro Corridor Phase 1",
                "Sector 4 Central Station",
                LocalDate.now().minusMonths(3),
                LocalDate.now().plusMonths(9)
        );

        Project highwayProject = new Project(
                "Riverside Highway Expansion",
                "Riverbank Bypass Bridge",
                LocalDate.now().minusMonths(1),
                LocalDate.now().plusMonths(14)
        );

        projectRepository.save(metroProject);
        projectRepository.save(highwayProject);

        // Seed 5 Tasks (including one overdue task)
        Task task1 = new Task(
                metroProject,
                "Foundation Excavation & Soil Stabilization",
                TaskStatus.COMPLETED,
                100,
                LocalDate.now().minusDays(15),
                "Piling depth tested and certified by geotechnical team."
        );
        task1.setLastUpdated(LocalDateTime.now().minusDays(15));

        Task task2 = new Task(
                metroProject,
                "Steel Rebar Cage Assembly for Pier 4",
                TaskStatus.IN_PROGRESS,
                65,
                LocalDate.now().plusDays(10),
                "Material delivery confirmed. Welding inspection scheduled for Friday."
        );
        task2.setLastUpdated(LocalDateTime.now().minusDays(2));

        // Task 3: OVERDUE task (due date is in the past, status is BLOCKED)
        Task task3 = new Task(
                metroProject,
                "Structural Concrete Pouring - Pier 4 Cap",
                TaskStatus.BLOCKED,
                25,
                LocalDate.now().minusDays(3),
                "Delayed due to unseasonal rain and cement batch mix approval."
        );
        task3.setLastUpdated(LocalDateTime.now().minusDays(1));

        Task task4 = new Task(
                highwayProject,
                "Drainage Culvert Excavation",
                TaskStatus.IN_PROGRESS,
                45,
                LocalDate.now().plusDays(20),
                "Precast culvert sections delivered on site. Backfilling in progress."
        );
        task4.setLastUpdated(LocalDateTime.now().minusHours(12));

        Task task5 = new Task(
                highwayProject,
                "Bridge Abutment Geodetic Survey",
                TaskStatus.NOT_STARTED,
                0,
                LocalDate.now().plusDays(30),
                "Awaiting topographical survey team from highway authority."
        );
        task5.setLastUpdated(LocalDateTime.now().minusDays(5));

        taskRepository.save(task1);
        taskRepository.save(task2);
        taskRepository.save(task3);
        taskRepository.save(task4);
        taskRepository.save(task5);
    }
}
