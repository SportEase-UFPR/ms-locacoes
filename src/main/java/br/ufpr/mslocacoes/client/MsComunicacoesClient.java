package br.ufpr.mslocacoes.client;


import br.ufpr.mslocacoes.model.dto.email.CriacaoEmailRequest;
import br.ufpr.mslocacoes.model.dto.notificacao.CriacaoNotificacaoRequest;
import br.ufpr.mslocacoes.security.TokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


@Service
public class MsComunicacoesClient {

    @Value("${url.ms.comunicacoes}")
    private String urlMsComunicacoes;

    private final RestTemplate restTemplate;
    private final TokenService tokenService;

    public MsComunicacoesClient(RestTemplate restTemplate, TokenService tokenService) {
        this.restTemplate = restTemplate;
        this.tokenService = tokenService;
    }

    private HttpHeaders gerarCabecalho() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("AuthorizationApi", tokenService.gerarTokenMs());
        return headers;
    }


    public void enviarNotificacao(CriacaoNotificacaoRequest request) {
        String url = urlMsComunicacoes + "/notificacoes";
        HttpHeaders headers = gerarCabecalho();

        restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(request, headers), Object.class).getBody();
    }

    public void enviarEmail(CriacaoEmailRequest request) {
        String url = urlMsComunicacoes + "/email/via-ms";
        HttpHeaders headers = gerarCabecalho();
        restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(request, headers), Object.class);
    }
}
