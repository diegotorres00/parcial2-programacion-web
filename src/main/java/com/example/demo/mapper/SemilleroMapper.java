package com.example.demo.mapper;

import com.example.demo.dto.request.SemilleroRequest;
import com.example.demo.entity.Semillero;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {DirectorMapper.class, IntegranteMapper.class})
public interface SemilleroMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "director", ignore = true)
    @Mapping(target = "lineas", ignore = true)
    @Mapping(target = "integrantes", ignore = true)
    Semillero toEntity(SemilleroRequest request);
}
