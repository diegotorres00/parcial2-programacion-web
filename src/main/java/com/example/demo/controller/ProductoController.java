package com.example.demo.controller;

import com.example.demo.dto.request.RegistroProductosRequest;
import com.example.demo.dto.response.RegistroProductosResponse;
import com.example.demo.service.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Productos", description = "Registro atómico de productos de investigación de un semillero")
@RestController
@RequestMapping("/api/semilleros/{semilleroId}/productos")
public class ProductoController {

    private final ProductoService service;

    public ProductoController(ProductoService service) {
        this.service = service;
    }

    @Operation(summary = "Registrar productos",
               description = "Registra de 1 a 10 productos del semillero en una sola transacción.")
    @PostMapping
    public ResponseEntity<RegistroProductosResponse> registrar(
            @PathVariable Long semilleroId,
            @Valid @RequestBody RegistroProductosRequest request) { // <-- @Valid activa Bean Validation (P2 y P3)
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.registrarProductos(semilleroId, request));
    }
}
