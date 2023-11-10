package br.ufpr.mslocacoes.controller;

import br.ufpr.mslocacoes.model.dto.espaco_esportivo.ComentarioEEResponse;
import br.ufpr.mslocacoes.model.dto.locacao.*;
import br.ufpr.mslocacoes.service.LocacaoService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins ="*")
@RestController
@RequestMapping("/locacoes")
@Slf4j
public class LocacaoController {

    private final LocacaoService locacaoService;

    public LocacaoController(LocacaoService locacaoService) {
        this.locacaoService = locacaoService;
    }

    @PostMapping("/solicitar-locacao")
    public ResponseEntity<SolicitacaoLocacaoResponse> solicitarLocacao(
            @Valid @RequestBody SolicitacaoLocacaoRequest request,
            @RequestHeader("AuthorizationUser") String token) {
        return ResponseEntity.status(HttpStatus.CREATED).body(locacaoService.solicitarLocacao(request, token));
    }

    @PostMapping("/horarios-disponiveis")
    public ResponseEntity<HorariosDisponiveisResponse> verificarHorariosDisponiveisParaLocacao(
            @Valid @RequestBody HorarioDisponivelRequest horarioDisponivelRequest) {
        log.info("INICIO verificarHorariosDisponiveisParaLocacao ms-locacoes");
        return ResponseEntity.status(HttpStatus.OK).body(locacaoService.verificarHorariosDisponiveisParaLocacao(horarioDisponivelRequest));
    }

    //Uso do cliente - Reservas em andamento
    @GetMapping("/listar-reservas-em-andamento")
    public ResponseEntity<List<BuscaReservaResponse>> listarReservasEmAndamento(
            @RequestHeader("AuthorizationUser") String token) {
        return ResponseEntity.status(HttpStatus.OK).body(locacaoService.listarReservasEmAndamento(token));
    }

    //Uso do cliente - histórico de reservas
    @GetMapping("/listar-historico-reservas")
    public ResponseEntity<List<BuscaReservaResponse>> listarHistoricoReservas(
            @RequestHeader("AuthorizationUser") String token) {
        return ResponseEntity.status(HttpStatus.OK).body(locacaoService.listarHistoricoReservas(token));
    }

    @PutMapping("/cancelar-reserva/{idReserva}")
    public ResponseEntity<Void> cancelarReserva(
            @PathVariable Long idReserva,
            @RequestHeader("AuthorizationUser") String token) {
        return ResponseEntity.status(HttpStatus.OK).body(locacaoService.cancelarReserva(token, idReserva));
    }

    @PutMapping("/confirmar-uso/{idReserva}")
    public ResponseEntity<Void> confirmarUsoReserva(
            @PathVariable Long idReserva,
            @RequestHeader("AuthorizationUser") String token) {
        return ResponseEntity.status(HttpStatus.OK).body(locacaoService.confirmarUsoReserva(token, idReserva));
    }

    //uso do adm - todas as reservas solicitadas
    @GetMapping("/listar-reservas-solicitadas")
    public ResponseEntity<List<ReservaDetalhadaResponse>> listarReservasSolicitadas() {
        return ResponseEntity.status(HttpStatus.OK).body(locacaoService.listarReservasSolicitadas());
    }

    @PutMapping("/aprovar-reserva/{idReserva}")
    public ResponseEntity<Void> aprovarReserva(@PathVariable Long idReserva, @RequestHeader("AuthorizationUser") String token) {
        return ResponseEntity.status(HttpStatus.OK).body(locacaoService.aprovarReserva(idReserva, token));
    }

    @PutMapping("/negar-reserva/{idReserva}")
    public ResponseEntity<Void> negarReserva(@PathVariable Long idReserva,
                                             @RequestBody @Valid NegarReservaRequest negarReservaRequest,
                                             @RequestHeader("AuthorizationUser") String token) {
        return ResponseEntity.status(HttpStatus.OK).body(locacaoService.negarReserva(idReserva, negarReservaRequest, token));
    }

    //uso do adm - todas as reservas
    @GetMapping("/relatorio-reservas")
    public ResponseEntity<List<ReservaDetalhadaResponse>> buscarRelatorioDeReservas() {
        return ResponseEntity.status(HttpStatus.OK).body(locacaoService.buscarRelatorioDeReservas());
    }

    @PostMapping("/avaliar-reserva/{idReserva}")
    public ResponseEntity<Void> avaliarReserva(@PathVariable Long idReserva,
                                               @RequestBody @Valid AvaliacaoReservaRequest request,
                                               @RequestHeader("AuthorizationUser") String token) {
        return ResponseEntity.status(HttpStatus.OK).body(locacaoService.avaliarReserva(idReserva, request, token));
    }

    @GetMapping("/comentarios/{idEspacoEsportivo}")
    public ResponseEntity<List<ComentarioEEResponse>> listarComentariosPorEspacoEsportivo(@PathVariable Long idEspacoEsportivo) {
        return ResponseEntity.status(HttpStatus.OK).body(locacaoService.listarComentariosPorEspacoEsportivo(idEspacoEsportivo));
    }
}
