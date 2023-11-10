package br.ufpr.mslocacoes.model.dto.locacao;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EncerrarReservaRequest {
    private String justificativa;
}
