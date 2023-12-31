package br.ufpr.mslocacoes.model.dto.espaco_esportivo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class EspEsportivoBuscaResponse {
    private Long id;
    private String nome;
    private String descricao;
    private String localidade;
    private String piso;
    private String dimensoes;
    private Short capacidadeMin;
    private Short capacidadeMax;
    private Boolean disponivel;
    private LocalTime horaAbertura;
    private LocalTime horaFechamento;
    private LocalTime periodoLocacao;
    private Integer maxLocacaoDia;
    private List<EsporteResponse> listaEsportes = new ArrayList<>();
    private String imagemBase64;
    private List<Integer> diasFuncionamento;
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    public EspEsportivoBuscaResponse(Object response) {
        if (response instanceof LinkedHashMap<?, ?> hm) {
            this.id = Long.valueOf((Integer) hm.get("id"));
            this.nome = (String) hm.get("nome");
            this.descricao = (String) hm.get("descricao");
            this.localidade = (String) hm.get("localidade");
            this.piso = (String) hm.get("piso");
            this.dimensoes = (String) hm.get("dimensoes");
            this.capacidadeMin = (short) ((int) hm.get("capacidadeMin"));
            this.capacidadeMax = (short) ((int) hm.get("capacidadeMax"));
            this.disponivel = (Boolean) hm.get("disponivel");
            this.horaAbertura =  LocalTime.parse((String) hm.get("horaAbertura"), formatter);
            this.horaFechamento = LocalTime.parse((String) hm.get("horaFechamento"), formatter);
            this.periodoLocacao = LocalTime.parse((String) hm.get("periodoLocacao"), formatter);
            this.maxLocacaoDia = (Integer) hm.get("maxLocacaoDia");

            ArrayList<?> listaEsportesObj = (ArrayList<?>) hm.get("listaEsportes");
            if(listaEsportesObj != null) {
                listaEsportesObj.forEach(esporte -> this.listaEsportes.add(new EsporteResponse(esporte)));
            }

            ArrayList<?> diasFuncionamentoObj = (ArrayList<?>) hm.get("diasFuncionamento");

            this.diasFuncionamento = (ArrayList<Integer>) diasFuncionamentoObj; //pode confiar =D

            this.imagemBase64 = (String) hm.get("imagemBase64");
        }


    }
}
