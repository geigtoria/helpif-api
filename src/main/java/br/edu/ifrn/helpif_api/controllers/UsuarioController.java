package br.edu.ifrn.helpif_api.controllers;

import br.edu.ifrn.helpif_api.dtos.ChamadoResponseDTO;
import br.edu.ifrn.helpif_api.entities.StatusChamado;
import br.edu.ifrn.helpif_api.services.ChamadoService;
import org.springframework.data.domain.Sort;
import br.edu.ifrn.helpif_api.dtos.UsuarioRequestDTO;
import br.edu.ifrn.helpif_api.dtos.UsuarioResponseDTO;
import br.edu.ifrn.helpif_api.entities.TipoUsuario;
import br.edu.ifrn.helpif_api.services.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {
    private final UsuarioService service;
    private final ChamadoService chamadoService;

    public UsuarioController(UsuarioService service,
            ChamadoService chamadoService) {
        this.service = service;
        this.chamadoService = chamadoService;
    }

    @PostMapping
    public ResponseEntity<UsuarioResponseDTO> criar(@Valid @RequestBody UsuarioRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(dto));
    }

    @GetMapping
    public ResponseEntity<Page<UsuarioResponseDTO>> listar(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) TipoUsuario tipo,
            @RequestParam(required = false) Long setorId,
            @PageableDefault(size = 10, sort = "nome") Pageable pageable) {
        return ResponseEntity.ok(service.listar(nome, tipo, setorId, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @GetMapping("/{id}/chamados")
    public ResponseEntity<Page<ChamadoResponseDTO>> listarChamados(
            @PathVariable Long id,
            @RequestParam(required = false) StatusChamado status,
            @PageableDefault(size = 10, sort = "dataAbertura",
                    direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(chamadoService.listarPorUsuario(id, status, pageable));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> atualizar(
            @PathVariable Long id, @Valid @RequestBody UsuarioRequestDTO dto) {
        return ResponseEntity.ok(service.atualizar(id, dto));
    }
}
