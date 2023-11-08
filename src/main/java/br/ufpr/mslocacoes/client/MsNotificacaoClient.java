package br.ufpr.mslocacoes.client;


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
public class MsNotificacaoClient {

    @Value("${url.ms.comunicacoes.notificacoes}")
    private String urlMsNotificacoes;


    public static final String AUTHORIZATION_USER = "AuthorizationUser";

    private final RestTemplate restTemplate;
    private final TokenService tokenService;

    public MsNotificacaoClient(RestTemplate restTemplate, TokenService tokenService) {
        this.restTemplate = restTemplate;
        this.tokenService = tokenService;
    }

    private HttpHeaders gerarCabecalho() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("AuthorizationApi", tokenService.gerarTokenMsLocacoes());
        return headers;
    }


    public void criarNotificacao(Long idCliente, String titulo, String conteudo) {
        String url = urlMsNotificacoes;
        HttpHeaders headers = gerarCabecalho();

        var body  = CriacaoNotificacaoRequest.builder()
                .idCliente(idCliente)
                .titulo(titulo)
                .conteudo(conteudo)
                .build();

        restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(body, headers), Object.class).getBody();
    }


}
