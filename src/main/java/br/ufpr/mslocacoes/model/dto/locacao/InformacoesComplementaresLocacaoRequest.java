package br.ufpr.mslocacoes.model.dto.locacao;

import br.ufpr.mslocacoes.model.entity.Locacao;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Builder
public class InformacoesComplementaresLocacaoRequest {
    private Long idLocacao;
    private Long idEspacoEsportivo;
    private Long idCliente;

    public InformacoesComplementaresLocacaoRequest(Locacao reserva) {
        this.idLocacao = reserva.getId();
        this.idEspacoEsportivo = reserva.getIdEspacoEsportivo();
        this.idCliente = reserva.getIdCliente();
    }
}
