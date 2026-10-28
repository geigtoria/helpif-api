package br.edu.ifrn.helpif_api.controllers;

import br.edu.ifrn.helpif_api.dtos.SetorRequestDTO;
import br.edu.ifrn.helpif_api.dtos.SetorResponseDTO;
import br.edu.ifrn.helpif_api.services.SetorService;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/setores")
public class SetorController {

    private final SetorService service;

    public SetorController(SetorService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<SetorResponseDTO> criar(
            @Valid @RequestBody SetorRequestDTO dto) {

        SetorResponseDTO setor = service.criar(dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(setor);
    }

    @GetMapping
    public ResponseEntity<Page<SetorResponseDTO>> listar(
            @RequestParam(required = false) String nome,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(
                page,
                Math.min(size, 50),
                Sort.by("nome").ascending()
        );

        return ResponseEntity.ok(service.listar(nome, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SetorResponseDTO> buscarPorId(
            @PathVariable Long id) {

        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SetorResponseDTO> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody SetorRequestDTO dto) {

        return ResponseEntity.ok(service.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {

        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
