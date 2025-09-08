package sv.edu.udb.data_collector.service.mapper;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import sv.edu.udb.data_collector.controller.request.UserRequest;
import sv.edu.udb.data_collector.controller.response.UserResponse;
import sv.edu.udb.data_collector.domain.User;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {

    private final UserMapper mapper = Mappers.getMapper(UserMapper.class);

    @Test
    void toUser_maps_basic_fields_and_password_to_hash_field() {
        UserRequest req = UserRequest.builder()
                .name("Bart")
                .email("bart@pukis.com")
                .password("123")
                .build();
        User u = mapper.toUser(req);

        assertThat(u.getId()).isNull();
        assertThat(u.getName()).isEqualTo("Bart");
        assertThat(u.getEmail()).isEqualTo("bart@pukis.com");
        // El mapper mueve password -> passwordHash (hash real se aplica en el servicio)
        assertThat(u.getPasswordHash()).isEqualTo("123");
    }

    @Test
    void toUserResponse_maps_fields() {
        User u = new User();
        u.setId("1");
        u.setName("Bart");
        u.setEmail("bart@pukis.com");
        u.setPasswordHash("HASH");

        UserResponse resp = mapper.toUserResponse(u);

        assertThat(resp.getId()).isEqualTo("1");
        assertThat(resp.getName()).isEqualTo("Bart");
        assertThat(resp.getEmail()).isEqualTo("bart@pukis.com");
    }
}
