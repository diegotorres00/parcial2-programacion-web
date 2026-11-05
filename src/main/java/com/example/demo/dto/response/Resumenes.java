package com.example.demo.dto.response;

// DTOs de solo lectura para que la página web pueda consultar datos
public final class Resumenes {
    private Resumenes() {}

    public record SemilleroResumen(Long id, String sigla, String nombre) {}
    public record IntegranteResumen(String codigo, String nombre, String programa, boolean esLider) {}
    public record ProductoResumen(Long id, String titulo, String tipo, Integer anio,
                                  String codigoAutor, String nombreAutor) {}
}
