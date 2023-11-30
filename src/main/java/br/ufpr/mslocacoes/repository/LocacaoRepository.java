package br.ufpr.mslocacoes.repository;

import br.ufpr.mslocacoes.model.dto.locacao.EstatisticasReservaResponse;
import br.ufpr.mslocacoes.model.entity.Locacao;
import jakarta.persistence.Tuple;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface LocacaoRepository extends JpaRepository<Locacao, Long> {


   @Query(value = """
    SELECT
    CASE WHEN COUNT(l.id) > 0
        THEN TRUE
        ELSE FALSE
    END
    FROM tb_locacoes l
    WHERE ?1 >= l.data_hora_inicio_reserva AND ?1 < l.data_hora_fim_reserva
    AND l.id_espaco_esportivo = ?2
    AND (status = 'APROVADA' OR status = 'SOLICITADA')""", nativeQuery = true)
    long existeConflitoDeHorarioEspacoEsportivo(
            LocalDateTime horarioInicial, Long idEspacoEsportivo);

    @Query(value = """ 
    SELECT
    CASE WHEN COUNT(l.id) > 0
        THEN TRUE
        ELSE FALSE
    END
    FROM tb_locacoes l
    WHERE ?1 >= l.data_hora_inicio_reserva AND ?1 < l.data_hora_fim_reserva
    AND l.id_cliente = ?2
    AND (status = 'APROVADA' OR status = 'SOLICITADA')""", nativeQuery = true)
    long existeConflitoDeHorarioCliente(LocalDateTime horarioInicial, Long idCliente);


    @Query(value = """
    SELECT *
    FROM tb_locacoes l
    WHERE DATE(l.data_hora_inicio_reserva) = ?1
    AND l.id_espaco_esportivo = ?2
    AND (l.status = 'APROVADA' or l.status = 'SOLICITADA')""", nativeQuery = true)
    List<Locacao> buscarLocacoesPorDiaEIdEspacoEsportivo(
            LocalDate data, Long idEspacoEsportivo);

 @Query(value = """
    SELECT *
    FROM tb_locacoes l
    WHERE  l.id_cliente = ?1
    AND (l.status = 'APROVADA' or l.status = 'SOLICITADA')""", nativeQuery = true)
 List<Locacao> listarReservasEmAndamento(Long idCliente);

 Optional<Locacao> findByIdAndIdCliente(Long id, Long idCliente);

 List<Locacao> findByIdCliente(Long idCliente);

 List<Locacao> findByIdEspacoEsportivo(Long idEspacoEsportivo);

 @Query(value = """
    SELECT *
    FROM tb_locacoes l
    WHERE  l.status = 'SOLICITADA'""", nativeQuery = true)
 List<Locacao> listarReservasSolicitadas();

 @Query(value = """
    SELECT *
    FROM tb_locacoes l
    WHERE  l.id_cliente = ?1  AND DATE(l.data_hora_inicio_reserva) = ?2 AND l.id_espaco_esportivo = ?3
    AND (l.status = 'APROVADA' or l.status = 'SOLICITADA')
    """, nativeQuery = true)
    List<Locacao> buscarLocacaoPorDiaEIdClienteEEspacoEsportivo(Long idCliente, LocalDate data, Long idEspacoEsportivo);

 @Transactional
 @Query(value = """
    UPDATE tb_locacoes l SET comentario_cliente=null
    WHERE  l.id = ?1""", nativeQuery = true)
 @Modifying
 void excluirComentario(Long idLocacao);


 @Query(value = """
        SELECT
            id_cliente as idCliente,
            COUNT(*) as totalReservas,
            SUM(CASE WHEN status = 'SOLICITADA' THEN 1 ELSE 0 END) as totalReservasSolicitadas,
            SUM(CASE WHEN status = 'CANCELADA' THEN 1 ELSE 0 END) as totalReservasCanceladas,
            SUM(CASE WHEN status = 'APROVADA' THEN 1 ELSE 0 END) as totalReservasAprovadas,
            SUM(CASE WHEN status = 'NEGADA' THEN 1 ELSE 0 END) as totalReservasNegadas,
            SUM(CASE WHEN status = 'FINALIZADA' THEN 1 ELSE 0 END) as totalReservasFinalizadas,
            SUM(CASE WHEN status = 'ENCERRADA' THEN 1 ELSE 0 END) as totalReservasEncerradas
        FROM
            tb_locacoes
        GROUP BY
            id_cliente;
        """, nativeQuery = true)
 List<Tuple> buscarEstatisticasReservas();


 default List<EstatisticasReservaResponse> converterTuplesParaEstatisticasReservaResponse(List<Tuple> tuples) {
  return tuples.stream()
          .map(this::converterTupleParaEstatisticasReservaResponse)
          .toList();
 }

 private EstatisticasReservaResponse converterTupleParaEstatisticasReservaResponse(Tuple tuple) {
  return EstatisticasReservaResponse.builder()
          .idCliente(tuple.get("idCliente", Long.class))
          .totalReservas(tuple.get("totalReservas", Long.class).intValue())
          .totalReservasSolicitadas(tuple.get("totalReservasSolicitadas", BigDecimal.class).intValue())
          .totalReservasCanceladas(tuple.get("totalReservasCanceladas", BigDecimal.class).intValue())
          .totalReservasAprovadas(tuple.get("totalReservasAprovadas", BigDecimal.class).intValue())
          .totalReservasNegadas(tuple.get("totalReservasNegadas", BigDecimal.class).intValue())
          .totalReservasFinalizadas(tuple.get("totalReservasFinalizadas", BigDecimal.class).intValue())
          .totalReservasEncerradas(tuple.get("totalReservasEncerradas", BigDecimal.class).intValue())
          .build();
 }

 @Transactional
 @Query(value = """
    UPDATE tb_locacoes l SET l.status='ENCERRADA', motivo_encerramento=?2
    WHERE  l.id_espaco_esportivo = ?1
    AND (l.status = 'APROVADA' OR l.status = 'SOLICITADA')
    AND data_hora_inicio_reserva > CONVERT_TZ(NOW(), '+00:00', '-03:00')""", nativeQuery = true)
 @Modifying
 void encerrarReservasFuturas(Long idEspacoEsportivo, String motivoEncerramento);

 @Query(value = """
    SELECT * FROM tb_locacoes l
    WHERE  l.id_espaco_esportivo = ?1
    AND (l.status = 'APROVADA' OR l.status = 'SOLICITADA')
    AND data_hora_inicio_reserva > CONVERT_TZ(NOW(), '+00:00', '-03:00')""", nativeQuery = true)
 List<Locacao> buscarReservasFuturas(Long idEspacoEsportivo);
}
