package br.ufpr.mslocacoes.constants;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public final class HorarioBrasil {

    private HorarioBrasil() {
        //vazio...
    }

    public static final LocalDateTime HORA_ATUAL = ZonedDateTime.now(ZoneOffset.ofHours(-3)).toLocalDateTime();
}
