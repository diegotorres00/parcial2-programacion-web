package com.example.demo.repository;

import com.example.demo.entity.Producto;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {
    List<Producto> findBySemilleroId(Long semilleroId);
    long countBySemilleroId(Long semilleroId);
}
