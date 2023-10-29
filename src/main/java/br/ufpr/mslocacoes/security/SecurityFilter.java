package br.ufpr.mslocacoes.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class SecurityFilter extends OncePerRequestFilter {

    private final TokenService tokenService;

    @Autowired
    public SecurityFilter(TokenService tokenService) {
        this.tokenService = tokenService;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        var tokenJWT = recuperarToken(request);

        if(tokenJWT != null) {
            tokenService.validarToken(tokenJWT); //valida o token
            var authentication = new UsernamePasswordAuthenticationToken(tokenJWT, null, null);
            SecurityContextHolder.getContext().setAuthentication(authentication); //considera o usuário logado para essa requisição
        }

        filterChain.doFilter(request, response); //Continua o fluxo da aplicação
    }

    private String recuperarToken(HttpServletRequest request) {
        var authorizationHeader = request.getHeader("Authorization");
        if(authorizationHeader != null) {
            return authorizationHeader.replace("Bearer ", ""); //remove o prefixo
        }
        return null;
    }
}
