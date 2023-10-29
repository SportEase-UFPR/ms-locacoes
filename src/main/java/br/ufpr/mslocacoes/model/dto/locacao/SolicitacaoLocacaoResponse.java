package br.ufpr.mslocacoes.model.dto.locacao;

import br.ufpr.mslocacoes.model.entity.Locacao;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class SolicitacaoLocacaoResponse {
    private Long id;
    private String motivoSolicitacao;
    private Integer qtdParticipantes;
    private LocalDateTime dataHoraInicioReserva;
    private LocalDateTime dataHoraFimReserva;
    private Long idEspacoEsportivo;

    public SolicitacaoLocacaoResponse(Locacao locacao) {
        this.id = locacao.getId();
        this.motivoSolicitacao = locacao.getMotivoSolicitacao();
        this.qtdParticipantes = locacao.getQtdParticipantes();
        this.dataHoraInicioReserva = locacao.getDataHoraInicioReserva();
        this.dataHoraFimReserva = locacao.getDataHoraFimReserva();
        this.idEspacoEsportivo = locacao.getIdEspacoEsportivo();
    }
}
