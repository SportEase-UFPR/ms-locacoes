package br.ufpr.mslocacoes.model.dto.locacao;

import br.ufpr.mslocacoes.model.entity.Locacao;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@Getter
@Builder
public class ReservaSolicitadaResponse {
    private Long id;
    private String motivoSolicitacao;
    private Integer qtdParticipantes;
    private LocalDateTime dataHoraSolicitacao;
    private LocalDateTime dataHoraInicioReserva;
    private LocalDateTime dataHoraFimReserva;

    private InformacoesComplementaresLocacaoResponse informacoesComplementaresLocacao;

    public ReservaSolicitadaResponse(Locacao reserva) {
        this.id = reserva.getId();
        this.motivoSolicitacao = reserva.getMotivoSolicitacao();
        this.qtdParticipantes = reserva.getQtdParticipantes();
        this.dataHoraSolicitacao = reserva.getDataHoraSolicitacao();
        this.dataHoraInicioReserva = reserva.getDataHoraInicioReserva();
        this.dataHoraFimReserva = reserva.getDataHoraFimReserva();
    }

    public void preencherInformacoesComplementares(List<InformacoesComplementaresLocacaoResponse> lista) {
        lista.forEach(infCompl -> {
            if(infCompl.getIdLocacao().equals(id)) {
                this.informacoesComplementaresLocacao = infCompl;
            }
        });

    }
}
