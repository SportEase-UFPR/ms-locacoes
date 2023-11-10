package br.ufpr.mslocacoes.service;

import br.ufpr.mslocacoes.client.MsCadastrosClient;
import br.ufpr.mslocacoes.client.MsNotificacaoClient;
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
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static br.ufpr.mslocacoes.constants.HorarioBrasil.HORA_ATUAL;

@Service
public class LocacaoService {

    private final TokenService tokenService;
    private final LocacaoRepository locacaoRepository;
    private final MsCadastrosClient msCadastrosClient;
    private final MsNotificacaoClient msNotificacaoClient;

    public LocacaoService(TokenService tokenService, LocacaoRepository locacaoRepository, MsCadastrosClient msCadastrosClient, MsNotificacaoClient msNotificacaoClient) {
        this.tokenService = tokenService;
        this.locacaoRepository = locacaoRepository;
        this.msCadastrosClient = msCadastrosClient;
        this.msNotificacaoClient = msNotificacaoClient;
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
            throw new BussinessException("Espaço esportivo está indisponível");
        }

        //verificar se dataHoraInicioReserva > horário atual
        if(dataHoraInicioReserva.isBefore(HORA_ATUAL)) {
            throw new BussinessException("dataHoraInicioReserva deve ser futuro a dataHora atual");
        }

        //verificar se dataHoraFimReserva > dataHoraInicioReserva
        if(dataHoraInicioReserva.isAfter(dataHoraFimReserva)) {
            throw new BussinessException("dataHoraFimReserva deve ser futuro à dataHoraInicioReserva");
        }

        //verificar se dataHoraInicioReserva e dataHoraFimReserva pertencem ao mesmo dia
        if(!dataHoraInicioReserva.toLocalDate().isEqual(dataHoraFimReserva.toLocalDate())) {
            throw new BussinessException("o início e o fim de uma reserva deve pertencer ao mesmo dia");
        }

        //verificar se dataHoraInicioReserva >= horarioAbertura
        if(dataHoraInicioReserva.toLocalTime().isBefore(ee.getHoraAbertura())) {
            throw new BussinessException("dataHoraInicioReserva não está dentro do período de funcionamento do espaço esportivo");
        }

        //verificar se dataHoraFimReserva <= horarioAbertura
        if(dataHoraFimReserva.toLocalTime().isAfter(ee.getHoraFechamento())) {
            throw new BussinessException("dataHoraFimReserva não está dentro do período de funcionamento do espaço esportivo");
        }

        //verificar se há conflito de horário para o espaço esportivo escolhido
        if(locacaoRepository.existeConflitoDeHorarioEspacoEsportivo(request.getDataHoraInicioReserva(), request.getIdEspacoEsportivo()) == 1
        ) {
            throw new BussinessException("já existe uma reserva para esse horário no espaço esportivo informado");
        }


        //verificar se o cliente já não solicitou uma reserva em algum espaço esportivo no mesmo dia e horário informado.
        if(locacaoRepository.existeConflitoDeHorarioCliente(request.getDataHoraInicioReserva(), idCliente) == 1
        ) {
            throw new BussinessException("O cliente já solicitou uma reserva para esse dia e horário informado");
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
            throw new BussinessException("o período de locação solicitado excede o máximo permitido");
        }

        //Criar locação
        Locacao novaLocacao = new Locacao(request, idCliente);
        locacaoRepository.save(novaLocacao);

        return new SolicitacaoLocacaoResponse(novaLocacao);
    }

    public HorariosDisponiveisResponse verificarHorariosDisponiveisParaLocacao(HorarioDisponivelRequest request) {
        //buscar locações solicitadas no dia informado
        List<Locacao> locacoes = locacaoRepository.buscarLocacoesPorDiaEIdEspacoEsportivo(request.getData(), request.getIdEspacoEsportivo());

        //Recuperar espaço esportivo
        EspEsportivoBuscaResponse ee = null;
        try {
            ee = msCadastrosClient.buscarEspacoEsportivoPorId(request.getIdEspacoEsportivo());
        } catch (HttpClientErrorException e) {
            throw new EntityNotFoundException("Espaço esportivo não cadastrado");
        }

        //verificar horários válidos para o espaço esportivo informado
        HorariosDisponiveisResponse response = new HorariosDisponiveisResponse(ee, ee.getPeriodoLocacao());


        // Remover as horas que já estão ocupadas
        locacoes.forEach(locacao -> {
            LocalTime horaInteiraInicio = locacao.getDataHoraInicioReserva().toLocalTime();
            LocalTime horaInteiraFim = locacao.getDataHoraFimReserva().toLocalTime();

            response.getHorariosDisponiveis().removeIf(hora ->
                    (hora.equals(horaInteiraInicio) || hora.isAfter(horaInteiraInicio)) &&
                    hora.isBefore(horaInteiraFim));
        });

        return response;
    }

    public List<BuscaReservaResponse> listarReservasEmAndamento(String token) {
        //recuperar id do cliente do token
        Long idCliente = Long.parseLong(tokenService.getIssuer(token, "idPessoa"));

        List<Locacao> listaReservas = locacaoRepository.listarReservasEmAndamento(idCliente);

        List<BuscaReservaResponse> response = new ArrayList<>();
        listaReservas.forEach(reserva -> response.add(new BuscaReservaResponse(reserva)));
        return response;
    }


    public List<BuscaReservaResponse> listarHistoricoReservas(String token) {
        //recuperar id do cliente do token
        Long idCliente = Long.parseLong(tokenService.getIssuer(token, "idPessoa"));

        List<Locacao> listaReservas = locacaoRepository.findByIdCliente(idCliente);

        List<BuscaReservaResponse> response = new ArrayList<>();
        listaReservas.forEach(reserva -> response.add(new BuscaReservaResponse(reserva)));
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

        if(HORA_ATUAL.plusMinutes(15).isAfter(locacao.getDataHoraInicioReserva())) {
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
        if(HORA_ATUAL.isBefore(locacao.getDataHoraInicioReserva())) {
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

        //TODO enviar notificação via email e colocar os textos das notificações em outra classe
        var ee = msCadastrosClient.buscarEspacoEsportivoPorId(locacao.getIdEspacoEsportivo());

        var diaLocacao = locacao.getDataHoraInicioReserva().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        var horaInicioLocacao = locacao.getDataHoraInicioReserva().format(DateTimeFormatter.ofPattern("HH:mm"));
        var horaFimLocacao =  locacao.getDataHoraFimReserva().format(DateTimeFormatter.ofPattern("HH:mm"));

        msNotificacaoClient.criarNotificacao(locacao.getIdCliente(), "SUA RESERVA FOI APROVADA!",
                "Que notícia boa, sua reserva para o espaço '" + ee.getNome()
                + "' no dia " + diaLocacao + " - " + horaInicioLocacao + " às " + horaFimLocacao + " foi aprovada :)");

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


        //TODO enviar notificação via email e colocar os textos das notificações em outra classe
        var ee = msCadastrosClient.buscarEspacoEsportivoPorId(locacao.getIdEspacoEsportivo());
        var diaLocacao = locacao.getDataHoraInicioReserva().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        var horaInicioLocacao = locacao.getDataHoraInicioReserva().format(DateTimeFormatter.ofPattern("HH:mm"));
        var horaFimLocacao =  locacao.getDataHoraFimReserva().format(DateTimeFormatter.ofPattern("HH:mm"));


        msNotificacaoClient.criarNotificacao(locacao.getIdCliente(), "SUA RESERVA FOI NEGADA",
                "Infelizmente sua reserva para o espaço '" + ee.getNome() +
                "' no dia " + diaLocacao + " - " + horaInicioLocacao + " às " + horaFimLocacao + " foi negada pelo seguinte motivo: "
                + request.getJustificativa());
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
        reserva.setDataHoraComentario(HORA_ATUAL);
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
}
