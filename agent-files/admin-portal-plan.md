================================================================================
         SPRING BOOT PORTFOLIO WEB APP — ARCHITECTURE & IMPLEMENTATION PLAN
================================================================================

================================================================================
1. DEPENDENCIES (from https://start.spring.io/)
================================================================================

Settings:
  - Project:       Maven
  - Language:       Java
  - Spring Boot:    3.4.x (latest stable)
  - Java:           21
  - Packaging:      Jar

Dependencies to select:

  Dependency                              Purpose
  ─────────────────────────────────────── ─────────────────────────────────────────
  Spring Web                              MVC controllers, serving pages
  Spring Security                         Form login, route protection for /admin/**
  Spring Data JPA                         Repository layer, entity persistence
  Thymeleaf                               Server-side HTML templating
  Thymeleaf Extras Spring Security 6      sec:authorize tags in templates
  Spring Boot Starter Validation          @Valid, @NotBlank on form DTOs
  Lombok                                  Boilerplate reduction
  PostgreSQL Driver                       Production database
  H2 Database                             Dev/test in-memory database
  Spring Boot DevTools                    Live reload during development
  Spring Boot Starter Test                Unit/integration testing (auto-included)
  Spring Security Test                    @WithMockUser, security test utilities


================================================================================
2. PROJECT FOLDER STRUCTURE
================================================================================

src/main/java/com/portfolio/
├── PortfolioApplication.java
├── config/
│   └── SecurityConfig.java
├── model/
│   └── Project.java
├── repository/
│   └── ProjectRepository.java
├── service/
│   └── ProjectService.java
├── controller/
│   ├── PublicController.java
│   └── AdminController.java
└── dto/
    └── ProjectForm.java

src/main/resources/
├── application.properties
├── application-prod.properties
├── static/
│   ├── css/
│   │   └── styles.css
│   └── images/
└── templates/
    ├── public/
    │   ├── home.html
    │   └── project-detail.html
    ├── admin/
    │   ├── login.html
    │   ├── dashboard.html
    │   └── project-form.html
    └── fragments/
        ├── header.html
        └── footer.html

src/test/java/com/portfolio/
├── controller/
│   ├── PublicControllerTest.java
│   └── AdminControllerTest.java
├── service/
│   └── ProjectServiceTest.java
└── repository/
    └── ProjectRepositoryTest.java


================================================================================
3. ROUTE MAP
================================================================================

Method  URL                           Auth     Purpose
─────── ───────────────────────────── ──────── ──────────────────────────────────
GET     /                             Public   Portfolio homepage / project gallery
GET     /projects/{id}                Public   Single project detail page
GET     /admin/login                  Public   Admin login form (only public admin URL)
POST    /admin/login                  Public   Spring Security processes login
GET     /admin/dashboard              ADMIN    List all projects with actions
GET     /admin/projects/new           ADMIN    Show create project form
GET     /admin/projects/{id}/edit     ADMIN    Show edit project form
POST    /admin/projects/save          ADMIN    Create or update a project
POST    /admin/projects/{id}/delete   ADMIN    Delete a project
POST    /admin/logout                 ADMIN    Sign out, redirect to login


================================================================================
4. FILE-BY-FILE SPECIFICATION
================================================================================

────────────────────────────────────────────────────────────────────────────────
4.1  PortfolioApplication.java
────────────────────────────────────────────────────────────────────────────────
Purpose:    Application entry point
Annotations:
  - @SpringBootApplication
Methods:
  - main(String[] args)  →  SpringApplication.run(...)

────────────────────────────────────────────────────────────────────────────────
4.2  config/SecurityConfig.java
────────────────────────────────────────────────────────────────────────────────
Purpose:    Configure form-based login, route protection, password encoding
Annotations:
  - @Configuration
  - @EnableWebSecurity
  - @Value("${app.admin.username}")  on field for admin username
  - @Value("${app.admin.password}")  on field for BCrypt-hashed admin password
Beans to define (each annotated with @Bean):
  - PasswordEncoder passwordEncoder()
      → return new BCryptPasswordEncoder()
  - UserDetailsService userDetailsService()
      → return InMemoryUserDetailsManager with a single user
      → User.builder() with username, pre-hashed password, roles("ADMIN")
  - SecurityFilterChain filterChain(HttpSecurity http)
      → authorizeHttpRequests:
          permitAll: "/", "/projects/**", "/css/**", "/images/**", "/js/**"
          permitAll: "/h2-console/**"  (dev only, remove in prod)
          hasRole("ADMIN"): "/admin/**"
          anyRequest().authenticated()
      → formLogin:
          loginPage("/admin/login")
          loginProcessingUrl("/admin/login")
          defaultSuccessUrl("/admin/dashboard", true)
          failureUrl("/admin/login?error=true")
          permitAll()
      → logout:
          logoutUrl("/admin/logout")
          logoutSuccessUrl("/admin/login?logout=true")
          permitAll()
      → headers: frameOptions sameOrigin (for H2 console in dev)
      → csrf: ignoringRequestMatchers("/h2-console/**") (dev only)

────────────────────────────────────────────────────────────────────────────────
4.3  model/Project.java
────────────────────────────────────────────────────────────────────────────────
Purpose:    JPA entity representing a portfolio project
Annotations:
  - @Entity
  - @Table(name = "projects")
  - @Getter, @Setter                       (Lombok)
  - @NoArgsConstructor, @AllArgsConstructor (Lombok)
  - @Builder                               (Lombok)
Fields:
  - Long id              → @Id, @GeneratedValue(strategy = GenerationType.IDENTITY)
  - String title         → @Column(nullable = false)
  - String description   → @Column(length = 2000)
  - String thumbnailUrl
  - String liveDemoUrl
  - String sourceCodeUrl
  - LocalDate dateAdded  → @Column(nullable = false)
Lifecycle callback:
  - @PrePersist method   → sets dateAdded to LocalDate.now() if null

────────────────────────────────────────────────────────────────────────────────
4.4  repository/ProjectRepository.java
────────────────────────────────────────────────────────────────────────────────
Purpose:    Data access layer for Project entity
Extends:    JpaRepository<Project, Long>
            (no annotation needed — Spring Data auto-detects interfaces
             extending JpaRepository within the component scan)
Custom query methods:
  - List<Project> findAllByOrderByDateAddedDesc()
    (Spring Data derived query — newest projects first)

────────────────────────────────────────────────────────────────────────────────
4.5  dto/ProjectForm.java
────────────────────────────────────────────────────────────────────────────────
Purpose:    Form-binding DTO for create/edit with validation
Annotations:
  - @Getter, @Setter                       (Lombok)
  - @NoArgsConstructor, @AllArgsConstructor (Lombok)
Fields:
  - Long id                 → null = create, non-null = edit (hidden field)
  - String title            → @NotBlank(message = "Title is required")
                              @Size(max = 200)
  - String description      → @Size(max = 2000, message = "...")
  - String thumbnailUrl     → no validation (optional)
  - String liveDemoUrl      → no validation (optional)
  - String sourceCodeUrl    → no validation (optional)

────────────────────────────────────────────────────────────────────────────────
4.6  service/ProjectService.java
────────────────────────────────────────────────────────────────────────────────
Purpose:    Business logic layer between controllers and repository
Annotations:
  - @Service
  - @RequiredArgsConstructor                (Lombok — constructor injection)
  - @Transactional(readOnly = true)         (class-level default)
Dependencies (final fields, injected via constructor):
  - ProjectRepository projectRepository
Methods:
  - List<Project> findAll()
      → delegates to projectRepository.findAllByOrderByDateAddedDesc()
  - Project findById(Long id)
      → projectRepository.findById(id).orElseThrow(...)
      → throw IllegalArgumentException if not found
  - @Transactional Project save(ProjectForm form)
      → if form.id != null → load existing entity (edit)
      → if form.id == null → create new Project
      → map DTO fields to entity, call projectRepository.save(entity)
  - @Transactional void delete(Long id)
      → projectRepository.deleteById(id)

────────────────────────────────────────────────────────────────────────────────
4.7  controller/PublicController.java
────────────────────────────────────────────────────────────────────────────────
Purpose:    Public-facing pages (no auth required)
Annotations:
  - @Controller
  - @RequiredArgsConstructor                (Lombok)
Dependencies (final fields):
  - ProjectService projectService
Handler methods:
  - @GetMapping("/") String home(Model model)
      → model.addAttribute("projects", projectService.findAll())
      → return "public/home"
  - @GetMapping("/projects/{id}") String projectDetail(@PathVariable Long id, Model)
      → model.addAttribute("project", projectService.findById(id))
      → return "public/project-detail"

────────────────────────────────────────────────────────────────────────────────
4.8  controller/AdminController.java
────────────────────────────────────────────────────────────────────────────────
Purpose:    Admin CRUD operations (requires ADMIN role)
Annotations:
  - @Controller
  - @RequestMapping("/admin")
  - @RequiredArgsConstructor                (Lombok)
Dependencies (final fields):
  - ProjectService projectService
Handler methods:
  - @GetMapping("/login") String loginPage()
      → return "admin/login"
  - @GetMapping("/dashboard") String dashboard(Model)
      → model.addAttribute("projects", projectService.findAll())
      → return "admin/dashboard"
  - @GetMapping("/projects/new") String newProjectForm(Model)
      → model.addAttribute("projectForm", new ProjectForm())
      → return "admin/project-form"
  - @GetMapping("/projects/{id}/edit") String editProjectForm(@PathVariable Long id, Model)
      → load project via service, map to ProjectForm DTO
      → model.addAttribute("projectForm", form)
      → return "admin/project-form"
  - @PostMapping("/projects/save") String saveProject(
        @Valid @ModelAttribute("projectForm") ProjectForm form,
        BindingResult result,
        RedirectAttributes redirectAttributes)
      → if result.hasErrors() → return "admin/project-form"
      → projectService.save(form)
      → redirectAttributes.addFlashAttribute("successMessage", "Project saved!")
      → return "redirect:/admin/dashboard"
  - @PostMapping("/projects/{id}/delete") String deleteProject(
        @PathVariable Long id,
        RedirectAttributes redirectAttributes)
      → projectService.delete(id)
      → redirectAttributes.addFlashAttribute("successMessage", "Project deleted.")
      → return "redirect:/admin/dashboard"


================================================================================
5. TEMPLATES SPECIFICATION
================================================================================

All templates use Thymeleaf (xmlns:th="http://www.thymeleaf.org").

────────────────────────────────────────────────────────────────────────────────
5.1  templates/admin/login.html
────────────────────────────────────────────────────────────────────────────────
- Standard HTML form posting to th:action="@{/admin/login}" (method=post)
- Fields: username (text), password (password), submit button
- Conditional messages:
    th:if="${param.error}"   → "Invalid username or password."
    th:if="${param.logout}"  → "You have been signed out."

────────────────────────────────────────────────────────────────────────────────
5.2  templates/admin/dashboard.html
────────────────────────────────────────────────────────────────────────────────
- Navigation links: "+ New Project" → /admin/projects/new, "Logout" → /admin/logout
- Flash message display: th:if="${successMessage}"
- Table listing all projects (th:each="project : ${projects}"):
    Columns: Title, Date Added, Actions (Edit link, Delete form/button)
    Edit link  → /admin/projects/{id}/edit
    Delete     → POST form to /admin/projects/{id}/delete with confirm dialog

────────────────────────────────────────────────────────────────────────────────
5.3  templates/admin/project-form.html
────────────────────────────────────────────────────────────────────────────────
- Dynamic heading: "Edit Project" vs "New Project" based on projectForm.id
- Form posting to th:action="@{/admin/projects/save}" with th:object="${projectForm}"
- Hidden field: th:field="*{id}"
- Visible fields: title, description (textarea), thumbnailUrl, liveDemoUrl, sourceCodeUrl
- Validation error display: th:if="${#fields.hasErrors('title')}" th:errors="*{title}"
- Buttons: Save, Cancel (link to /admin/dashboard)

────────────────────────────────────────────────────────────────────────────────
5.4  templates/public/home.html
────────────────────────────────────────────────────────────────────────────────
- Header with portfolio title and subtitle
- Project grid/cards (th:each="project : ${projects}"):
    - Thumbnail image (th:if="${project.thumbnailUrl}")
    - Title
    - Truncated description: th:text="${#strings.abbreviate(project.description, 150)}"
    - "View Details" link → /projects/{id}
- Empty state message when no projects exist

────────────────────────────────────────────────────────────────────────────────
5.5  templates/public/project-detail.html
────────────────────────────────────────────────────────────────────────────────
- Back link → /
- Project title, date, thumbnail image, full description
- Conditional links:
    "Live Demo"   → th:if="${project.liveDemoUrl}"  th:href="${project.liveDemoUrl}"
    "Source Code"  → th:if="${project.sourceCodeUrl}" th:href="${project.sourceCodeUrl}"

────────────────────────────────────────────────────────────────────────────────
5.6  templates/fragments/header.html & footer.html
────────────────────────────────────────────────────────────────────────────────
- Reusable Thymeleaf fragments (th:fragment="header" / th:fragment="footer")
- Included in other templates via th:replace="~{fragments/header :: header}"
- Header: nav bar with portfolio name, optional admin link (sec:authorize)
- Footer: copyright, social links


================================================================================
6. CONFIGURATION FILES
================================================================================

────────────────────────────────────────────────────────────────────────────────
6.1  application.properties (dev profile, H2)
────────────────────────────────────────────────────────────────────────────────
- spring.datasource.url=jdbc:h2:mem:portfolio
- spring.datasource.driver-class-name=org.h2.Driver
- spring.h2.console.enabled=true
- spring.jpa.hibernate.ddl-auto=create-drop
- spring.jpa.show-sql=true
- app.admin.username=admin
- app.admin.password=<BCrypt hash of your chosen password>
  (generate with: new BCryptPasswordEncoder().encode("yourPassword"))

────────────────────────────────────────────────────────────────────────────────
6.2  application-prod.properties (production, PostgreSQL)
────────────────────────────────────────────────────────────────────────────────
- spring.datasource.url=jdbc:postgresql://localhost:5432/portfolio
- spring.datasource.username=${DB_USERNAME}
- spring.datasource.password=${DB_PASSWORD}
- spring.jpa.hibernate.ddl-auto=validate
- spring.h2.console.enabled=false
- app.admin.username=${ADMIN_USERNAME}
- app.admin.password=${ADMIN_PASSWORD_HASH}


================================================================================
7. IMPLEMENTATION PHASES
================================================================================

PHASE 1 — Skeleton & Database
  Files:  PortfolioApplication.java, Project.java, ProjectRepository.java,
          application.properties
  Goal:   App boots, entity created in H2, H2 console accessible at /h2-console
  Test:   Run app → navigate to /h2-console → verify PROJECTS table exists

PHASE 2 — Security
  Files:  SecurityConfig.java, templates/admin/login.html
  Goal:   /admin/** routes require login, public routes are open
  Test:   Visit /admin/dashboard → redirected to /admin/login
          Login with admin credentials → lands on /admin/dashboard (empty page OK)
          Visit / → accessible without login

PHASE 3 — Service & DTO Layer
  Files:  ProjectService.java, ProjectForm.java
  Goal:   Business logic layer with create/read/update/delete operations
  Test:   Write unit tests for ProjectService (mock the repository)

PHASE 4 — Admin Controller & Templates
  Files:  AdminController.java, dashboard.html, project-form.html
  Goal:   Full CRUD through the browser as an authenticated admin
  Test:   Login → create project → see it in dashboard → edit → delete

PHASE 5 — Public Controller & Templates
  Files:  PublicController.java, home.html, project-detail.html,
          fragments/header.html, fragments/footer.html
  Goal:   Anonymous users see the portfolio gallery and project details
  Test:   Open / in incognito → see project cards → click one → see detail page

PHASE 6 — Polish & Production Readiness
  Tasks:  CSS styling, responsive design, error pages 