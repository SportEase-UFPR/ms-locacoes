package br.ufpr.mslocacoes.model.dto.cliente;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.LinkedHashMap;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NomeClienteResponse {
    private Long idCliente;
    private String nomeCliente;

    public NomeClienteResponse(Object obj) {
        if (obj instanceof LinkedHashMap<?, ?> hm) {
            this.idCliente = Long.valueOf((Integer) hm.get("idCliente"));
            this.nomeCliente = (String) hm.get("nomeCliente");
        }
    }
}
