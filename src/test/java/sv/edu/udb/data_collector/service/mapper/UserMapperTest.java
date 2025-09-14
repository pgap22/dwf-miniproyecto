package sv.edu.udb.data_collector.service.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import sv.edu.udb.data_collector.controller.request.UserRequest;
import sv.edu.udb.data_collector.controller.response.UserResponse;
import sv.edu.udb.data_collector.domain.User;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {

    private final UserMapper mapper = Mappers.getMapper(UserMapper.class);

    @Test
    @DisplayName("toUser: mapea campos básicos y password -> passwordHash")
    void toUser_maps_basic_fields_and_password_to_hash_field() {
        // ---------- Arrange ----------
        String givenName = "Bart";
        String givenEmail = "bart@pukis.com";
        String givenPassword = "123";

        UserRequest request = UserRequest.builder()
                .name(givenName)
                .email(givenEmail)
                .password(givenPassword)
                .build();

        // ---------- Act ----------
        User mappedUser = mapper.toUser(request);

        // ---------- Assert ----------
        assertThat(mappedUser.getId()).as("ID debe ser nulo al mapear").isNull();
        assertThat(mappedUser.getName()).isEqualTo(givenName);
        assertThat(mappedUser.getEmail()).isEqualTo(givenEmail);
        // El mapper copia password en passwordHash (hash real se aplica después)
        assertThat(mappedUser.getPasswordHash()).isEqualTo(givenPassword);
    }

    @Test
    @DisplayName("toUserResponse: mapea campos de dominio a DTO")
    void toUserResponse_maps_fields() {
        // ---------- Arrange ----------
        String userId = "1";
        String userName = "Bart";
        String userEmail = "bart@pukis.com";
        String userHash = "HASH";

        User domainUser = new User();
        domainUser.setId(userId);
        domainUser.setName(userName);
        domainUser.setEmail(userEmail);
        domainUser.setPasswordHash(userHash);

        // ---------- Act ----------
        UserResponse response = mapper.toUserResponse(domainUser);

        // ---------- Assert ----------
        assertThat(response.getId()).isEqualTo(userId);
        assertThat(response.getName()).isEqualTo(userName);
        assertThat(response.getEmail()).isEqualTo(userEmail);
    }
}
