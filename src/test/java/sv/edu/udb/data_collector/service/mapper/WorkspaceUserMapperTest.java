package sv.edu.udb.data_collector.service.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import sv.edu.udb.data_collector.controller.response.MemberResponse;
import sv.edu.udb.data_collector.domain.MemberRole;
import sv.edu.udb.data_collector.domain.User;
import sv.edu.udb.data_collector.domain.Workspace;
import sv.edu.udb.data_collector.domain.WorkspaceUser;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("WorkspaceUserMapper")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class WorkspaceUserMapperTest {

    // MapStruct genera la implementación; la obtenemos directamente
    private final WorkspaceUserMapper mapper = Mappers.getMapper(WorkspaceUserMapper.class);

    @Test
    @DisplayName("toResponse mapea todos los campos y relaciones correctamente")
    void toResponse_maps_all_fields() {
        // ---------- Arrange ----------
        Workspace workspace = Workspace.builder()
                .name("Data Team")
                .createdAt(Instant.now())
                .build();

        User user = User.builder()
                .email("alice@example.com")
                .name("Alice")
                .passwordHash("demo")
                .build();

        Instant joined = Instant.now();

        WorkspaceUser entity = WorkspaceUser.builder()
                .workspace(workspace)
                .user(user)
                .role(MemberRole.ADMIN)
                .createdAt(joined)
                .build();

        // ---------- Act ----------
        MemberResponse dto = mapper.toResponse(entity);

        // ---------- Assert ----------
        assertThat(dto).isNotNull();
        // Ids pueden ser null si no han sido persistidos, pero los campos anidados
        // deben coincidir
        assertThat(dto.getUserId()).isEqualTo(user.getId());
        assertThat(dto.getEmail()).isEqualTo("alice@example.com");
        assertThat(dto.getName()).isEqualTo("Alice");
        assertThat(dto.getRole()).isEqualTo(MemberRole.ADMIN.name());
        assertThat(dto.getJoinedAt()).isEqualTo(joined);
    }

    @Test
    @DisplayName("toResponse retorna null cuando la entidad es null")
    void toResponse_returns_null_when_entity_is_null() {
        // ---------- Arrange ----------
        WorkspaceUser entity = null;

        // ---------- Act ----------
        MemberResponse dto = mapper.toResponse(entity);

        // ---------- Assert ----------
        assertThat(dto).isNull();
    }
}
