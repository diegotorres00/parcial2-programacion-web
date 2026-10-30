package com.example.demo.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ProductoRequest(
        @NotBlank(message = "El título es obligatorio")
        @Size(max = 200, message = "El título no puede superar 200 caracteres")
        @Schema(description = "Título del producto", example = "Arquitectura de microservicios en la UFPS")
        String titulo,

        @NotBlank(message = "El tipo es obligatorio")
        @Pattern(regexp = "(?i)ponencia|articulo|software",
                 message = "El tipo debe ser ponencia, articulo o software")
        @Schema(description = "ponencia, articulo o software", example = "articulo")
        String tipo,

        @NotNull(message = "El año es obligatorio")
        @Min(value = 2000, message = "El año debe ser mínimo 2000")
        @Max(value = 2100, message = "El año debe ser máximo 2100")
        @Schema(description = "Año de publicación", example = "2025")
        Integer anio,

        @NotBlank(message = "El código del autor es obligatorio")
        @Schema(description = "Código de un integrante del semillero", example = "1152002")
        String codigoAutor
) {}
