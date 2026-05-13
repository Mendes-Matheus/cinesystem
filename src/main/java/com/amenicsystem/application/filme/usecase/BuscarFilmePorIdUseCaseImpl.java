package com.amenicsystem.application.filme.usecase;

import com.amenicsystem.application.filme.dto.FilmeResult;
import com.amenicsystem.application.port.out.query.FilmeQueryPort;
import com.amenicsystem.domain.filme.FilmeId;
import com.amenicsystem.domain.shared.ResourceNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class BuscarFilmePorIdUseCaseImpl implements BuscarFilmePorIdUseCase {

    private final FilmeQueryPort filmeQueryPort;

    public BuscarFilmePorIdUseCaseImpl(FilmeQueryPort filmeQueryPort) {
        this.filmeQueryPort = filmeQueryPort;
    }

    @Override
    public FilmeResult execute(FilmeId filmeId) {
        return filmeQueryPort.findResultById(filmeId)
                .orElseThrow(() -> new ResourceNotFoundException("Filme não encontrado: " + filmeId.id()));
    }
}
