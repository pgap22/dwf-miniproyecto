package sv.edu.udb.data_collector.configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
// 1. Define la información general de la API
@OpenAPIDefinition(
    info = @Info(
        title = "Data Collector API",
        version = "v1.0",
        description = "Documentación de la API para el sistema Data Collector."
    ),
    // 2. Aplica el requisito de seguridad a TODOS los endpoints
    security = @SecurityRequirement(name = "bearerAuth")
)
// 3. Define el esquema de seguridad que se usará
@SecurityScheme(
    name = "bearerAuth", // Un nombre interno para referenciarlo
    type = SecuritySchemeType.HTTP, // El tipo de seguridad
    scheme = "bearer", // El esquema a usar (Bearer Token)
    bearerFormat = "JWT" // Un hint para la UI sobre el formato del token
)
public class OpenApiConfig {
    // Esta clase puede estar vacía, ya que toda la configuración
    // se realiza a través de las anotaciones de arriba.
}