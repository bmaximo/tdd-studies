package br.sp.bmaximo.servicos;

import static br.sp.bmaximo.utils.DataUtils.isMesmaData;
import static br.sp.bmaximo.utils.DataUtils.obterDataComDiferencaDias;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.junit.rules.ExpectedException;

import br.sp.bmaximo.entidades.Filme;
import br.sp.bmaximo.entidades.Locacao;
import br.sp.bmaximo.entidades.Usuario;
import br.sp.bmaximo.exceptions.FilmeSemEstoqueException;
import br.sp.bmaximo.exceptions.LocadoraException;
import br.sp.bmaximo.servicos.LocacaoService;
import br.sp.bmaximo.utils.DataUtils;


public class LocacaoServiceTest {
	
	private LocacaoService service;
	private List<Filme> filmes;
	
	@Rule
	public ErrorCollector error = new ErrorCollector();
	
	@Rule
	public ExpectedException exception = ExpectedException.none();
	
	@Before
	public void setup() {
		service = new LocacaoService();
		filmes = new ArrayList<>();
	}
	
	@Test
	public void deveAlugarFilme() throws Exception{
		//cenario
		Usuario usuario = new Usuario("Barbara");
		filmes = Arrays.asList(new Filme("Teste filme", 1, 5.00));
		
		//acao		
		Locacao locacao = service.alugarFilme(usuario, filmes);
		 
		//verificacao
		error.checkThat(locacao.getValor(), is(equalTo(5.0)));
		error.checkThat(locacao.getValor(), is(not(6.0)));
		error.checkThat(isMesmaData(locacao.getDataLocacao(), new Date()), is(true));
		error.checkThat(isMesmaData(locacao.getDataRetorno(), obterDataComDiferencaDias(1)), is(true));

	}
	
	//Elegante
	@Test(expected = FilmeSemEstoqueException.class)
	public void naoDeveAlugarFilmeSemEstoque() throws Exception {
		Usuario usuario = new Usuario("Barbara");
		filmes = Arrays.asList(new Filme("Teste filme", 0, 5.00), new Filme("Teste filme", 2, 5.00));
		
		service.alugarFilme(usuario, filmes);
	}
	
	//Robusta
	@Test
	public void naoDeveAlugarFilmeSemUsuario() throws FilmeSemEstoqueException {
		//Cenario
		filmes = Arrays.asList(new Filme("Teste filme", 1, 5.00));
		
		//acao		
		try {
			service.alugarFilme(null, filmes);
			Assert.fail();
		} catch (LocadoraException e) {
			assertThat(e.getMessage(), is("Usuario vazio"));
		}
	}
	
	//Nova
	@Test
	public void naoDeveAlugarFilmeSemFilme() throws FilmeSemEstoqueException, LocadoraException {
		//Cenario		
		Usuario usuario = new Usuario("Usuario 1");
		
		exception.expect(LocadoraException.class);
		exception.expectMessage("Filme vazio");
		
		//acao
		service.alugarFilme(usuario, filmes);
	}
	
	@Test
	public void devePagar75PctNoFilme3() throws FilmeSemEstoqueException, LocadoraException {
		//Cenario		
		Usuario usuario = new Usuario("Usuario 1");
		filmes = Arrays.asList(new Filme("Filme 1", 2, 4.00), new Filme("Filme 2", 2, 4.00), new Filme("Filme 2", 2, 4.00));
		
		//acao
		Locacao resultado = service.alugarFilme(usuario, filmes);
		
		//verificacao - 4+4+3 = 11
		assertThat(resultado.getValor(), is(11.0));
	}
	
	@Test
	public void devePagar50PctNoFilme4() throws FilmeSemEstoqueException, LocadoraException {
		//Cenario		
		Usuario usuario = new Usuario("Usuario 1");
		filmes = Arrays.asList(
				new Filme("Filme 1", 2, 4.00), 
				new Filme("Filme 2", 2, 4.00), 
				new Filme("Filme 3", 2, 4.00),
				new Filme("Filme 4", 2, 4.00));
		
		//acao
		Locacao resultado = service.alugarFilme(usuario, filmes);
		
		//verificacao - 4+4+3+2 = 13
		assertThat(resultado.getValor(), is(13.0));
	}

	@Test
	public void devePagar25PctNoFilme5() throws FilmeSemEstoqueException, LocadoraException {
		//Cenario		
		Usuario usuario = new Usuario("Usuario 1");
		filmes = Arrays.asList(
				new Filme("Filme 1", 2, 4.00), 
				new Filme("Filme 2", 2, 4.00), 
				new Filme("Filme 3", 2, 4.00),
				new Filme("Filme 4", 2, 4.00),
				new Filme("Filme 5", 2, 4.00));
		
		//acao
		Locacao resultado = service.alugarFilme(usuario, filmes);
		
		//verificacao - 4+4+3+2+1 = 14
		assertThat(resultado.getValor(), is(14.0));
	}
	
	@Test
	public void devePagar0NoFilme5() throws FilmeSemEstoqueException, LocadoraException {
		//Cenario		
		Usuario usuario = new Usuario("Usuario 1");
		filmes = Arrays.asList(
				new Filme("Filme 1", 2, 4.00), 
				new Filme("Filme 2", 2, 4.00), 
				new Filme("Filme 3", 2, 4.00),
				new Filme("Filme 4", 2, 4.00),
				new Filme("Filme 5", 2, 4.00),
				new Filme("Filme 6", 2, 4.00));
		
		//acao
		Locacao resultado = service.alugarFilme(usuario, filmes);
		
		//verificacao - 4+4+3+2+1 = 14
		assertThat(resultado.getValor(), is(14.0));
	}
	
	@Test 
	public void naoDeveDevolverFilmeDomingo() throws FilmeSemEstoqueException, LocadoraException {
		//Cenario		
		Usuario usuario = new Usuario("Usuario 1");
		filmes = Arrays.asList(new Filme("Filme 1", 2, 4.00));
		
		//acao
		Locacao retorno = service.alugarFilme(usuario, filmes);
		
		//verificacao
		boolean ehSegunda = DataUtils.verificarDiaSemana(retorno.getDataRetorno(), Calendar.MONDAY);
		Assert.assertTrue(ehSegunda);
	}
}
