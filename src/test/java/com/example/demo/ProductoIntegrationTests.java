package com.example.demo;

import com.example.demo.repository.ProductoRepository;
import com.example.demo.repository.SemilleroRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Usa el semillero SIAWEB de V2 (integrantes 1152001, 1152002, 1152003)
@SpringBootTest
class ProductoIntegrationTests {

    @Autowired private WebApplicationContext context;
    @Autowired private SemilleroRepository semilleroRepository;
    @Autowired private ProductoRepository productoRepository;

    private MockMvc mockMvc;
    private Long semilleroId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        productoRepository.deleteAll();
        semilleroId = semilleroRepository.findBySigla("SIAWEB").orElseThrow().getId();
    }

    private String producto(String titulo, String tipo, int anio, String autor) {
        return String.format("{\"titulo\":\"%s\",\"tipo\":\"%s\",\"anio\":%d,\"codigoAutor\":\"%s\"}",
                titulo, tipo, anio, autor);
    }

    private String body(String... productos) {
        return "{\"productos\":[" + String.join(",", productos) + "]}";
    }

    @Test
    @DisplayName("Todo válido -> 201 y se guardan los 3")
    void valido_201() throws Exception {
        mockMvc.perform(post("/api/semilleros/" + semilleroId + "/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(
                                producto("Ponencia A", "ponencia", 2024, "1152001"),
                                producto("Articulo B", "articulo", 2025, "1152002"),
                                producto("Software C", "software", 2025, "1152003"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.semilleroId").value(semilleroId))
                .andExpect(jsonPath("$.totalGuardados").value(3));
        assertThat(productoRepository.countBySemilleroId(semilleroId)).isEqualTo(3);
    }

    @Test
    @DisplayName("Tercero con autor de otro semillero -> 400 y no se guarda ninguno")
    void tercerAutorInvalido_400_rollback() throws Exception {
        long antes = productoRepository.countBySemilleroId(semilleroId);
        mockMvc.perform(post("/api/semilleros/" + semilleroId + "/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(
                                producto("Ponencia A", "ponencia", 2024, "1152001"),
                                producto("Articulo B", "articulo", 2025, "1152002"),
                                producto("Software C", "software", 2025, "1153001")))) // 1153001 es de SIABD
                .andExpect(status().isBadRequest());
        assertThat(productoRepository.countBySemilleroId(semilleroId)).isEqualTo(antes);
    }

    @Test
    @DisplayName("Semillero inexistente -> 404")
    void semilleroNoExiste_404() throws Exception {
        mockMvc.perform(post("/api/semilleros/999999/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(producto("X", "ponencia", 2024, "1152001"))))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Lista vacía -> 400 (P2)")
    void listaVacia_400() throws Exception {
        mockMvc.perform(post("/api/semilleros/" + semilleroId + "/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productos\":[]}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Año y tipo inválidos -> 400 (P3)")
    void anioYTipoInvalidos_400() throws Exception {
        mockMvc.perform(post("/api/semilleros/" + semilleroId + "/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(producto("X", "libro", 1999, "1152001"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Título repetido en la petición -> 400 (P5)")
    void tituloRepetidoEnPeticion_400() throws Exception {
        mockMvc.perform(post("/api/semilleros/" + semilleroId + "/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(
                                producto("Mismo titulo", "ponencia", 2024, "1152001"),
                                producto("mismo TITULO", "articulo", 2025, "1152002"))))
                .andExpect(status().isBadRequest());
        assertThat(productoRepository.countBySemilleroId(semilleroId)).isZero();
    }
}
