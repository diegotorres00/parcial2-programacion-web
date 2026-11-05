package com.example.demo.controller;

import com.example.demo.dto.response.Resumenes.IntegranteResumen;
import com.example.demo.dto.response.Resumenes.ProductoResumen;
import com.example.demo.dto.response.Resumenes.SemilleroResumen;
import com.example.demo.service.ConsultaService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Consultas", description = "Lecturas que usa la interfaz web")
@RestController
@RequestMapping("/api/semilleros")
public class ConsultaController {

    private final ConsultaService service;

    public ConsultaController(ConsultaService service) {
        this.service = service;
    }

    @GetMapping
    public List<SemilleroResumen> semilleros() { return service.semilleros(); }

    @GetMapping("/{id}/integrantes")
    public List<IntegranteResumen> integrantes(@PathVariable Long id) { return service.integrantes(id); }

    @GetMapping("/{id}/productos")
    public List<ProductoResumen> productos(@PathVariable Long id) { return service.productos(id); }
}
