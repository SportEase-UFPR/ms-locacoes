package br.ufpr.mslocacoes.model.dto.espaco_esportivo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.LinkedHashMap;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class EsporteResponse {
    private Long id;
    private String nome;

    public EsporteResponse(Object esporte) {
        if (esporte instanceof LinkedHashMap<?, ?> esp) {
            this.id = Long.valueOf((Integer) esp.get("id"));
            this.nome = (String) esp.get("nome");
        }
    }
}
