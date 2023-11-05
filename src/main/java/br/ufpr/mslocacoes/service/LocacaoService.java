package br.ufpr.mslocacoes.service;

import br.ufpr.mslocacoes.client.MsCadastrosClient;
import br.ufpr.mslocacoes.exceptions.BussinessException;
import br.ufpr.mslocacoes.exceptions.EntityNotFoundException;
import br.ufpr.mslocacoes.model.dto.espaco_esportivos.EspEsportivoBuscaResponse;
import br.ufpr.mslocacoes.model.dto.locacao.*;
import br.ufpr.mslocacoes.model.entity.Locacao;
import br.ufpr.mslocacoes.model.enums.StatusLocacao;
import br.ufpr.mslocacoes.repository.LocacaoRepository;
import br.ufpr.mslocacoes.security.TokenService;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class LocacaoService {

    private final TokenService tokenService;
    private final LocacaoRepository locacaoRepository;
    private final MsCadastrosClient msCadastrosClient;

    public LocacaoService(TokenService tokenService, LocacaoRepository locacaoRepository, MsCadastrosClient msCadastrosClient) {
        this.tokenService = tokenService;
        this.locacaoRepository = locacaoRepository;
        this.msCadastrosClient = msCadastrosClient;
    }

    public SolicitacaoLocacaoResponse solicitarLocacao(SolicitacaoLocacaoRequest request, String token) {
        //recuperar id do cliente do token
        Long idCliente = Long.parseLong(tokenService.getIssuer(token, "idPessoa"));

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

        //verificar se dataHoraFimReserva > dataHoraInicioReserva
        if(request.getDataHoraInicioReserva().isAfter(request.getDataHoraFimReserva())) {
            throw new BussinessException("dataHoraFimReserva deve ser futuro à dataHoraInicioReserva");
        }

        //verificar se dataHoraInicioReserva e dataHoraFimReserva pertencem ao mesmo dia
        if(!request.getDataHoraInicioReserva().toLocalDate().isEqual(request.getDataHoraFimReserva().toLocalDate())) {
            throw new BussinessException("o início e o fim de uma reserva deve pertencer ao mesmo dia");
        }

        //verificar se o tempo entre dataHoraInicioReserva e dataHoraFimReserva não excede o tempo máximo permitido
        Duration duracaoLocacao = Duration.between(request.getDataHoraInicioReserva(), request.getDataHoraFimReserva());
        Duration duracaoCiclo = Duration.between(LocalTime.MIN, ee.getPeriodoLocacao());
        if(duracaoLocacao.toMillis() > ee.getMaxLocacaoDia() * duracaoCiclo.toMillis()) {
            throw new BussinessException("o período de locação solicitado excede o máximo permitido");
        }

        //verificar se dataHoraInicioReserva >= horarioAbertura
        if(request.getDataHoraInicioReserva().toLocalTime().isBefore(ee.getHoraAbertura())) {
            throw new BussinessException("dataHoraInicioReserva não está dentro do período de funcionamento do espaço esportivo");
        }

        //verificar se dataHoraFimReserva <= horarioAbertura
        if(request.getDataHoraFimReserva().toLocalTime().isAfter(ee.getHoraFechamento())) {
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
        if(LocalDateTime.now().isBefore(locacao.getDataHoraInicioReserva())) {
            throw new BussinessException("Só é possível confirmar o uso após o início do horário da reserva");
        }

        locacao.setStatus(StatusLocacao.FINALIZADA);
        locacaoRepository.save(locacao);

        return null;
    }

    public List<BuscaReservaResponse> listarReservasSolicitadas() {
        List<Locacao> listaReservasSolicitadas = locacaoRepository.listarReservasSolicitadas();

        if(listaReservasSolicitadas.isEmpty()) {
            throw new EntityNotFoundException("Nenhuma reserva solicitada");
        }

        List<BuscaReservaResponse> response = new ArrayList<>();
        listaReservasSolicitadas.forEach(reserva -> response.add(new BuscaReservaResponse(reserva)));
        return response;
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


        //TODO enviar notificação e email para o usuário informando que a reserva foi aprovada
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


        //TODO enviar notificação e email para o usuário informando que a reserva foi negada
        return null;
    }
}
