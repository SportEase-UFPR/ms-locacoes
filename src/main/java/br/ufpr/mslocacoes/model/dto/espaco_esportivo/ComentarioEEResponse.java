package br.ufpr.mslocacoes.model.dto.espaco_esportivo;

import br.ufpr.mslocacoes.model.dto.cliente.NomeClienteResponse;
import br.ufpr.mslocacoes.model.entity.Locacao;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ComentarioEEResponse {
    Long idEspacoEsportivo;
    Long idCliente;
    String nomeCliente;
    LocalDateTime dataHoraComentario;
    String comentario;
    Integer avaliacao;

    public ComentarioEEResponse(Locacao reserva) {
        this.idEspacoEsportivo = reserva.getIdEspacoEsportivo();
        this.idCliente = reserva.getIdCliente();
        this.dataHoraComentario = reserva.getDataHoraComentario();
        this.comentario = reserva.getComentarioCliente();
        this.avaliacao = reserva.getAvaliacao();
    }

    public void preencherNomeCliente(List<NomeClienteResponse> lista) {
        lista.forEach(item -> {
            if(item.getIdCliente().equals(idCliente)) {
                this.nomeCliente = item.getNomeCliente();
            }
        });
    }
}
