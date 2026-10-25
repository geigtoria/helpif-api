package br.edu.ifrn.helpif_api.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categorias")
@Getter
@Setter
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descricao;

    @ManyToOne(optional = false)
    @JoinColumn(name = "setor_id", nullable = false)
    private Setor setor;

    @OneToMany(mappedBy = "categoria")
    private List<Chamado> chamados = new ArrayList<>();
}
