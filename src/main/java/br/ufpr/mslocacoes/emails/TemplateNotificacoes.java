package br.ufpr.mslocacoes.emails;

import br.ufpr.mslocacoes.model.dto.espaco_esportivo.EspEsportivoBuscaResponse;
import br.ufpr.mslocacoes.model.dto.notificacao.CriacaoNotificacaoRequest;
import br.ufpr.mslocacoes.model.entity.Locacao;

import java.time.format.DateTimeFormatter;

public class TemplateNotificacoes {
    private TemplateNotificacoes(){/*vazio..*/}

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public static CriacaoNotificacaoRequest notificacaoAprovacaoReserva(Long idCliente, Locacao locacao, EspEsportivoBuscaResponse ee) {
        var dataFormatada = locacao.getDataHoraInicioReserva().format(DATE_FORMATTER);
        var horaInicioFormatada = locacao.getDataHoraInicioReserva().format(TIME_FORMATTER);
        var horaFimFormatada = locacao.getDataHoraFimReserva().format(TIME_FORMATTER);

        var titulo = "SUA RESERVA FOI APROVADA!";

        var conteudo = """
                Que notícia boa, sua reserva para o espaço "%s" no dia %s - das %s às %s foi aprovada :)
                """
                .formatted(ee.getNome(), dataFormatada, horaInicioFormatada, horaFimFormatada);

        return CriacaoNotificacaoRequest.builder()
                .idCliente(idCliente)
                .titulo(titulo)
                .conteudo(conteudo)
                .build();
    }

    public static CriacaoNotificacaoRequest notificacaoNegacaoReserva(Long idCliente, Locacao locacao, EspEsportivoBuscaResponse ee) {
        var dataFormatada = locacao.getDataHoraInicioReserva().format(DATE_FORMATTER);
        var horaInicioFormatada = locacao.getDataHoraInicioReserva().format(TIME_FORMATTER);
        var horaFimFormatada = locacao.getDataHoraFimReserva().format(TIME_FORMATTER);

        var titulo = "SUA RESERVA FOI NEGADA";

        var conteudo = """
                Infelizmente sua reserva para o espaço "%s" no dia %s - das %s às %s foi negada pelo seguinte motivo: %s
                """
                .formatted(ee.getNome(), dataFormatada, horaInicioFormatada, horaFimFormatada, locacao.getMotivoCancelamento());

        return CriacaoNotificacaoRequest.builder()
                .idCliente(idCliente)
                .titulo(titulo)
                .conteudo(conteudo)
                .build();
    }

    public static CriacaoNotificacaoRequest notificacaoEncerramentoReserva(Long idCliente, Locacao locacao, EspEsportivoBuscaResponse ee) {
        var dataFormatada = locacao.getDataHoraInicioReserva().format(DATE_FORMATTER);
        var horaInicioFormatada = locacao.getDataHoraInicioReserva().format(TIME_FORMATTER);
        var horaFimFormatada = locacao.getDataHoraFimReserva().format(TIME_FORMATTER);

        var titulo = "SUA RESERVA FOI ENCERRADA";

        var conteudo = """
                Sua reserva para o espaço "%s" no dia %s - das %s às %s foi encerrada pelo administrador. %s
                """
                .formatted(ee.getNome(), dataFormatada, horaInicioFormatada, horaFimFormatada,
                        locacao.getMotivoEncerramento() != null ? " Justificativa: " + locacao.getMotivoEncerramento() : "");

        return CriacaoNotificacaoRequest.builder()
                .idCliente(idCliente)
                .titulo(titulo)
                .conteudo(conteudo)
                .build();
    }
}