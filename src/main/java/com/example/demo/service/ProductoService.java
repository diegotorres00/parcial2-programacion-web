package com.example.demo.service;

import com.example.demo.dto.request.ProductoRequest;
import com.example.demo.dto.request.RegistroProductosRequest;
import com.example.demo.dto.response.RegistroProductosResponse;
import com.example.demo.entity.Integrante;
import com.example.demo.entity.Producto;
import com.example.demo.entity.Semillero;
import com.example.demo.exception.RecursoNoEncontradoException;
import com.example.demo.exception.ReglaNegocioException;
import com.example.demo.repository.IntegranteRepository;
import com.example.demo.repository.ProductoRepository;
import com.example.demo.repository.SemilleroRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductoService {

    private final SemilleroRepository semilleroRepository;
    private final IntegranteRepository integranteRepository;
    private final ProductoRepository productoRepository;

    public ProductoService(SemilleroRepository semilleroRepository,
                           IntegranteRepository integranteRepository,
                           ProductoRepository productoRepository) {
        this.semilleroRepository = semilleroRepository;
        this.integranteRepository = integranteRepository;
        this.productoRepository = productoRepository;
    }

    @Transactional
    public RegistroProductosResponse registrarProductos(Long semilleroId, RegistroProductosRequest request) {

        Semillero semillero = semilleroRepository.findById(semilleroId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe el semillero con id " + semilleroId));

        Map<String, Integrante> integrantesPorCodigo = new HashMap<>();
        for (Integrante i : integranteRepository.findBySemilleroId(semilleroId)) {
            integrantesPorCodigo.put(i.getCodigo().trim(), i);
        }

        Set<String> titulosGuardados = new HashSet<>();
        for (Producto p : productoRepository.findBySemilleroId(semilleroId)) {
            titulosGuardados.add(normalizar(p.getTitulo()));
        }

        Set<String> titulosPeticion = new HashSet<>();

        List<Producto> porGuardar = new ArrayList<>();
        int pos = 0;
        for (ProductoRequest pr : request.productos()) {
            pos++;

            Integrante autor = integrantesPorCodigo.get(pr.codigoAutor().trim());
            if (autor == null) {
                throw new ReglaNegocioException("Producto " + pos + ": el código de autor '"
                        + pr.codigoAutor() + "' no pertenece al semillero " + semilleroId);
            }

            String clave = normalizar(pr.titulo());
            if (!titulosPeticion.add(clave)) {
                throw new ReglaNegocioException("Producto " + pos + ": el título '"
                        + pr.titulo() + "' está repetido en la petición");
            }
            if (titulosGuardados.contains(clave)) {
                throw new ReglaNegocioException("Producto " + pos + ": ya existe un producto con el título '"
                        + pr.titulo() + "' en el semillero " + semilleroId);
            }

            porGuardar.add(Producto.builder()
                    .semillero(semillero)
                    .autor(autor)
                    .titulo(pr.titulo().trim())
                    .tipo(pr.tipo().trim().toLowerCase(Locale.ROOT))
                    .anio(pr.anio())
                    .build());
        }

        List<Producto> guardados = productoRepository.saveAll(porGuardar);

        return new RegistroProductosResponse(semilleroId, guardados.size());
    }

    private String normalizar(String titulo) {
        return titulo.trim().toLowerCase(Locale.ROOT);
    }
}
