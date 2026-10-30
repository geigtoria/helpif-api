package br.edu.ifrn.helpif_api.controllers;

import br.edu.ifrn.helpif_api.services.CategoriaService;
import br.edu.ifrn.helpif_api.services.SetorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

class SetorControllerTest {
    private final SetorService service = mock(SetorService.class);
    private MockMvc mvc;

    @BeforeEach
    void preparar() {
        var resolver = new PageableHandlerMethodArgumentResolver();
        resolver.setMaxPageSize(50);
        mvc = MockMvcBuilders.standaloneSetup(new SetorController(service, mock(CategoriaService.class)))
                .setCustomArgumentResolvers(resolver).build();
        when(service.listar(nullable(String.class), any(Pageable.class)))
                .thenAnswer(i -> Page.empty(i.getArgument(1)));
    }

    @Test
    void aceitaOrdenacaoECombinacaoComFiltroDeNome() throws Exception {
        var resultado = mvc.perform(get("/setores")
                .param("nome", "tecnologia").param("sort", "sigla,desc")).andReturn();
        assertEquals(200, resultado.getResponse().getStatus());
        var captor = ArgumentCaptor.forClass(Pageable.class);
        verify(service).listar(eq("tecnologia"), captor.capture());
        assertEquals(Sort.by(Sort.Order.desc("sigla")), captor.getValue().getSort());
    }

    @Test
    void mantemPaginacaoEOrdenacaoPadrao() throws Exception {
        assertEquals(200, mvc.perform(get("/setores")).andReturn().getResponse().getStatus());
        var captor = ArgumentCaptor.forClass(Pageable.class);
        verify(service).listar(isNull(), captor.capture());
        assertEquals(0, captor.getValue().getPageNumber());
        assertEquals(10, captor.getValue().getPageSize());
        assertEquals(Sort.by("nome"), captor.getValue().getSort());
    }

    @Test
    void limitaTamanhoPreservandoPaginaEOrdenacaoMultipla() throws Exception {
        assertEquals(200, mvc.perform(get("/setores").param("page", "2").param("size", "200")
                .param("sort", "sigla,desc", "nome,asc")).andReturn().getResponse().getStatus());
        var captor = ArgumentCaptor.forClass(Pageable.class);
        verify(service).listar(isNull(), captor.capture());
        assertEquals(2, captor.getValue().getPageNumber());
        assertEquals(50, captor.getValue().getPageSize());
        assertEquals(Sort.by(Sort.Order.desc("sigla"), Sort.Order.asc("nome")), captor.getValue().getSort());
    }
}
