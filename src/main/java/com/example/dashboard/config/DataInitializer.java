package com.example.dashboard.config;

import com.example.dashboard.model.JobStatus;
import com.example.dashboard.model.Project;
import com.example.dashboard.model.Role;
import com.example.dashboard.model.Task;
import com.example.dashboard.model.TaskStatus;
import com.example.dashboard.model.User;
import com.example.dashboard.repository.ProjectRepository;
import com.example.dashboard.repository.TaskRepository;
import com.example.dashboard.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@Profile({"dev", "default"})
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${DEV_ADMIN_PASSWORD:admin123}")
    private String adminPassword;

    @Value("${DEV_MANAGER_PASSWORD:manager123}")
    private String managerPassword;

    @Value("${DEV_ENGINEER_PASSWORD:engineer123}")
    private String engineerPassword;

    public DataInitializer(ProjectRepository projectRepository,
                           TaskRepository taskRepository,
                           UserRepository userRepository,
                           PasswordEncoder passwordEncoder) {
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        ensureDataDirectoryExists();
        seedUsers();
        seedProjectsAndTasks();
        migrateExistingUserData();
    }

    private void ensureDataDirectoryExists() {
        try {
            File dataDir = new File("./data");
            if (!dataDir.exists()) {
                boolean created = dataDir.mkdirs();
                if (created) {
                    log.info("Created local persistent data directory at: {}", dataDir.getAbsolutePath());
                }
            }
        } catch (Exception e) {
            log.warn("Could not pre-create data directory: {}", e.getMessage());
        }
    }

    private void seedUsers() {
        seedUserIfMissing("admin", adminPassword, Role.ADMIN, "System Administrator", "admin@construction.local");
        seedUserIfMissing("manager", managerPassword, Role.MANAGER, "Project Manager", "manager@construction.local");
        seedUserIfMissing("engineer", engineerPassword, Role.ENGINEER, "Lead Site Engineer", "engineer@construction.local");
    }

    private void seedUserIfMissing(String username, String rawPassword, Role role, String fullName, String email) {
        if (!userRepository.existsByUsername(username)) {
            User user = new User(
                    username,
                    passwordEncoder.encode(rawPassword),
                    role,
                    fullName,
                    email,
                    JobStatus.ACTIVE,
                    "system"
            );
            user.setEnabled(true);
            userRepository.save(user);
            log.info("Idempotently seeded user '{}' with role {}", username, role);
        }
    }

    private void seedProjectsAndTasks() {
        // Guard: skip if data already seeded
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
        task1.setUpdatedBy("engineer");

        Task task2 = new Task(
                metroProject,
                "Reinforced Concrete Pillar Pouring",
                TaskStatus.IN_PROGRESS,
                65,
                LocalDate.now().plusDays(20),
                "Pillars 1 to 8 completed; pouring pillars 9-12 currently underway."
        );
        task2.setLastUpdated(LocalDateTime.now().minusDays(1));
        task2.setUpdatedBy("engineer");

        Task task3 = new Task(
                metroProject,
                "Structural Steel Truss Assembly",
                TaskStatus.BLOCKED,
                20,
                LocalDate.now().plusDays(10),
                "Delayed awaiting shipment of high-tensile bolts from supplier."
        );
        task3.setLastUpdated(LocalDateTime.now().minusDays(3));
        task3.setUpdatedBy("manager");

        Task task4 = new Task(
                highwayProject,
                "Pre-stressed Bridge Girder Placement",
                TaskStatus.IN_PROGRESS,
                40,
                LocalDate.now().minusDays(5),
                "Crane mechanical breakdown caused a 5-day schedule overrun."
        );
        task4.setLastUpdated(LocalDateTime.now().minusDays(5));
        task4.setUpdatedBy("engineer");

        Task task5 = new Task(
                highwayProject,
                "Asphalt Paving & Surface Leveling",
                TaskStatus.NOT_STARTED,
                0,
                LocalDate.now().plusDays(45),
                "Scheduled to begin immediately after girder inspection sign-off."
        );
        task5.setLastUpdated(LocalDateTime.now().minusDays(10));
        task5.setUpdatedBy("system");

        taskRepository.save(task1);
        taskRepository.save(task2);
        taskRepository.save(task3);
        taskRepository.save(task4);
        taskRepository.save(task5);
        log.info("Idempotently seeded 2 projects and 5 initial tasks");
    }

    private void migrateExistingUserData() {
        List<User> existingUsers = userRepository.findAll();
        for (User u : existingUsers) {
            boolean modified = false;
            if (u.getFullName() == null || u.getFullName().isBlank()) {
                String capitalized = u.getUsername().substring(0, 1).toUpperCase() + u.getUsername().substring(1);
                u.setFullName(capitalized + " User");
                modified = true;
            }
            if (u.getEmail() == null || u.getEmail().isBlank()) {
                u.setEmail(u.getUsername().toLowerCase() + "@construction.local");
                modified = true;
            }
            if (u.getJobStatus() == null) {
                u.setJobStatus(JobStatus.ACTIVE);
                modified = true;
            }
            if (u.getCreatedBy() == null || u.getCreatedBy().isBlank()) {
                u.setCreatedBy("system");
                modified = true;
            }
            if (u.getCreatedAt() == null) {
                u.setCreatedAt(LocalDateTime.now());
                modified = true;
            }
            if (u.getLastUpdated() == null) {
                u.setLastUpdated(LocalDateTime.now());
                modified = true;
            }
            if (modified) {
                userRepository.save(u);
            }
        }
    }
}
