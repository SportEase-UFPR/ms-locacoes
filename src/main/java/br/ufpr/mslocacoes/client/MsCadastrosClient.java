package br.ufpr.mslocacoes.client;

import br.ufpr.mslocacoes.model.dto.espaco_esportivos.EspEsportivoBuscaResponse;
import br.ufpr.mslocacoes.security.TokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

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
}
