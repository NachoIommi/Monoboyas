package com.monoboyas.api;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.monoboyas.persistencia.MonoboyaDAO;

@RestController
@RequestMapping("/v1/monoboyas")
public class MonoboyaController {

    private final MonoboyaDAO monoboyaDAO;

    public MonoboyaController(MonoboyaDAO monoboyaDAO) {
        this.monoboyaDAO = monoboyaDAO;
    }

    @GetMapping
    public List<MonoboyaDAO.MonoboyaInfo> listar() {
        return monoboyaDAO.listarTodasInfo();
    }

    @GetMapping("/{id}")
    public MonoboyaDAO.MonoboyaInfo obtener(@PathVariable int id) {
        return monoboyaDAO.buscarInfoPorId(id);
    }
}