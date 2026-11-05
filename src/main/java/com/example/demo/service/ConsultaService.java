package com.example.demo.service;

import com.example.demo.dto.response.Resumenes.IntegranteResumen;
import com.example.demo.dto.response.Resumenes.ProductoResumen;
import com.example.demo.dto.response.Resumenes.SemilleroResumen;
import com.example.demo.exception.RecursoNoEncontradoException;
import com.example.demo.repository.IntegranteRepository;
import com.example.demo.repository.ProductoRepository;
import com.example.demo.repository.SemilleroRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true) // necesario: el autor del producto es LAZY y open-in-view=false
public class ConsultaService {

    private final SemilleroRepository semilleroRepository;
    private final IntegranteRepository integranteRepository;
    private final ProductoRepository productoRepository;

    public ConsultaService(SemilleroRepository s, IntegranteRepository i, ProductoRepository p) {
        this.semilleroRepository = s;
        this.integranteRepository = i;
        this.productoRepository = p;
    }

    public List<SemilleroResumen> semilleros() {
        return semilleroRepository.findAll().stream()
                .map(s -> new SemilleroResumen(s.getId(), s.getSigla(), s.getNombre())).toList();
    }

    public List<IntegranteResumen> integrantes(Long semilleroId) {
        existe(semilleroId);
        return integranteRepository.findBySemilleroId(semilleroId).stream()
                .map(i -> new IntegranteResumen(i.getCodigo(), i.getNombre(), i.getPrograma(), i.isEsLider())).toList();
    }

    public List<ProductoResumen> productos(Long semilleroId) {
        existe(semilleroId);
        return productoRepository.findBySemilleroId(semilleroId).stream()
                .map(p -> new ProductoResumen(p.getId(), p.getTitulo(), p.getTipo(), p.getAnio(),
                        p.getAutor().getCodigo(), p.getAutor().getNombre())).toList();
    }

    private void existe(Long id) {
        if (!semilleroRepository.existsById(id)) {
            throw new RecursoNoEncontradoException("No existe el semillero con id " + id);
        }
    }
}
