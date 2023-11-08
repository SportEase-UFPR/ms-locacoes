package br.ufpr.mslocacoes.model.dto.locacao;

import br.ufpr.mslocacoes.model.entity.Locacao;
import br.ufpr.mslocacoes.model.enums.StatusLocacao;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@Getter
@Builder
public class ReservaDetalhadaResponse {
    private Long id;
    private String motivoSolicitacao;
    private Integer qtdParticipantes;
    private LocalDateTime dataHoraSolicitacao;
    private LocalDateTime dataHoraInicioReserva;
    private LocalDateTime dataHoraFimReserva;
    private StatusLocacao status;
    private String motivoCancelamento;
    private String comentario;
    private Integer avaliacao;


    private InformacoesComplementaresLocacaoResponse informacoesComplementaresLocacao;

    public ReservaDetalhadaResponse(Locacao reserva) {
        this.id = reserva.getId();
        this.motivoSolicitacao = reserva.getMotivoSolicitacao();
        this.qtdParticipantes = reserva.getQtdParticipantes();
        this.dataHoraSolicitacao = reserva.getDataHoraSolicitacao();
        this.dataHoraInicioReserva = reserva.getDataHoraInicioReserva();
        this.dataHoraFimReserva = reserva.getDataHoraFimReserva();
        this.status = reserva.getStatus();
        this.motivoCancelamento = reserva.getMotivoCancelamento();
        this.comentario = reserva.getComentarioCliente();
        this.avaliacao = reserva.getAvaliacao();
    }

    public void preencherInformacoesComplementares(List<InformacoesComplementaresLocacaoResponse> lista) {
        lista.forEach(infCompl -> {
            if(infCompl.getIdLocacao().equals(id)) {
                this.informacoesComplementaresLocacao = infCompl;
            }
        });

    }
}
