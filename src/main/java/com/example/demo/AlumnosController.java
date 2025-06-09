package com.example.demo;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@RestController
public class AlumnosController {

    private final List<Alumno> alumnos = new ArrayList<>();
    private final AtomicLong counter = new AtomicLong();

    public AlumnosController() {
        alumnos.add(new Alumno(counter.incrementAndGet(), "Javier", "Gamarra", 25, true, LocalDate.of(2023, 9, 15)));
        alumnos.add(new Alumno(counter.incrementAndGet(), "Lucía", "García", 30, true, LocalDate.of(2023, 10, 1)));
        alumnos.add(new Alumno(counter.incrementAndGet(), "Pedro", "Gamarra", 22, false, LocalDate.of(2022, 12, 5)));
    }

    @GetMapping("/alumnos")
    public List<Alumno> getAlumnos() {
        return alumnos;
    }

    @GetMapping("/alumnos/{id}")
    public Alumno getAlumnoById(@PathVariable long id) {
        return alumnos.stream()
                .filter(a -> a.id() == id)
                .findFirst()
                .orElseThrow(AlumnoNotFoundException::new);
    }

    @GetMapping("/alumnos/apellido/{apellidos}")
    public List<Alumno> getAlumnosPorApellido(@PathVariable String apellidos) {
        return alumnos.stream()
                .filter(a -> a.apellidos().equalsIgnoreCase(apellidos))
                .toList();
    }

    @PostMapping("/alumnos")
    public Alumno createAlumno(@RequestBody Alumno nuevo) {
        Alumno alumno = new Alumno(
                counter.incrementAndGet(),
                nuevo.name(),
                nuevo.apellidos(),
                nuevo.edad(),
                nuevo.activo(),
                nuevo.fechaIngreso()
        );
        alumnos.add(alumno);
        return alumno;
    }

    @PutMapping("/alumnos/{id}")
    public Alumno updateAlumno(@PathVariable long id, @RequestBody Alumno nuevo) {
        Alumno existente = getAlumnoById(id);
        alumnos.remove(existente);
        Alumno actualizado = new Alumno(
                id,
                nuevo.name(),
                nuevo.apellidos(),
                nuevo.edad(),
                nuevo.activo(),
                nuevo.fechaIngreso()
        );
        alumnos.add(actualizado);
        return actualizado;
    }

    @PatchMapping("/alumnos/{id}")
    public Alumno patchAlumno(@PathVariable long id, @RequestBody Map<String, Object> updates) {
        Alumno existente = getAlumnoById(id);
        alumnos.remove(existente);

        String name = updates.getOrDefault("name", existente.name()).toString();
        String apellidos = updates.getOrDefault("apellidos", existente.apellidos()).toString();
        int edad = updates.containsKey("edad") ? (int) updates.get("edad") : existente.edad();
        boolean activo = updates.containsKey("activo") ? (boolean) updates.get("activo") : existente.activo();
        LocalDate fechaIngreso = updates.containsKey("fechaIngreso")
                ? LocalDate.parse(updates.get("fechaIngreso").toString())
                : existente.fechaIngreso();

        Alumno actualizado = new Alumno(id, name, apellidos, edad, activo, fechaIngreso);
        alumnos.add(actualizado);
        return actualizado;
    }

    @DeleteMapping("/alumnos/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAlumno(@PathVariable long id) {
        alumnos.removeIf(a -> a.id() == id);
    }


    @GetMapping("/alumnos/filtrar")
    public List<Alumno> filtrarAlumnos(
            @RequestParam(required = false, defaultValue = "") String nombre,
            @RequestParam(required = false, defaultValue = "0") int edad,
            @RequestParam(required = false, defaultValue = "true") boolean activo,
            @RequestParam(required = false) String fechaIngreso
    ) {
        return alumnos.stream()
                .filter(a -> nombre.isEmpty() || a.name().equalsIgnoreCase(nombre))
                .filter(a -> edad == 0 || a.edad() == edad)
                .filter(a -> a.activo() == activo)
                .filter(a -> fechaIngreso == null || a.fechaIngreso().toString().equals(fechaIngreso))
                .toList();
    }

    @ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "Alumno no encontrado")
    static class AlumnoNotFoundException extends RuntimeException {}

    record Alumno(long id, String name, String apellidos, int edad, boolean activo, LocalDate fechaIngreso) {}
}
