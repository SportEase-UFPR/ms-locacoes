package br.ufpr.mslocacoes.model.dto.locacao;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SolicitacaoLocacaoRequest {
    @NotBlank(message = "O campo motivoSolicitacao é obrigatório")
    private String motivoSolicitacao;

    @NotNull(message = "O campo qtdParticipantes é obrigatório")
    private Integer qtdParticipantes;

    private LocalDateTime dataHoraInicioReserva;

    private LocalDateTime dataHoraFimReserva;

    @NotNull(message = "O idEspacoEsportivo é obrigatóio")
    private Long idEspacoEsportivo;
}
