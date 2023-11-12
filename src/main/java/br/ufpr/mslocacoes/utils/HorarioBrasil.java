package br.ufpr.mslocacoes.utils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public final class HorarioBrasil {

    private HorarioBrasil() {
        //vazio...
    }

    public static LocalDateTime buscarHoraAtual() {
        return ZonedDateTime.now(ZoneOffset.ofHours(-3)).toLocalDateTime();
    }
}
