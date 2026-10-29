
package br.edu.ifrn.helpif_api.exceptions;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> tratarValidacao(
            MethodArgumentNotValidException exception,
            HttpServletRequest request) {

        String mensagem = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(erro -> erro.getField() + ": "
                        + erro.getDefaultMessage())
                .sorted()
                .collect(Collectors.joining("; "));

        return criarResposta(
                HttpStatus.BAD_REQUEST,
                mensagem,
                request
        );
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErroResponse> tratarRegraNegocio(
            ResponseStatusException exception,
            HttpServletRequest request) {

        return criarResposta(
                exception.getStatusCode(),
                exception.getReason(),
                request
        );
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErroResponse> tratarRegraNegocio(
            RegraNegocioException exception, HttpServletRequest request) {
        return criarResposta(HttpStatus.CONFLICT, exception.getMessage(), request);
    }

    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ResponseEntity<ErroResponse> tratarIntegridade(HttpServletRequest request) {
        return criarResposta(HttpStatus.CONFLICT,
                "Operação viola unicidade ou vínculo entre registros", request);
    }

    @ExceptionHandler({org.springframework.http.converter.HttpMessageNotReadableException.class,
            org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ErroResponse> tratarFormato(HttpServletRequest request) {
        return criarResposta(HttpStatus.BAD_REQUEST, "Dados da requisição inválidos", request);
    }

    private ResponseEntity<ErroResponse> criarResposta(
            org.springframework.http.HttpStatusCode status,
            String mensagem,
            HttpServletRequest request) {

        ErroResponse erro = new ErroResponse(
                LocalDateTime.now(),
                status.value(),
                mensagem,
                request.getRequestURI()
        );

        return ResponseEntity.status(status).body(erro);
    }

    public record ErroResponse(
            LocalDateTime timestamp,
            int status,
            String mensagem,
            String caminho
    ) {}
}
