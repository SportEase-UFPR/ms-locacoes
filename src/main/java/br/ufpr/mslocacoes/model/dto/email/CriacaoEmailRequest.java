package br.ufpr.mslocacoes.model.dto.email;

import lombok.*;

@Getter
@Setter @Builder
@AllArgsConstructor
@NoArgsConstructor
public class CriacaoEmailRequest {
    private String email;
    private String assunto;
    private String mensagem;
}
