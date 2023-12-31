package br.ufpr.mslocacoes.templates;


import br.ufpr.mslocacoes.model.dto.cliente.ClienteBuscaResponse;
import br.ufpr.mslocacoes.model.dto.email.CriacaoEmailRequest;
import br.ufpr.mslocacoes.model.dto.espaco_esportivo.EspEsportivoBuscaResponse;
import br.ufpr.mslocacoes.model.entity.Locacao;

import java.time.format.DateTimeFormatter;

public class TemplateEmails {
    private TemplateEmails(){/*vazio..*/}

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public static CriacaoEmailRequest emailAprovacaoReserva(ClienteBuscaResponse cliente, Locacao locacao, EspEsportivoBuscaResponse ee) {
        var dataFormatada = locacao.getDataHoraInicioReserva().format(DATE_FORMATTER);
        var horaInicioFormatada = locacao.getDataHoraInicioReserva().format(TIME_FORMATTER);
        var horaFimFormatada = locacao.getDataHoraFimReserva().format(TIME_FORMATTER);

        var assunto = "SportEase - Sua reserva foi aprovada!";
        var mensagem = """
                <html><body>
                    <h2>Olá %s,</h2>
                    <p>A sua reserva para o espaço esportivo "%s" foi aprovada!</p>
                    <p>Detalhes da reserva:</p>
                    <ul>
                      <li>Localidade: %s</li>
                      <li>Quantidade de participantes: %s</li>
                      <li>Data: %s</li>
                      <li>Horário: das %s às %s</li>
                    </ul>
                    <p>Caso não possa comparecer ao espaço esportivo, lembre-se de cancelar a reserva com no mínimo 24 horas antes do horário marcado.</p>
                    <p style="margin: 0;">Atenciosamente,</p>
                    <p style="margin: 0;">A Equipe SportEase.</p>
                </body></html>
                """.formatted(cliente.getNome(), ee.getNome(), ee.getLocalidade(),
                locacao.getQtdParticipantes().toString(), dataFormatada, horaInicioFormatada, horaFimFormatada);

        return CriacaoEmailRequest.builder()
                .assunto(assunto)
                .mensagem(mensagem)
                .email(cliente.getEmail())
                .build();
    }

    public static CriacaoEmailRequest emailNegacaoReserva(ClienteBuscaResponse cliente, Locacao locacao, EspEsportivoBuscaResponse ee) {
        var dataFormatada = locacao.getDataHoraInicioReserva().format(DATE_FORMATTER);
        var horaInicioFormatada = locacao.getDataHoraInicioReserva().format(TIME_FORMATTER);
        var horaFimFormatada = locacao.getDataHoraFimReserva().format(TIME_FORMATTER);

        var assunto = "SportEase - Sua reserva foi negada";
        var mensagem = """
        <html><body>
            <h2>Olá %s,</h2>
            <p>Infelizmente, sua reserva para o espaço esportivo "%s" no dia %s, das %s às %s, foi negada.</p>
            <p>O motivo para a não aprovação da sua reserva é: "%s"</p>
            <p>Em caso de dúvidas, você pode responder a esse email.</p>
            <p style="margin: 0;">Atenciosamente,</p>
            <p style="margin: 0;">A Equipe SportEase.</p>
        </body></html>
        """.formatted(cliente.getNome(), ee.getNome(), dataFormatada, horaInicioFormatada, horaFimFormatada,
                locacao.getMotivoCancelamento());


        return CriacaoEmailRequest.builder()
                .assunto(assunto)
                .mensagem(mensagem)
                .email(cliente.getEmail())
                .build();
    }

    public static CriacaoEmailRequest emailEncerramentoReserva(ClienteBuscaResponse cliente, Locacao locacao, EspEsportivoBuscaResponse ee) {
        var dataFormatada = locacao.getDataHoraInicioReserva().format(DATE_FORMATTER);
        var horaInicioFormatada = locacao.getDataHoraInicioReserva().format(TIME_FORMATTER);
        var horaFimFormatada = locacao.getDataHoraFimReserva().format(TIME_FORMATTER);

        var assunto = "SportEase - Sua reserva foi encerrada";
        var mensagem = """
        <html><body>
            <h2>Olá %s,</h2>
            <p>Sua reserva para o espaço esportivo "%s" no dia %s, das %s às %s, foi encerrada pelo administrador.</p>
            <p>%s</p>
            <p>Em caso de dúvidas, você pode responder a esse email.</p>
            <p style="margin: 0;">Atenciosamente,</p>
            <p style="margin: 0;">A Equipe SportEase.</p>
        </body></html>
        """.formatted(cliente.getNome(), ee.getNome(), dataFormatada, horaInicioFormatada, horaFimFormatada,
                locacao.getMotivoEncerramento() != null ?
                        ("O motivo para o encerramento da sua reserva é: " + locacao.getMotivoEncerramento()) : "");


        return CriacaoEmailRequest.builder()
                .assunto(assunto)
                .mensagem(mensagem)
                .email(cliente.getEmail())
                .build();
    }

    public static CriacaoEmailRequest emailEspacoEsportivoIndisponivel(ClienteBuscaResponse cliente, EspEsportivoBuscaResponse ee) {
        var assunto = "SportEase - Sua reserva foi cancelada";
        var mensagem = """
        <html><body>
            <h2>Olá %s,</h2>
            <p>Suas reservas para o espaço esportivo "%s" foram canceladas pois o espaço esportivo não está mais disponível.</p>
            <p>Em caso de dúvidas, você pode responder a esse email.</p>
            <p style="margin: 0;">Atenciosamente,</p>
            <p style="margin: 0;">A Equipe SportEase.</p>
        </body></html>
        """.formatted(cliente.getNome(), ee.getNome());


        return CriacaoEmailRequest.builder()
                .assunto(assunto)
                .mensagem(mensagem)
                .email(cliente.getEmail())
                .build();
    }
}
