package br.ufpr.mslocacoes.model.dto.cliente;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.LinkedHashMap;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ClienteBuscaResponse {
    private Long id;
    private String nome;
    private String email;
    private String cpf;
    private String grr;

    public ClienteBuscaResponse(Object obj) {
        if (obj instanceof LinkedHashMap<?, ?> hm) {
            this.id = Long.valueOf((Integer) hm.get("id"));
            this.nome = (String) hm.get("nome");
            this.email = (String) hm.get("email");
            this.cpf = (String) hm.get("cpf");
            this.grr = (String) hm.get("grr");
        }
    }
}
