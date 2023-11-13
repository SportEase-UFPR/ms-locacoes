package br.ufpr.mslocacoes.model.dto.espaco_esportivo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.LinkedHashMap;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class EspacoEsportivoSimplificado {
    private Long id;
    private String nome;
    private String localidade;

    public EspacoEsportivoSimplificado(Object obj) {
        if (obj instanceof LinkedHashMap<?, ?> hm) {
            this.id = Long.valueOf((Integer) hm.get("id"));
            this.nome = (String) hm.get("nome");
            this.localidade = (String) hm.get("localidade");
        }
    }
}
