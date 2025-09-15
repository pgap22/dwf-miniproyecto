package sv.edu.udb.data_collector.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sv.edu.udb.data_collector.domain.WorkspaceMember;

import java.util.List;
import java.util.Optional;

public interface WorkspaceMemberRepository extends JpaRepository<WorkspaceMember, String> {

    List<WorkspaceMember> findByWorkspaceId(String workspaceId);

    boolean existsByWorkspaceIdAndUserId(String workspaceId, String userId);

    Optional<WorkspaceMember> findByWorkspaceIdAndUserId(String workspaceId, String userId);
}
