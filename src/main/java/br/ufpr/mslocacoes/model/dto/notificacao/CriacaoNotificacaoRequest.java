package br.ufpr.mslocacoes.model.dto.notificacao;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CriacaoNotificacaoRequest {
    private Long idCliente;
    private String titulo;
    private String conteudo;

}
