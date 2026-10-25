package br.edu.ifrn.helpif_api.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "interacoes")
@Getter
@Setter
public class Interacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String texto;

    @Column(nullable = false)
    private LocalDateTime dataHora;

    @ManyToOne(optional = false)
    @JoinColumn(name = "chamado_id", nullable = false)
    private Chamado chamado;

    @ManyToOne(optional = false)
    @JoinColumn(name = "autor_id", nullable = false)
    private Usuario autor;
}
