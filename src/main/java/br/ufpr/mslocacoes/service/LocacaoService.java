package br.ufpr.mslocacoes.service;

import br.ufpr.mslocacoes.client.MsCadastrosClient;
import br.ufpr.mslocacoes.client.MsComunicacoesClient;
import br.ufpr.mslocacoes.emails.TemplateEmails;
import br.ufpr.mslocacoes.emails.TemplateNotificacoes;
import br.ufpr.mslocacoes.exceptions.BussinessException;
import br.ufpr.mslocacoes.exceptions.EntityNotFoundException;
import br.ufpr.mslocacoes.model.dto.espaco_esportivo.AtualizarMediaAvaliacaoEERequest;
import br.ufpr.mslocacoes.model.dto.espaco_esportivo.ComentarioEEResponse;
import br.ufpr.mslocacoes.model.dto.espaco_esportivo.EspEsportivoBuscaResponse;
import br.ufpr.mslocacoes.model.dto.locacao.*;
import br.ufpr.mslocacoes.model.entity.Locacao;
import br.ufpr.mslocacoes.model.enums.StatusLocacao;
import br.ufpr.mslocacoes.repository.LocacaoRepository;
import br.ufpr.mslocacoes.security.TokenService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.time.*;
import java.util.ArrayList;
import java.util.List;

import static br.ufpr.mslocacoes.utils.HorarioBrasil.buscarHoraAtual;


@Service
public class LocacaoService {

    private final TokenService tokenService;
    private final LocacaoRepository locacaoRepository;
    private final MsCadastrosClient msCadastrosClient;
    private final MsComunicacoesClient msComunicacoesClient;


    public LocacaoService(TokenService tokenService, LocacaoRepository locacaoRepository, MsCadastrosClient msCadastrosClient, MsComunicacoesClient msComunicacoesClient) {
        this.tokenService = tokenService;
        this.locacaoRepository = locacaoRepository;
        this.msCadastrosClient = msCadastrosClient;
        this.msComunicacoesClient = msComunicacoesClient;
    }

    public SolicitacaoLocacaoResponse solicitarLocacao(SolicitacaoLocacaoRequest request, String token) {
        var dataHoraInicioReserva = request.getDataHoraInicioReserva();
        var dataHoraFimReserva = request.getDataHoraFimReserva();
        //recuperar id do cliente do token
        var idCliente = Long.parseLong(tokenService.getIssuer(token, "idPessoa"));

        //Recuperar espaço esportivo
        EspEsportivoBuscaResponse ee = null;
        try {
            ee = msCadastrosClient.buscarEspacoEsportivoPorId(request.getIdEspacoEsportivo());
        } catch (HttpClientErrorException e) {
            throw new EntityNotFoundException("Espaço esportivo não cadastrado");
        }

        //se espaço esportivo não está disponível, lançar exceção
        if(Boolean.FALSE.equals(ee.getDisponivel())) {
            throw new BussinessException("Espaço esportivo não está disponível");
        }

        //se o dia da semana não for condizente com os dias possíveis para locação, lançar exceção
        var diaSemana = request.getDataHoraInicioReserva().getDayOfWeek().getValue();
        diaSemana = diaSemana == 7 ? 0 : diaSemana;

        if(!ee.getDiasFuncionamento().contains(diaSemana)) {
            throw new BussinessException("O espaço esportivo não permite reservas nesse dia da semana");
        }


        //verificar se dataHoraInicioReserva > horário atual
        if(dataHoraInicioReserva.isBefore(buscarHoraAtual())) {
            throw new BussinessException("A data e hora iniciais da reserva devem ser futuras à data e hora atual");
        }

        //verificar se dataHoraFimReserva > dataHoraInicioReserva
        if(dataHoraInicioReserva.isAfter(dataHoraFimReserva)) {
            throw new BussinessException("A data e hora finais da reserva deve ser futuras à data e hora incial da reserva");
        }

        //verificar se dataHoraInicioReserva e dataHoraFimReserva pertencem ao mesmo dia
        if(!dataHoraInicioReserva.toLocalDate().isEqual(dataHoraFimReserva.toLocalDate())) {
            throw new BussinessException("O horário de início e o de fim de uma reserva devem pertencer ao mesmo dia");
        }

        //verificar se dataHoraInicioReserva >= horarioAbertura
        if(dataHoraInicioReserva.toLocalTime().isBefore(ee.getHoraAbertura())) {
            throw new BussinessException("A date a a hora de início da reserva não estão dentro do período de funcionamento do espaço esportivo");
        }

        //verificar se dataHoraFimReserva <= horarioAbertura
        if(dataHoraFimReserva.toLocalTime().isAfter(ee.getHoraFechamento())) {
            throw new BussinessException("A data e a hora de fim da reserva não estão dentro do período de funcionamento do espaço esportivo");
        }

        //verificar se há conflito de horário para o espaço esportivo escolhido
        if(locacaoRepository.existeConflitoDeHorarioEspacoEsportivo(request.getDataHoraInicioReserva(), request.getIdEspacoEsportivo()) == 1
        ) {
            throw new BussinessException("Já existe uma reserva para essa data e horário no espaço esportivo informado");
        }

        //verificar se o cliente já não solicitou uma reserva em algum espaço esportivo no mesmo dia e horário informado.
        if(locacaoRepository.existeConflitoDeHorarioCliente(request.getDataHoraInicioReserva(), idCliente) == 1
        ) {
            throw new BussinessException("Você já solicitou uma reserva para esse data e horário informado");
        }

        //verificar se o tempo de locação não excede o máximo permitido
        var duracaoLocacao = Duration.between(dataHoraInicioReserva, dataHoraFimReserva);
        var duracaoTotal = duracaoLocacao;

        var possiveisLocacoesExistentes = locacaoRepository.buscarLocacaoPorDiaEIdClienteEEspacoEsportivo(idCliente, request.getDataHoraInicioReserva().toLocalDate(), request.getIdEspacoEsportivo());
        for (var locacao : possiveisLocacoesExistentes) {
            var horaInicio = locacao.getDataHoraInicioReserva();
            var horaFim = locacao.getDataHoraFimReserva();
            duracaoLocacao = Duration.between(horaInicio, horaFim);
            duracaoTotal = duracaoTotal.plus(duracaoLocacao);
        }

        var duracaoCiclo = Duration.between(LocalTime.MIN, ee.getPeriodoLocacao());
        if(duracaoTotal.toMillis() > ee.getMaxLocacaoDia() * duracaoCiclo.toMillis()) {
            throw new BussinessException("Você já atingiu o limite de reservas do dia para o espaço esportivo escolhido");
        }

        //Criar locação
        Locacao novaLocacao = new Locacao(request, idCliente);
        locacaoRepository.save(novaLocacao);

        return new SolicitacaoLocacaoResponse(novaLocacao);
    }

    public HorariosDisponiveisResponse verificarHorariosDisponiveisParaLocacao(HorarioDisponivelRequest request, String token) {
        var idCliente = Long.parseLong(tokenService.getIssuer(token, "idPessoa"));

        //buscar locações solicitadas no dia informado
        var locacoes = locacaoRepository.buscarLocacoesPorDiaEIdEspacoEsportivo(request.getData(), request.getIdEspacoEsportivo());

        //Recuperar espaço esportivo
        EspEsportivoBuscaResponse ee = null;
        try {
            ee = msCadastrosClient.buscarEspacoEsportivoPorId(request.getIdEspacoEsportivo());
        } catch (HttpClientErrorException e) {
            throw new EntityNotFoundException("Espaço esportivo não cadastrado");
        }

        //Verificar se é possível reservar espaço esportivo para o dia informado
        var diaSemana = request.getData().getDayOfWeek().getValue();
        diaSemana = diaSemana == 7 ? 0 : diaSemana;

        if(!ee.getDiasFuncionamento().contains(diaSemana)) {
            throw new BussinessException("O espaço esportivo não permite reservas nesse dia da semana");
        }

        //verificar se o cliente não chegou ao limite de locações para o espaço esportivo nesse dia informado.
        var listaLocacoesDesseCliente = locacoes.stream().filter(locacao -> locacao.getIdCliente() == idCliente).toList();

        var duracaoTotal = Duration.ZERO;
        for (var locacao : listaLocacoesDesseCliente) {
            var dataHoraInicio = locacao.getDataHoraInicioReserva();
            var dataHoraFim = locacao.getDataHoraFimReserva();
            var duracaoLocacao = Duration.between(dataHoraInicio, dataHoraFim);
            duracaoTotal = duracaoTotal.plus(duracaoLocacao);
        }
        var tempoPorLocacao = Duration.between(LocalTime.MIN, ee.getPeriodoLocacao());
        if(duracaoTotal.compareTo(tempoPorLocacao.multipliedBy(ee.getMaxLocacaoDia())) >= 0) {
            throw new BussinessException("Você já atingiu o limite de reservas do espaço esportivo para o dia escolhido");
        }

        //verificar horários válidos para o espaço esportivo informado
        var response = new HorariosDisponiveisResponse(ee, ee.getPeriodoLocacao());


        // Remover as horas que já estão ocupadas
        locacoes.forEach(locacao -> {
            var horaInicio = locacao.getDataHoraInicioReserva().toLocalTime();
            var horaFim = locacao.getDataHoraFimReserva().toLocalTime();

            response.getHorariosDisponiveis().removeIf(hora ->
                    (hora.equals(horaInicio) || hora.isAfter(horaInicio)) &&
                    hora.isBefore(horaFim));
        });

        return response;
    }

    public List<BuscaReservaResponse> listarReservasEmAndamento(String token) {
        Long idCliente = Long.parseLong(tokenService.getIssuer(token, "idPessoa"));
        List<Locacao> listaReservas = locacaoRepository.listarReservasEmAndamento(idCliente);
        return processarReservas(listaReservas);
    }

    public List<BuscaReservaResponse> listarHistoricoReservas(String token) {
        Long idCliente = Long.parseLong(tokenService.getIssuer(token, "idPessoa"));
        List<Locacao> listaReservas = locacaoRepository.findByIdCliente(idCliente);
        return processarReservas(listaReservas);
    }

    public List<BuscaReservaResponse> processarReservas(List<Locacao> listaReservas) {
        List<BuscaReservaResponse> response = new ArrayList<>();
        listaReservas.forEach(reserva -> response.add(new BuscaReservaResponse(reserva)));

        // buscar espaço esportivo
        var listaIdsEE = listaReservas.stream()
                .map(Locacao::getIdEspacoEsportivo)
                .distinct()
                .toList();
        var listaEE = msCadastrosClient.buscarEspacoesEsportivosSimplificado(listaIdsEE);
        response.forEach(reserva -> reserva.preencherEE(listaEE));

        return response;
    }

    public Void cancelarReserva(String token, Long idReserva) {
        //recuperar id do cliente do token
        Long idCliente = Long.parseLong(tokenService.getIssuer(token, "idPessoa"));

        //recuperar reserva
        Locacao locacao = locacaoRepository.findByIdAndIdCliente(idReserva, idCliente)
                .orElseThrow(() -> new EntityNotFoundException("Locação não encontrada"));

        //só é possível cancelar reserva se ela é solicitada ou aprovada
        if(!locacao.getStatus().equals(StatusLocacao.SOLICITADA) &&  !locacao.getStatus().equals(StatusLocacao.APROVADA)) {
            throw new BussinessException("Status da locação não permite cancelamento");
        }

        if(buscarHoraAtual().plusMinutes(15).isAfter(locacao.getDataHoraInicioReserva())) {
            throw new BussinessException("Não é mais possível cancelar a reserva");
        }

        locacao.setStatus(StatusLocacao.CANCELADA);
        locacaoRepository.save(locacao);

        return null;
    }

    public Void confirmarUsoReserva(String token,  Long idReserva) {
        //recuperar id do cliente do token
        Long idCliente = Long.parseLong(tokenService.getIssuer(token, "idPessoa"));

        //recuperar reserva
        Locacao locacao = locacaoRepository.findByIdAndIdCliente(idReserva, idCliente)
                .orElseThrow(() -> new EntityNotFoundException("Locação não encontrada"));

        //só é possível confirmar uso da reserva se ela é aprovada
        if(!locacao.getStatus().equals(StatusLocacao.APROVADA)) {
            throw new BussinessException("Status da locação não permite confirmar uso");
        }

        //só é possível confirmar uso se o horário do inicio da reserva superou o horário atual
        if(buscarHoraAtual().isBefore(locacao.getDataHoraInicioReserva())) {
            throw new BussinessException("Só é possível confirmar o uso após o início do horário da reserva");
        }

        locacao.setStatus(StatusLocacao.FINALIZADA);
        locacaoRepository.save(locacao);

        return null;
    }

    public Void aprovarReserva(Long idReserva, String token) {
        //recuperar o id do adm do token
        Long idAdm = Long.parseLong(tokenService.getIssuer(token, "idPessoa"));

        //recuperar reserva
        Locacao locacao = locacaoRepository.findById(idReserva)
                .orElseThrow(() -> new EntityNotFoundException("Locação não encontrada"));

        //validar status da reserva
        if(!locacao.getStatus().equals(StatusLocacao.SOLICITADA)) {
            throw new BussinessException("Status da locação não permite aprová-la");
        }

        locacao.setStatus(StatusLocacao.APROVADA);
        locacao.setIdAdministrador(idAdm);
        locacaoRepository.save(locacao);

        //enviar notificação e email informando a aprovação
        var ee = msCadastrosClient.buscarEspacoEsportivoPorId(locacao.getIdEspacoEsportivo());
        var cliente = msCadastrosClient.buscarClientePorId(locacao.getIdCliente());

        msComunicacoesClient.enviarEmail(TemplateEmails.emailAprovacaoReserva(cliente, locacao, ee));
        msComunicacoesClient.enviarNotificacao(TemplateNotificacoes.notificacaoAprovacaoReserva(locacao.getIdCliente(), locacao, ee));

        return null;
    }

    public Void negarReserva(Long idReserva, NegarReservaRequest request, String token) {
        //recuperar o id do adm do token
        Long idAdm = Long.parseLong(tokenService.getIssuer(token, "idPessoa"));

        //recuperar reserva
        Locacao locacao = locacaoRepository.findById(idReserva)
                .orElseThrow(() -> new EntityNotFoundException("Locação não encontrada"));

        //validar status da reserva
        if(!locacao.getStatus().equals(StatusLocacao.SOLICITADA)) {
            throw new BussinessException("Status da locação não permite negá-la");
        }

        locacao.setStatus(StatusLocacao.NEGADA);
        locacao.setIdAdministrador(idAdm);
        locacao.setMotivoCancelamento(request.getJustificativa());
        locacaoRepository.save(locacao);


        //enviar notificação e email informando a negação
        var ee = msCadastrosClient.buscarEspacoEsportivoPorId(locacao.getIdEspacoEsportivo());
        var cliente = msCadastrosClient.buscarClientePorId(locacao.getIdCliente());

        msComunicacoesClient.enviarEmail(TemplateEmails.emailNegacaoReserva(cliente, locacao, ee));
        msComunicacoesClient.enviarNotificacao(TemplateNotificacoes.notificacaoNegacaoReserva(locacao.getIdCliente(), locacao, ee));
        return null;
    }

    public Void encerrarReserva(Long idReserva, EncerrarReservaRequest request) {
        //recuperar reserva
        Locacao locacao = locacaoRepository.findById(idReserva)
                .orElseThrow(() -> new EntityNotFoundException("Locação não encontrada"));

        locacao.setStatus(StatusLocacao.ENCERRADA);
        if(request.getJustificativa() != null) {
            locacao.setMotivoEncerramento(request.getJustificativa());
        }
        locacaoRepository.save(locacao);

        //enviar notificação e email informando a aprovação
        var ee = msCadastrosClient.buscarEspacoEsportivoPorId(locacao.getIdEspacoEsportivo());
        var cliente = msCadastrosClient.buscarClientePorId(locacao.getIdCliente());

        msComunicacoesClient.enviarEmail(TemplateEmails.emailEncerramentoReserva(cliente, locacao, ee));
        msComunicacoesClient.enviarNotificacao(TemplateNotificacoes.notificacaoEncerramentoReserva(locacao.getIdCliente(), locacao, ee));

        return null;
    }

    public List<ReservaDetalhadaResponse> buscarRelatorioDeReservas() {
        var listaReservas = locacaoRepository.findAll();
        return buscarDetalhesReserva(listaReservas);
    }

    public List<ReservaDetalhadaResponse> listarReservasSolicitadas() {
        var listaReservasSolicitadas = locacaoRepository.listarReservasSolicitadas();
        return buscarDetalhesReserva(listaReservasSolicitadas);
    }

    private List<ReservaDetalhadaResponse> buscarDetalhesReserva(List<Locacao> listaReservas) {
        if (listaReservas.isEmpty()) {
            throw new EntityNotFoundException("Nenhuma reserva solicitada");
        }

        // Busca detalhes do cliente e do espaço esportivo da reserva
        var infComplementaresRequest = new ArrayList<InformacoesComplementaresLocacaoRequest>();
        listaReservas.forEach(reserva -> infComplementaresRequest.add(new InformacoesComplementaresLocacaoRequest(reserva)));
        var infComplementares = msCadastrosClient.buscarInformacoesComplementaresLocacao(infComplementaresRequest);

        // Monta o objeto de retorno
        var response = new ArrayList<ReservaDetalhadaResponse>();
        listaReservas.forEach(reserva -> response.add(new ReservaDetalhadaResponse(reserva)));
        response.forEach(reserva -> reserva.preencherInformacoesComplementares(infComplementares));

        return response;
    }

    public Void avaliarReserva(Long idReserva, AvaliacaoReservaRequest request, String token) {
        Long idCliente = Long.parseLong(tokenService.getIssuer(token, "idPessoa"));

        var reserva = locacaoRepository.findByIdAndIdCliente(idReserva, idCliente)
                .orElseThrow(() -> new EntityNotFoundException("Reserva não encontrada"));

        if(reserva.getAvaliacao() != null) {
            throw new BussinessException("Essa reserva já foi avaliada");
        }

        if(!reserva.getStatus().equals(StatusLocacao.FINALIZADA)) {
            throw new BussinessException("Status da locação não permite avaliá-la");
        }
        reserva.setAvaliacao(request.getAvaliacao());
        reserva.setComentarioCliente(request.getComentario());
        reserva.setDataHoraComentario(buscarHoraAtual());
        locacaoRepository.save(reserva);

        //atualizar média de avaliações do espaço esportivo
        msCadastrosClient.atualizarMediaAvaliacaoEE(reserva.getIdEspacoEsportivo(), new AtualizarMediaAvaliacaoEERequest(request.getAvaliacao()));

        return null;
    }

    public List<ComentarioEEResponse> listarComentariosPorEspacoEsportivo(Long idEspacoEsportivo) {
        //buscar todas as reservas do espaço esportivo especificado
        var listaReservas = locacaoRepository.findByIdEspacoEsportivo(idEspacoEsportivo);

        if(listaReservas.isEmpty()) {
            throw new EntityNotFoundException("Não existem comentários para esse espaço esportivo");
        }

        //criando os objetos com os comentários
        var listaComentarios = new ArrayList<ComentarioEEResponse>();
        listaReservas.forEach(reserva -> listaComentarios.add(new ComentarioEEResponse(reserva)));

        //buscar nomes dos clientes
        var listaIdsClientes = listaReservas.stream()
                .map(Locacao::getIdCliente)
                .distinct()
                .toList();
        var listaNomesClientes = msCadastrosClient.buscarNomesClientes(listaIdsClientes);
        listaComentarios.forEach(comentario -> comentario.preencherNomeCliente(listaNomesClientes));

        return listaComentarios;
    }

    public List<EstatisticasReservaResponse> buscarEstatisticasReserva() {
        var tuplas = locacaoRepository.buscarEstatisticasReservas();
        var listaEstatisticasReservaResponse = locacaoRepository.converterTuplesParaEstatisticasReservaResponse(tuplas);
        var listaEstatistica = new ArrayList<EstatisticasReservaResponse>();

        listaEstatisticasReservaResponse.forEach(estatistica -> listaEstatistica.add(new EstatisticasReservaResponse(estatistica)));

        return listaEstatistica;
    }

    @Transactional
    public Void excluirComentario(Long idLocacao) {
        if(!locacaoRepository.existsById(idLocacao)) {
            throw new EntityNotFoundException("Locação não encontrada");
        }
        locacaoRepository.excluirComentario(idLocacao);
        return null;
    }
}
