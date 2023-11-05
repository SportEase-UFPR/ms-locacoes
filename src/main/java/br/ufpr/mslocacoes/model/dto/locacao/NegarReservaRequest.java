package br.ufpr.mslocacoes.model.dto.locacao;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NegarReservaRequest {
    @NotBlank(message = "O campo justificativa é obrigatório")
    private String justificativa;
}
