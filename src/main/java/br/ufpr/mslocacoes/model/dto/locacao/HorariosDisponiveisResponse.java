package br.ufpr.mslocacoes.model.dto.locacao;

import br.ufpr.mslocacoes.model.dto.espaco_esportivo.EspEsportivoBuscaResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Getter
@Builder
public class HorariosDisponiveisResponse {
    List<LocalTime> horariosDisponiveis;
    LocalTime periodoLocacao;

    public HorariosDisponiveisResponse(EspEsportivoBuscaResponse ee, LocalTime periodo) {
        horariosDisponiveis = calcularHorariosDisponiveis(ee);
        periodoLocacao = periodo;
    }

    private List<LocalTime> calcularHorariosDisponiveis(EspEsportivoBuscaResponse ee) {
        List<LocalTime> horarios = new ArrayList<>();
        LocalTime horaAtual = ee.getHoraAbertura();
        LocalTime horaFechamento = ee.getHoraFechamento();
        Duration periodoLocacao = Duration.between(LocalTime.MIN, ee.getPeriodoLocacao());

        while (horaAtual.plus(periodoLocacao).isBefore(horaFechamento)) {
            horarios.add(horaAtual);
            horaAtual = horaAtual.plus(periodoLocacao);
        }

        return horarios;
    }

}
