package br.edu.ifrn.helpif_api.controllers;

import br.edu.ifrn.helpif_api.dtos.*;
import br.edu.ifrn.helpif_api.entities.Prioridade;
import br.edu.ifrn.helpif_api.entities.StatusChamado;
import br.edu.ifrn.helpif_api.services.ChamadoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/chamados")
public class ChamadoController {
    private final ChamadoService service;

    public ChamadoController(ChamadoService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ChamadoResponseDTO> abrir(@Valid @RequestBody ChamadoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.abrir(dto));
    }

    @GetMapping
    public ResponseEntity<Page<ChamadoResponseDTO>> listar(
            @RequestParam(required = false) StatusChamado status,
            @RequestParam(required = false) Prioridade prioridade,
            @RequestParam(required = false) Long setorId,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) Long solicitanteId,
            @RequestParam(required = false) Long tecnicoId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataFim,
            @PageableDefault(size = 10, sort = "dataAbertura", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(service.listar(status, prioridade, setorId, categoriaId,
                solicitanteId, tecnicoId, dataInicio, dataFim, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChamadoDetalheDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @PatchMapping("/{id}/assumir")
    public ResponseEntity<ChamadoResponseDTO> assumir(@PathVariable Long id,
            @Valid @RequestBody AssumirChamadoDTO dto) {
        return ResponseEntity.ok(service.assumir(id, dto));
    }

    @PatchMapping("/{id}/resolver")
    public ResponseEntity<ChamadoResponseDTO> resolver(@PathVariable Long id,
            @Valid @RequestBody ResolverChamadoDTO dto) {
        return ResponseEntity.ok(service.resolver(id, dto));
    }

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<ChamadoResponseDTO> cancelar(@PathVariable Long id,
            @Valid @RequestBody CancelarChamadoDTO dto) {
        return ResponseEntity.ok(service.cancelar(id, dto));
    }

    @PostMapping("/{id}/interacoes")
    public ResponseEntity<InteracaoResponseDTO> registrarInteracao(@PathVariable Long id,
            @Valid @RequestBody InteracaoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.registrarInteracao(id, dto));
    }
}
