package br.ufpr.mslocacoes.client;

import br.ufpr.mslocacoes.model.dto.espaco_esportivos.AtualizarMediaAvaliacaoEERequest;
import br.ufpr.mslocacoes.model.dto.espaco_esportivos.EspEsportivoBuscaResponse;
import br.ufpr.mslocacoes.model.dto.locacao.InformacoesComplementaresLocacaoRequest;
import br.ufpr.mslocacoes.model.dto.locacao.InformacoesComplementaresLocacaoResponse;
import br.ufpr.mslocacoes.security.TokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class MsCadastrosClient {

    @Value("${url.ms.cadastros.espacos_esportivos}")
    private String urlMsCadastroEE;

    public static final String AUTHORIZATION_USER = "AuthorizationUser";

    private final RestTemplate restTemplate;
    private final TokenService tokenService;

    public MsCadastrosClient(RestTemplate restTemplate, TokenService tokenService) {
        this.restTemplate = restTemplate;
        this.tokenService = tokenService;
    }

    private HttpHeaders gerarCabecalho() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("AuthorizationApi", tokenService.gerarTokenMsLocacoes());
        return headers;
    }

    public EspEsportivoBuscaResponse buscarEspacoEsportivoPorId(Long idEspacoEsportivo) {
        String url = urlMsCadastroEE + "/" + idEspacoEsportivo;
        HttpHeaders headers = gerarCabecalho();
        var response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), Object.class).getBody();

        return new EspEsportivoBuscaResponse(response);
    }


    public List<InformacoesComplementaresLocacaoResponse> buscarInformacoesComplementaresLocacao(List<InformacoesComplementaresLocacaoRequest> request) {
        String url = urlMsCadastroEE + "/buscar-inf-complementares-locacao" ;
        HttpHeaders headers = gerarCabecalho();
        var response = restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(request, headers), new ParameterizedTypeReference<List<Object>>() {}).getBody();
        var listaInfComplementares = new ArrayList<InformacoesComplementaresLocacaoResponse>();
        assert response != null;
        response.forEach(obj -> listaInfComplementares.add(new InformacoesComplementaresLocacaoResponse(obj)));
        return listaInfComplementares;
    }

    public void atualizarMediaAvaliacaoEE(Long idEspacoEsportivo, AtualizarMediaAvaliacaoEERequest request) {
        String url = urlMsCadastroEE + "/atualizar-media-avaliacao/" + idEspacoEsportivo;
        HttpHeaders headers = gerarCabecalho();
        headers.set("AuthorizationApi", tokenService.gerarTokenMsLocacoes());
        restTemplate.exchange(url, HttpMethod.PUT, new HttpEntity<>(request, headers), Object.class);
    }
}
