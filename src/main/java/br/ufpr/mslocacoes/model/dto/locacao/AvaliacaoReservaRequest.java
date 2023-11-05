package br.ufpr.mslocacoes.model.dto.locacao;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AvaliacaoReservaRequest {
    @NotNull(message = "avaliacao é obrigatório")
    @Min(value = 1, message = "A avaliação mínima deve ser 1")
    @Max(value = 5, message = "A avaliação máxima deve ser 5")
    private Integer avaliacao;

    private String comentario;
}
