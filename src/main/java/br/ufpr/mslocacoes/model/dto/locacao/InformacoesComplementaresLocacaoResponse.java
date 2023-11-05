package br.ufpr.mslocacoes.model.dto.locacao;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import java.util.LinkedHashMap;

@AllArgsConstructor
@Getter
@Builder
public class InformacoesComplementaresLocacaoResponse {
    private Long idLocacao;

    private Long idEspacoEsportivo;
    private String nomeEspacoEsportivo;
    private String localidadeEspacoEsportivo;

    private Long idCliente;
    private String nomeCliente;
    private String cpfCliente;
    private Boolean alunoUFPR;
    private String grr;

    public InformacoesComplementaresLocacaoResponse(Object obj) {
        if (obj instanceof LinkedHashMap<?, ?> hm) {
            this.idLocacao = Long.valueOf((Integer) hm.get("idLocacao"));
            this.idEspacoEsportivo = Long.valueOf((Integer) hm.get("idEspacoEsportivo"));
            this.idCliente = Long.valueOf((Integer) hm.get("idCliente"));
            this.alunoUFPR = (Boolean) hm.get("alunoUFPR");
            this.nomeEspacoEsportivo = (String) hm.get("nomeEspacoEsportivo");
            this.localidadeEspacoEsportivo = (String) hm.get("localidadeEspacoEsportivo");
            this.nomeCliente = (String) hm.get("nomeCliente");
            this.cpfCliente = (String) hm.get("cpfCliente");
            this.grr = (String) hm.get("grr");
        }
    }
}
