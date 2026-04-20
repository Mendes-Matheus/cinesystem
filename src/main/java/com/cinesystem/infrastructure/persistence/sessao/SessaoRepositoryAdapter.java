package com.cinesystem.infrastructure.persistence.sessao;

import com.cinesystem.domain.assento.AssentoId;
import com.cinesystem.domain.sessao.Sessao;
import com.cinesystem.domain.sessao.SessaoAssento;
import com.cinesystem.domain.sessao.SessaoId;
import com.cinesystem.domain.sessao.SessaoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class SessaoRepositoryAdapter implements SessaoRepository {

    private final SessaoJpaRepository sessaoJpaRepository;
    private final SessaoAssentoJpaRepository sessaoAssentoJpaRepository;
    private final SessaoJpaMapper mapper;

    public SessaoRepositoryAdapter(SessaoJpaRepository sessaoJpaRepository, 
                                   SessaoAssentoJpaRepository sessaoAssentoJpaRepository, 
                                   SessaoJpaMapper mapper) {
        this.sessaoJpaRepository = sessaoJpaRepository;
        this.sessaoAssentoJpaRepository = sessaoAssentoJpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Sessao save(Sessao sessao) {
        SessaoJpaEntity entity = mapper.toJpaSessao(sessao);
        SessaoJpaEntity saved = sessaoJpaRepository.save(entity);
        return mapper.toDomainSessao(saved);
    }

    @Override
    public Optional<Sessao> findById(SessaoId id) {
        return sessaoJpaRepository.findById(id.id())
                .map(mapper::toDomainSessao);
    }

    @Override
    public Optional<SessaoAssento> findSessaoAssento(SessaoId sessaoId, AssentoId assentoId) {
        return sessaoAssentoJpaRepository.findBySessaoIdAndAssentoId(sessaoId.id(), assentoId.id())
                .map(mapper::toDomainSessaoAssento);
    }

    @Override
    public List<SessaoAssento> findAssentosDisponiveis(SessaoId sessaoId) {
        return sessaoAssentoJpaRepository.findBySessaoId(sessaoId.id()).stream()
                .filter(entity -> entity.getStatus() == com.cinesystem.domain.assento.StatusAssento.DISPONIVEL)
                .map(mapper::toDomainSessaoAssento)
                .collect(Collectors.toList());
    }

    @Override
    public void saveAllAssentos(List<SessaoAssento> assentos) {
        List<SessaoAssentoJpaEntity> entities = assentos.stream()
                .map(mapper::toJpaSessaoAssento)
                .collect(Collectors.toList());
        sessaoAssentoJpaRepository.saveAll(entities);
    }

    @Override
    public List<SessaoAssento> findReservasExpiradas(java.time.LocalDateTime dataLimite) {
        return sessaoAssentoJpaRepository.findReservasExpiradas(dataLimite).stream()
                .map(mapper::toDomainSessaoAssento)
                .collect(Collectors.toList());
    }
}
