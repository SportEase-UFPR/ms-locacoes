package br.ufpr.mslocacoes.model.dto.locacao;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class HorarioDisponivelRequest {
    @NotNull(message = "O campo data é obrigatório")
    private LocalDate data;
    @NotNull(message = "O campo idEspacoEsportivo é obrigatório")
    private Long idEspacoEsportivo;
}
