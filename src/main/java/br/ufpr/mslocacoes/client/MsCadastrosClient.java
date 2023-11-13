package br.ufpr.mslocacoes.client;

import br.ufpr.mslocacoes.model.dto.cliente.ClienteBuscaResponse;
import br.ufpr.mslocacoes.model.dto.cliente.NomeClienteResponse;
import br.ufpr.mslocacoes.model.dto.espaco_esportivo.AtualizarMediaAvaliacaoEERequest;
import br.ufpr.mslocacoes.model.dto.espaco_esportivo.EspEsportivoBuscaResponse;
import br.ufpr.mslocacoes.model.dto.espaco_esportivo.EspacoEsportivoSimplificado;
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

    @Value("${url.ms.cadastros}")
    private String urlMsCadastro;

    private final RestTemplate restTemplate;
    private final TokenService tokenService;

    public MsCadastrosClient(RestTemplate restTemplate, TokenService tokenService) {
        this.restTemplate = restTemplate;
        this.tokenService = tokenService;
    }

    private HttpHeaders gerarCabecalho() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("AuthorizationApi", tokenService.gerarTokenMs());
        return headers;
    }

    public EspEsportivoBuscaResponse buscarEspacoEsportivoPorId(Long idEspacoEsportivo) {
        String url = urlMsCadastro + "/espacos-esportivos/" + idEspacoEsportivo;
        HttpHeaders headers = gerarCabecalho();
        var response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), Object.class).getBody();

        return new EspEsportivoBuscaResponse(response);
    }


    public List<InformacoesComplementaresLocacaoResponse> buscarInformacoesComplementaresLocacao(List<InformacoesComplementaresLocacaoRequest> request) {
        String url = urlMsCadastro + "/espacos-esportivos/buscar-inf-complementares-locacao" ;
        HttpHeaders headers = gerarCabecalho();
        var response = restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(request, headers), new ParameterizedTypeReference<List<Object>>() {}).getBody();
        var listaInfComplementares = new ArrayList<InformacoesComplementaresLocacaoResponse>();
        assert response != null;
        response.forEach(obj -> listaInfComplementares.add(new InformacoesComplementaresLocacaoResponse(obj)));
        return listaInfComplementares;
    }

    public void atualizarMediaAvaliacaoEE(Long idEspacoEsportivo, AtualizarMediaAvaliacaoEERequest request) {
        String url = urlMsCadastro + "/espacos-esportivos/atualizar-media-avaliacao/" + idEspacoEsportivo;
        HttpHeaders headers = gerarCabecalho();
        restTemplate.exchange(url, HttpMethod.PUT, new HttpEntity<>(request, headers), Object.class);
    }

    public List<NomeClienteResponse> buscarNomesClientes(List<Long> request) {
        String url = urlMsCadastro + "/espacos-esportivos/buscar-lista-nomes";
        HttpHeaders headers = gerarCabecalho();
        var response = restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(request, headers), new ParameterizedTypeReference<List<Object>>() {}).getBody();
        var listaNomes = new ArrayList<NomeClienteResponse>();
        assert response != null;
        response.forEach(obj -> listaNomes.add(new NomeClienteResponse(obj)));
        return listaNomes;
    }

    public List<EspacoEsportivoSimplificado> buscarEspacoesEsportivosSimplificado(List<Long> request) {
        String url = urlMsCadastro + "/espacos-esportivos/buscar-lista-ee-simplificado";
        HttpHeaders headers = gerarCabecalho();
        var response = restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(request, headers), new ParameterizedTypeReference<List<Object>>() {}).getBody();
        var listaEE = new ArrayList<EspacoEsportivoSimplificado>();
        assert response != null;
        response.forEach(obj -> listaEE.add(new EspacoEsportivoSimplificado(obj)));
        return listaEE;
    }

    public ClienteBuscaResponse buscarClientePorId(Long idCliente) {
        String url = urlMsCadastro + "/clientes/via-ms/" + idCliente;
        HttpHeaders headers = gerarCabecalho();
        var response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), Object.class).getBody();
        return new ClienteBuscaResponse(response);

    }
}
