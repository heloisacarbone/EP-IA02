import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;

public class AlgoritmoGenetico {

	/**
	 * Armazena a populacao total
	 */
	private ArrayList<Cromossomo> populacao = new ArrayList<Cromossomo>();

	/**
	 * Numero de geracoes esperada
	 */
	private int geracoesMaximas;

	/**
	 * Numero de individuos que devem ter em uma populacao final
	 */
	private int numIndividuos;

	/**
	 * Mapa das coordenadas de cada cidade
	 */
	private HashMap<Integer, double[]> mapaCidades = new HashMap<Integer, double[]>();

	/**
	 * Taxa de mutacao
	 */
	private double taxaMutacao;

	/**
	 * Taxa de crossover
	 */
	private double taxaCrossover;

	/**
	 * operadores que podem ser aplicados
	 */
	private ArrayList<String> operadores;

	/**
	 * Construtor da classe
	 * @param cromossomos
	 * @param mapaCidades
	 * @param geracoesMaximas
	 */
	public AlgoritmoGenetico(ArrayList<Cromossomo> cromossomos, HashMap<Integer, double[]> mapaCidades,
			int geracoesMaximas, int numIndividuos, double taxaMutacao, double taxaCrossover, ArrayList<String> operadores) {
		this.setPopulacao(cromossomos);
		this.setMapaCidades(mapaCidades);
		this.setGeracoesMaximas(geracoesMaximas);
		this.setNumIndividuos(numIndividuos);
		this.setTaxaCrossover(taxaCrossover);
		this.setTaxaMutacao(taxaMutacao);
		this.setOperadores(operadores);
	}

	/**
	 * Metodo chamado para iniciar um algoritmo genetico em cima de uma populacao.
	 *
	 * @param cromossomos Populacao que sera analisada.
	 * @param mapaCidades Lugar que se encontra cada cidade que compoe as distancias nos cromossomos da populacao em um mapa.
	 */
	public void iniciaAlgoritmoGenetico() {

		try {
			String nomeArq = "src/arquivos/evolucao_g" + geracoesMaximas+"_pop"+ this.getPopulacao().size()+"_op"+ this.getOperadores().size() +".csv";
			File file = new File(nomeArq);

			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}

			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write("geracao;fi;distancia\n");
			int t = 0;
			System.out.println("Gerações: " + geracoesMaximas);
			ArrayList<Cromossomo> geracaoFinal = selecionaNovaPopulacao(this.getPopulacao(), this.mapaCidades);

			while (t < this.geracoesMaximas) {
				System.out.println("Geraçao: " + t + " Populacao: " + geracaoFinal.size());
				geracaoFinal = selecionaNovaPopulacao(geracaoFinal, this.mapaCidades);
				Cromossomo vencedor = Selecao.melhorIndividuo(geracaoFinal);
				bw.write(t + ";" + vencedor.getFi() + ";" + vencedor.getDistancia() + ";");
				bw.write("\n");

				t++;
			}

			// Gera arquivo com populacao completa
			String arqPop = "src/arquivos/populacao_final_g" + geracoesMaximas+"_pop"+ this.getPopulacao().size()+"_op"+ this.getOperadores().size() +".csv";
			File filePop = new File(arqPop);

			// if file doesnt exists, then create it
			if (!filePop.exists()) {
				filePop.createNewFile();
			}

			FileWriter fwpop = new FileWriter(filePop.getAbsoluteFile());
			BufferedWriter bwpop = new BufferedWriter(fwpop);
			bwpop.write("n;fi;distancia\n");


			for (int j = 1; j <= numIndividuos; j++) {
				Cromossomo vencedor = Selecao.melhorIndividuo(geracaoFinal);
				System.out.println("Melhor Cromossomo "+ j);
				bwpop.write(j + ";" + vencedor.getFi() + ";" + vencedor.getDistancia() + "\n");
				System.out.println();
				System.out.println("fi: "+ vencedor.getFi() + " distancia: " + vencedor.getDistancia());
				geracaoFinal.remove(vencedor);
			}
			System.out.println("FIM GA");
			bwpop.close();
			bw.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Seleciona uma populacao nova a partir de uma populacao gerada por subpopulacoes que sofreram interferencias
	 * @param populacao
	 * @param mapaCidades
	 * @return
	 */
	private ArrayList<Cromossomo> selecionaNovaPopulacao(ArrayList<Cromossomo> populacao, HashMap<Integer, double[]> mapaCidades){
		/*Gera populacao com subpopulacoes*/
		ArrayList<Cromossomo> subPopulacao= selecionarSubpopulacao(populacao, mapaCidades);

		// Aplica roleta russa em cima dos melhores, porem escolhe o melhor de todos de qualquer forma
		subPopulacao = Selecao.selecaoRoletaRussaMelhor(subPopulacao, this.numIndividuos);

		return subPopulacao;
	}

	/**
	 * Seleciona subpopulacao de um conjunto de cromossomos
	 *
	 * @param cromossomos
	 * @return
	 */
	private ArrayList<Cromossomo> selecionarSubpopulacao(ArrayList<Cromossomo> populacao, HashMap<Integer, double[]> mapaCidades) {

		ArrayList<Cromossomo> novaPopulacao = new ArrayList<Cromossomo>();
		novaPopulacao.addAll(populacao);
		for (int i = 0; i < this.getOperadores().size(); i++) {
			switch (this.getOperadores().get(i)) {
			case "crossover":
				ArrayList<Cromossomo> subpopulacaoCrossovers = Crossover.selecaoCrossover(populacao, mapaCidades, this.getTaxaCrossover());
				novaPopulacao.addAll(subpopulacaoCrossovers);
				break;
			case "mutacao":
				ArrayList<Cromossomo> subpopulacaoMutacoes = Mutacao.selecaoMutacao(populacao, mapaCidades, this.getTaxaMutacao());
				novaPopulacao.addAll(subpopulacaoMutacoes);
				break;
			case "crosmut":
				ArrayList<Cromossomo> crossover = Crossover.selecaoCrossover(populacao, mapaCidades, this.getTaxaCrossover());
				ArrayList<Cromossomo> crosmut = Mutacao.selecaoMutacao(crossover, mapaCidades, this.getTaxaMutacao());
				novaPopulacao.addAll(crosmut);
				break;
			}

		}

		ArrayList<Cromossomo> subpopulacaoTorneio = Selecao.selecaoTorneio(novaPopulacao);

		return subpopulacaoTorneio;
	}


	/*GETTERS E SETTERS*/

	public ArrayList<Cromossomo> getPopulacao() {
		return populacao;
	}

	public void setPopulacao(ArrayList<Cromossomo> populacao) {
		this.populacao = populacao;
	}

	public int getGeracoesMaximas() {
		return geracoesMaximas;
	}

	public void setGeracoesMaximas(int geracoesMaximas) {
		this.geracoesMaximas = geracoesMaximas;
	}

	public HashMap<Integer, double[]> getMapaCidades() {
		return mapaCidades;
	}

	public void setMapaCidades(HashMap<Integer, double[]> mapaCidades) {
		this.mapaCidades = mapaCidades;
	}

	public int getNumIndividuos() {
		return numIndividuos;
	}

	public void setNumIndividuos(int numIndividuos) {
		this.numIndividuos = numIndividuos;
	}

	public double getTaxaMutacao() {
		return taxaMutacao;
	}

	public void setTaxaMutacao(double taxaMutacao) {
		this.taxaMutacao = taxaMutacao;
	}

	public double getTaxaCrossover() {
		return taxaCrossover;
	}

	public void setTaxaCrossover(double taxaCrossover) {
		this.taxaCrossover = taxaCrossover;
	}

	public ArrayList<String> getOperadores() {
		return operadores;
	}

	public void setOperadores(ArrayList<String> operadores) {
		this.operadores = operadores;
	}
}
