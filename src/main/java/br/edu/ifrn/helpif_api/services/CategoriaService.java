package br.edu.ifrn.helpif_api.services;

import br.edu.ifrn.helpif_api.dtos.CategoriaRequestDTO;
import br.edu.ifrn.helpif_api.dtos.CategoriaResponseDTO;
import br.edu.ifrn.helpif_api.entities.Categoria;
import br.edu.ifrn.helpif_api.entities.Setor;
import br.edu.ifrn.helpif_api.repositories.CategoriaRepository;
import br.edu.ifrn.helpif_api.repositories.SetorRepository;
import br.edu.ifrn.helpif_api.repositories.ChamadoRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CategoriaService {

    private final CategoriaRepository repository;
    private final SetorRepository setorRepository;
    private final ChamadoRepository chamadoRepository;

    public CategoriaService(
            CategoriaRepository repository,
            SetorRepository setorRepository,
            ChamadoRepository chamadoRepository) {
        this.repository = repository;
        this.setorRepository = setorRepository;
        this.chamadoRepository = chamadoRepository;
    }

    @Transactional
    public CategoriaResponseDTO criar(CategoriaRequestDTO dto) {
        Setor setor = encontrarSetor(dto.setorId());

        Categoria categoria = new Categoria();
        categoria.setNome(dto.nome());
        categoria.setDescricao(dto.descricao());
        categoria.setSetor(setor);

        return new CategoriaResponseDTO(repository.save(categoria));
    }


    @Transactional(readOnly = true)
    public Page<CategoriaResponseDTO> listar(
            String nome, Long setorId, Pageable pageable) {

        boolean temNome = nome != null && !nome.isBlank();
        boolean temSetor = setorId != null;

        Page<Categoria> categorias;

        if (temNome && temSetor) {
            categorias = repository
                    .findByNomeContainingIgnoreCaseAndSetor_Id(
                            nome.trim(), setorId, pageable);
        } else if (temNome) {
            categorias = repository
                    .findByNomeContainingIgnoreCase(
                            nome.trim(), pageable);
        } else if (temSetor) {
            categorias = repository.findBySetor_Id(
                    setorId, pageable);
        } else {
            categorias = repository.findAll(pageable);
        }

        return categorias.map(CategoriaResponseDTO::new);
    }


    @Transactional(readOnly = true)
    public CategoriaResponseDTO buscarPorId(Long id) {
        return new CategoriaResponseDTO(encontrarCategoria(id));
    }

    @Transactional(readOnly = true)
    public Page<CategoriaResponseDTO> listarPorSetor(
            Long setorId, Pageable pageable) {

        encontrarSetor(setorId);

        return listar(null, setorId, pageable);
    }

    @Transactional
    public CategoriaResponseDTO atualizar(
            Long id, CategoriaRequestDTO dto) {

        Categoria categoria = encontrarCategoria(id);
        Setor setor = encontrarSetor(dto.setorId());

        categoria.setNome(dto.nome());
        categoria.setDescricao(dto.descricao());
        categoria.setSetor(setor);

        return new CategoriaResponseDTO(repository.save(categoria));
    }

    @Transactional
    public void excluir(Long id) {
        Categoria categoria = encontrarCategoria(id);

        if (chamadoRepository.existsByCategoria_Id(id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Não é possível excluir uma categoria com chamados vinculados"
            );
        }

        repository.delete(categoria);
        repository.flush();
    }

    private Categoria encontrarCategoria(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Categoria não encontrada"
                ));
    }

    private Setor encontrarSetor(Long id) {
        return setorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Setor não encontrado"
                ));
    }
}
