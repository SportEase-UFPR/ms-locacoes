package br.ufpr.mslocacoes.repository;

import br.ufpr.mslocacoes.model.entity.Locacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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

 @Query(value = """
    SELECT *
    FROM tb_locacoes l
    WHERE  l.status = 'SOLICITADA'""", nativeQuery = true)
 List<Locacao> listarReservasSolicitadas();
}
