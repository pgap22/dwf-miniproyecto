package sv.edu.udb.data_collector.repository;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import jakarta.persistence.EntityManager;
import sv.edu.udb.data_collector.domain.MemberRole;
import sv.edu.udb.data_collector.domain.User;
import sv.edu.udb.data_collector.domain.Workspace;
import sv.edu.udb.data_collector.domain.WorkspaceUser;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@DisplayName("WorkspaceUserRepository")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class WorkspaceUserRepositoryTest {

    @Autowired
    private WorkspaceUserRepository repository;

    @Autowired
    private EntityManager em;

    private Workspace ws;
    private User userA;
    private User userB;

    @BeforeEach
    void setUp() {
        // Arrange (AAA): crear y persistir datos base
        ws = persistWorkspace("Data Team");
        userA = persistUser("a@example.com", "Alice");
        userB = persistUser("b@example.com", "Bob");

        persistMembership(ws, userA, MemberRole.MEMBER);
        // userB todavía no es miembro para probar exists/find negativos
        em.flush();  // fuerza escritura para que los IDs estén listos
        em.clear();  // limpia el PC y evitar falsos positivos por caché
    }

    @Test
    @DisplayName("findByWorkspaceId() devuelve los miembros del workspace")
    void findByWorkspaceId_returnsMembers() {
        // Act
        List<WorkspaceUser> members = repository.findByWorkspaceId(ws.getId());

        // Assert
        assertThat(members)
                .hasSize(1)
                .allSatisfy(m -> {
                    assertThat(m.getWorkspace().getId()).isEqualTo(ws.getId());
                    assertThat(m.getUser().getId()).isEqualTo(userA.getId());
                    assertThat(m.getRole()).isEqualTo(MemberRole.MEMBER);
                });
    }

    @Test
    @DisplayName("existsByWorkspaceIdAndUserId() true cuando el usuario pertenece al workspace")
    void exists_returnsTrueWhenMemberExists() {
        // Act
        boolean existsA = repository.existsByWorkspaceIdAndUserId(ws.getId(), userA.getId());
        boolean existsB = repository.existsByWorkspaceIdAndUserId(ws.getId(), userB.getId());

        // Assert
        assertThat(existsA).isTrue();
        assertThat(existsB).isFalse();
    }

    @Test
    @DisplayName("findByWorkspaceIdAndUserId() devuelve Optional con el miembro cuando existe")
    void findByWorkspaceIdAndUserId_returnsMember() {
        // Act
        var found = repository.findByWorkspaceIdAndUserId(ws.getId(), userA.getId());

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getRole()).isEqualTo(MemberRole.MEMBER);
    }

    @Test
    @DisplayName("deleteByWorkspaceIdAndUserId() borra y devuelve 1 cuando elimina al miembro")
    void deleteByWorkspaceIdAndUserId_deletesOne() {
        // Act
        long deleted = repository.deleteByWorkspaceIdAndUserId(ws.getId(), userA.getId());
        em.flush();
        em.clear();

        // Assert
        assertThat(deleted).isEqualTo(1);
        assertThat(repository.existsByWorkspaceIdAndUserId(ws.getId(), userA.getId())).isFalse();
    }

    @Test
    @DisplayName("updateRoleByWorkspaceIdAndUserId() actualiza el rol y devuelve filas afectadas")
    void updateRoleByWorkspaceIdAndUserId_updatesRole() {
        // Act
        int updated = repository.updateRoleByWorkspaceIdAndUserId(ws.getId(), userA.getId(), MemberRole.ADMIN);
        em.flush();
        em.clear();

        // Assert
        assertThat(updated).isEqualTo(1);

        var refreshed = repository.findByWorkspaceIdAndUserId(ws.getId(), userA.getId()).orElseThrow();
        assertThat(refreshed.getRole()).isEqualTo(MemberRole.ADMIN);
    }

    @Test
    @DisplayName("No permite duplicar miembro por (workspaceId, userId) único")
    void uniqueConstraint_blocksDuplicates() {
        // Arrange
        var another = WorkspaceUser.builder()
                .workspace(em.getReference(Workspace.class, ws.getId()))
                .user(em.getReference(User.class, userA.getId()))
                .role(MemberRole.MEMBER)
                .build();

        // Act + Assert
        assertThatThrownBy(() -> {
            repository.saveAndFlush(another); // dispara la violación de restricción
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    // ========== Helpers (fáciles de poner breakpoints) ==========

    private Workspace persistWorkspace(String name) {
        Workspace w = new Workspace();
        w.setName(name);
        w.setCreatedAt(Instant.now());
        em.persist(w);
        return w;
    }

    private User persistUser(String email, String name) {
        User u = new User();
        u.setEmail(email);
        u.setName(name);
        u.setPasswordHash("demo");
        em.persist(u);
        return u;
    }

    private WorkspaceUser persistMembership(Workspace w, User u, MemberRole role) {
        WorkspaceUser m = WorkspaceUser.builder()
                .workspace(w)
                .user(u)
                .role(role)
                .build();
        em.persist(m);
        return m;
    }
}
