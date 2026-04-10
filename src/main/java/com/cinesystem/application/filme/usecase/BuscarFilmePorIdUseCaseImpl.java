package com.cinesystem.application.filme.usecase;

import com.cinesystem.application.filme.dto.FilmeResult;
import com.cinesystem.application.port.out.query.FilmeQueryPort;
import com.cinesystem.domain.filme.FilmeId;
import com.cinesystem.domain.shared.ResourceNotFoundException;
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
