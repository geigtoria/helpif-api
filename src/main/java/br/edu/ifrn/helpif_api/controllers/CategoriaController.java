package br.edu.ifrn.helpif_api.controllers;

import br.edu.ifrn.helpif_api.dtos.CategoriaRequestDTO;
import br.edu.ifrn.helpif_api.dtos.CategoriaResponseDTO;
import br.edu.ifrn.helpif_api.services.CategoriaService;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/categorias")
public class CategoriaController {

    private final CategoriaService service;

    public CategoriaController(CategoriaService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<CategoriaResponseDTO> criar(
            @Valid @RequestBody CategoriaRequestDTO dto) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.criar(dto));
    }

    @GetMapping
    public ResponseEntity<Page<CategoriaResponseDTO>> listar(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) Long setorId,
            @PageableDefault(
                    size = 10,
                    sort = "nome",
                    direction = Sort.Direction.ASC
            ) Pageable pageable) {

        return ResponseEntity.ok(
                service.listar(nome, setorId, limitarPagina(pageable))
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoriaResponseDTO> buscarPorId(
            @PathVariable Long id) {

        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoriaResponseDTO> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody CategoriaRequestDTO dto) {

        return ResponseEntity.ok(service.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {

        service.excluir(id);
        return ResponseEntity.noContent().build();
    }

    private Pageable limitarPagina(Pageable pageable) {
        return org.springframework.data.domain.PageRequest.of(
                pageable.getPageNumber(),
                Math.min(pageable.getPageSize(), 50),
                pageable.getSort()
        );
    }
}
