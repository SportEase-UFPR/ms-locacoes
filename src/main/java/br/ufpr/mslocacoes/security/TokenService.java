package br.ufpr.mslocacoes.security;

import br.ufpr.mslocacoes.exceptions.TokenInvalidoException;
import br.ufpr.mslocacoes.model.enums.NivelAcesso;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static br.ufpr.mslocacoes.constants.HorarioBrasil.HORA_ATUAL;

@Slf4j
@Service
public class TokenService {

    @Value("${api.security.token.mslocacoes.secret}")
    private String msLocacoesSecret;

    @Value("${api.security.token.apigateway.secret}")
    private String apiGatewaySecret;

    @Value("${api.gateway.issuer}")
    private String apiGatewayIssuer;

    @Value("${api.mslocacoes.issuer}")
    private String msLocacoesIssuer;


    public void validarToken(String tokenJWT) {
        var tokenFormatado = removerPrefixoToken(tokenJWT);
        try {
            var algoritmo = Algorithm.HMAC256(apiGatewaySecret);
            JWT.require(algoritmo)
                    .withIssuer(apiGatewayIssuer)
                    .build()
                    .verify(tokenFormatado);
        } catch (JWTVerificationException ex) {
            log.error(ex.getMessage());
            throw new TokenInvalidoException("Token JWT inválido ou expirado");
        }
    }

    public String gerarTokenMsLocacoes() {
        var algoritmo = Algorithm.HMAC256(msLocacoesSecret);
        return JWT.create()
                .withIssuer(msLocacoesIssuer)
                .withSubject(msLocacoesIssuer)
                .withExpiresAt(dataExpiracao(20)) //data da expiração
                .sign(algoritmo); //assinatura

    }

    public String removerPrefixoToken(String token) {
        return token.replace("Bearer ", "");
    }

    //recupera um issuer do token
    public String getIssuer(String tokenJWT, String issuer) {
        return JWT.decode(tokenJWT).getClaim(issuer).asString();
    }

    //recupera o subject do token
    public String getSubject(String tokenJWT) {
        return JWT.decode(tokenJWT).getSubject();
    }

    private Instant dataExpiracao(Integer minutes) {
        return HORA_ATUAL.plusMinutes(minutes).toInstant(ZoneOffset.of("-03:00"));
    }
}
