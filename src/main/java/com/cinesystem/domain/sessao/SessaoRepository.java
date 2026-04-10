package com.cinesystem.domain.sessao;

import com.cinesystem.domain.assento.AssentoId;

import java.util.List;
import java.util.Optional;

public interface SessaoRepository {
    Sessao save(Sessao sessao);
    Optional<Sessao> findById(SessaoId id);
    Optional<SessaoAssento> findSessaoAssento(SessaoId sessaoId, AssentoId assentoId);
    List<SessaoAssento> findAssentosDisponiveis(SessaoId sessaoId);
    void saveAllAssentos(List<SessaoAssento> assentos);
}
