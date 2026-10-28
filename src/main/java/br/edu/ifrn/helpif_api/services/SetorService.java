package br.edu.ifrn.helpif_api.services;

import br.edu.ifrn.helpif_api.dtos.SetorRequestDTO;
import br.edu.ifrn.helpif_api.dtos.SetorResponseDTO;
import br.edu.ifrn.helpif_api.entities.Setor;
import br.edu.ifrn.helpif_api.repositories.SetorRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class SetorService {

    private final SetorRepository repository;

    public SetorService(SetorRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public SetorResponseDTO criar(SetorRequestDTO dto) {
        if (repository.existsBySiglaIgnoreCase(dto.sigla())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Sigla já cadastrada");
        }

        Setor setor = new Setor();
        setor.setNome(dto.nome());
        setor.setSigla(dto.sigla());

        return new SetorResponseDTO(repository.save(setor));
    }

    @Transactional(readOnly = true)
    public Page<SetorResponseDTO> listar(String nome, Pageable pageable) {
        Page<Setor> setores;

        if (nome == null || nome.isBlank()) {
            setores = repository.findAll(pageable);
        } else {
            setores = repository.findByNomeContainingIgnoreCase(
                    nome, pageable);
        }

        return setores.map(SetorResponseDTO::new);
    }

    @Transactional(readOnly = true)
    public SetorResponseDTO buscarPorId(Long id) {
        Setor setor = encontrarSetor(id);
        return new SetorResponseDTO(setor);
    }

    @Transactional
    public SetorResponseDTO atualizar(Long id, SetorRequestDTO dto) {
        Setor setor = encontrarSetor(id);

        if (repository.existsBySiglaIgnoreCaseAndIdNot(
                dto.sigla(), id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Sigla já cadastrada");
        }

        setor.setNome(dto.nome());
        setor.setSigla(dto.sigla());

        return new SetorResponseDTO(repository.save(setor));
    }

    @Transactional
    public void excluir(Long id) {
        Setor setor = encontrarSetor(id);
        repository.delete(setor);
        repository.flush();
    }

    private Setor encontrarSetor(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Setor não encontrado"));
    }
}
