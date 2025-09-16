package sv.edu.udb.data_collector.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import sv.edu.udb.data_collector.domain.MemberRole;
import sv.edu.udb.data_collector.domain.WorkspaceUser;

import java.util.List;
import java.util.Optional;

public interface WorkspaceUserRepository extends JpaRepository<WorkspaceUser, String> {

    List<WorkspaceUser> findByWorkspaceId(String workspaceId);

    boolean existsByWorkspaceIdAndUserId(String workspaceId, String userId);

    Optional<WorkspaceUser> findByWorkspaceIdAndUserId(String workspaceId, String userId);

    // elimina y devuelve cuántas filas se borraron
    long deleteByWorkspaceIdAndUserId(String workspaceId, String userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update WorkspaceUser wm
            set wm.role = :role
            where wm.workspace.id = :workspaceId
              and wm.user.id = :userId
            """)
    int updateRoleByWorkspaceIdAndUserId(String workspaceId, String userId, MemberRole role);
}
