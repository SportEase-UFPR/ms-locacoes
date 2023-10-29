package br.ufpr.mslocacoes.model.dto.locacao;

import br.ufpr.mslocacoes.model.entity.Locacao;
import br.ufpr.mslocacoes.model.enums.StatusLocacao;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
@Builder
public class BuscaReservaResponse {
    private Long id;
    private String motivoSolicitacao;
    private Integer qtdParticipantes;
    private LocalDateTime dataHoraSolicitacao;
    private LocalDateTime dataHoraInicioReserva;
    private LocalDateTime dataHoraFimReserva;
    private StatusLocacao status;
    private Long idEspacoEsportivo;
    private Long idAdministrador; //preenchido quando locação é aprovada ou negada
    private Long idCliente;
    private String motivoCancelamento;

    public BuscaReservaResponse(Locacao reserva) {
        this.id = reserva.getId();
        this.motivoSolicitacao = reserva.getMotivoSolicitacao();
        this.qtdParticipantes = reserva.getQtdParticipantes();
        this.dataHoraSolicitacao = reserva.getDataHoraSolicitacao();
        this.dataHoraInicioReserva = reserva.getDataHoraInicioReserva();
        this.dataHoraFimReserva = reserva.getDataHoraFimReserva();
        this.status = reserva.getStatus();
        this.idEspacoEsportivo = reserva.getIdEspacoEsportivo();
        this.idAdministrador = reserva.getIdAdministrador();
        this.idCliente = reserva.getIdCliente();
        this.motivoCancelamento = reserva.getMotivoCancelamento();
    }
}
