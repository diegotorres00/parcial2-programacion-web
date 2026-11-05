package com.example.demo.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record RegistroProductosRequest(
        // P2: entre 1 y 10 productos.
        // @Valid hace que Bean Validation entre a validar cada ProductoRequest (P3).
        @NotNull(message = "La lista de productos es obligatoria")
        @Size(min = 1, max = 10, message = "Debe enviar entre 1 y 10 productos")
        List<@Valid ProductoRequest> productos
) {}
