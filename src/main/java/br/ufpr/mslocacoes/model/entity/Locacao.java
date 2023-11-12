package br.ufpr.mslocacoes.model.entity;

import br.ufpr.mslocacoes.model.dto.locacao.SolicitacaoLocacaoRequest;
import br.ufpr.mslocacoes.model.enums.StatusLocacao;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

import static br.ufpr.mslocacoes.utils.HorarioBrasil.buscarHoraAtual;


@Entity(name = "tb_locacoes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Locacao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String motivoSolicitacao;

    @Column(nullable = false)
    private Integer qtdParticipantes;

    @Column(nullable = false)
    private LocalDateTime dataHoraSolicitacao;

    @Column(nullable = false)
    private LocalDateTime dataHoraInicioReserva;

    @Column(nullable = false)
    private LocalDateTime dataHoraFimReserva;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusLocacao status;

    @Column(nullable = false)
    private Long idEspacoEsportivo;

    @Column
    private Long idAdministrador; //preenchido quando locação é aprovada ou negada

    @Column(nullable = false)
    private Long idCliente;

    private String motivoCancelamento;

    private String motivoEncerramento;

    private Integer avaliacao;

    private String comentarioCliente;

    private LocalDateTime dataHoraComentario;

    public Locacao(SolicitacaoLocacaoRequest solicitacao, Long idCliente) {
        this.motivoSolicitacao = solicitacao.getMotivoSolicitacao();
        this.qtdParticipantes = solicitacao.getQtdParticipantes();
        this.dataHoraSolicitacao = buscarHoraAtual();
        this.dataHoraInicioReserva = solicitacao.getDataHoraInicioReserva();
        this.dataHoraFimReserva = solicitacao.getDataHoraFimReserva();
        this.status = StatusLocacao.SOLICITADA;
        this.idEspacoEsportivo = solicitacao.getIdEspacoEsportivo();
        this.idCliente = idCliente;
    }
}
