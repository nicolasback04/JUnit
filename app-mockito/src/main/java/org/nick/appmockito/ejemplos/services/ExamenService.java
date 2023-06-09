package org.nick.appmockito.ejemplos.services;

import org.nick.appmockito.ejemplos.models.Examen;

import java.util.Optional;

public interface ExamenService {
    Optional<Examen> findExamenPorNombre(String nombre);
    Examen findExamenPorNombreConPregunta(String nombre);
    Examen guardar(Examen examen);
}
