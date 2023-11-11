package br.ufpr.mslocacoes.model.dto.locacao;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Builder
public class EstatisticasReservaResponse {
    private Long idCliente;
    private Integer totalReservas;
    private Integer totalReservasSolicitadas;
    private Integer totalReservasCanceladas;
    private Integer totalReservasAprovadas;
    private Integer totalReservasNegadas;
    private Integer totalReservasFinalizadas;
    private Integer totalReservasEncerradas;

    public EstatisticasReservaResponse(EstatisticasReservaResponse response) {
        if (response.getIdCliente() != null) {
            this.idCliente = response.getIdCliente();

            this.totalReservas = response.getTotalReservas() != null ? response.getTotalReservas() : 0;
            this.totalReservasSolicitadas = response.getTotalReservasSolicitadas() != null ? response.getTotalReservasSolicitadas() : 0;
            this.totalReservasCanceladas = response.getTotalReservasCanceladas() != null ? response.getTotalReservasCanceladas() : 0;
            this.totalReservasAprovadas = response.getTotalReservasAprovadas() != null ? response.getTotalReservasAprovadas() : 0;
            this.totalReservasNegadas = response.getTotalReservasNegadas() != null ? response.getTotalReservasNegadas() : 0;
            this.totalReservasFinalizadas = response.getTotalReservasFinalizadas() != null ? response.getTotalReservasFinalizadas() : 0;
            this.totalReservasEncerradas = response.getTotalReservasEncerradas() != null ? response.getTotalReservasEncerradas() : 0;
        }
    }
}
