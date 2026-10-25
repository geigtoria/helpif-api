package br.edu.ifrn.helpif_api.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String matricula;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoUsuario tipo;

    @ManyToOne
    @JoinColumn(name = "setor_id")
    private Setor setor;

    @OneToMany(mappedBy = "solicitante")
    private List<Chamado> chamadosAbertos = new ArrayList<>();

    @OneToMany(mappedBy = "tecnico")
    private List<Chamado> chamadosAtribuidos = new ArrayList<>();
}
