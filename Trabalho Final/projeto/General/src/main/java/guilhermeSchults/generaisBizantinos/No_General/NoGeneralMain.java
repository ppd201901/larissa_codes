package guilhermeSchults.generaisBizantinos.No_General;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.xml.bind.DatatypeConverter;

public class NoGeneralMain
{

    private static final String NOME_NOS_ARQUIVO_PROPRIEDADE = "endereco_nos" + File.separator + "endereco_Nos.propriedades";
    private static final int NUMERO_DE_TENENTES = 4; 
    private final KeyFactory keyFactory;

    private List<EnderecoTenente> enderecoDosTenentes = new ArrayList<EnderecoTenente>();
    private PrivateKey privateKey = null;
    private EstadoHonestidade estadoHonestidade = EstadoHonestidade.HONESTO;   //Por padrao o estadoHonestidade e honesto
    private Scanner scanner = new Scanner(System.in);
    
    public NoGeneralMain(String nomeChavePrivada) throws FileNotFoundException, IOException, InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException 
    {

        if (nomeChavePrivada == null || nomeChavePrivada.isEmpty()) 
        {
            throw new IllegalArgumentException("Chave privada invalida");
        }

        FileInputStream privateKeyFile = new FileInputStream(nomeChavePrivada);
        byte[] privateKeyBytes = new byte[privateKeyFile.available()];
        privateKeyFile.read(privateKeyBytes);
        privateKeyFile.close();

        keyFactory = KeyFactory.getInstance("DSA");

        this.privateKey = this.keyFactory.generatePrivate(new PKCS8EncodedKeySpec(privateKeyBytes));
    }

    public static void main(String[] args) throws Exception 
    {
       
        if (args.length < 1) 
        {
            System.out.println("Erro: Chave privada nao localizada. Verifique se a mesma foi informada como argumento na linha de comando "
            		+ "da seguinte forma:'[nome-da-chave-privada] [nome-no:{general} [honestidade-do-no:{honesto,traidor}]' ");
            System.exit(1);
        }
       
        NoGeneralMain noGeneral = new NoGeneralMain(args[0]);  // importante: o argumento 'args[0]' e a chave privada do general
        noGeneral.init();
        noGeneral.run();
    }

    public void init() throws Exception 
    {
        carregarUrlNos();
    }

    private void carregarUrlNos() throws Exception 
    {

        // Os nós tenentes são cadastros previamente no arquivo endereco_Nos.propriedades 
    	// onde temos a url e a porta de cada No.
    	
        Properties propriedadesUrls = new Properties();
        InputStream in = NoGeneralMain.class.getClassLoader().getResourceAsStream(NOME_NOS_ARQUIVO_PROPRIEDADE);
        propriedadesUrls.load(in);

        String tenente;
        String url;
        String porta;
     
        for (int i = 1; i <= NUMERO_DE_TENENTES; i++)
        {

            tenente = propriedadesUrls.getProperty("tenente" + i);          
            url = tenente.split(":")[0];
            porta = tenente.split(":")[1];

            this.enderecoDosTenentes.add(new EnderecoTenente(url, porta, "tenente" + i)); //Adicionado nome do tenente na verificacao dos nos online/offline
        }

    }
    
    private void run() throws Exception
    {
        // Carrega o nó General e deixa-o online
    	// E aguarda comando do usuario

        String opcao = "7";
        do
        {   
            			  System.out.println("|----------------------------------|");
                          System.out.println("|==== Nome: No General.            |");
                          System.out.println("|==== Status: Online.              |");
            System.out.println(String.format("|==== Status Honestidade: %s. |",estadoHonestidade));
            			  System.out.println("|----------------------------------|\n");

            System.out.println("*******Selecione uma opcao:************************\n");
            System.out.println("(1) - Testar conectividade dos Nos (online/offline)");
            System.out.println("(2) - Enviar comando: Atacar");
            System.out.println("(3) - Enviar comando: Recuar");
            System.out.println("(4) - Alterar estado do General para: Honesto");
            System.out.println("(5) - Alterar estado do General para: Traidor");
            System.out.println("(6) - Imprimir Nome e Localizacao dos Nos Tenentes - Thread com Exclusao Mutua");
            System.out.println("(7) - Finalizar");
                    System.out.print("Opcao: ");       
                    opcao = scanner.nextLine();
              
            if (opcao.equals("1"))
            {
                verificarEstadoDosNos();
                pressioneEnterParaContinuar();
                
            } else if (opcao.equals("2")) 
            {
                enviarOrdem("atacar");
                pressioneEnterParaContinuar();
                
            } else if (opcao.equals("3")) 
            {
                enviarOrdem("recuar");
                pressioneEnterParaContinuar();
                
            } else if (opcao.equals("4"))
            {
                this.estadoHonestidade = EstadoHonestidade.HONESTO;           
                
            } else if (opcao.equals("5")) 
            {
                this.estadoHonestidade = EstadoHonestidade.TRAIDOR;            
                
            } else if(opcao.equals("6"))
            {
                System.out.print("\n");
            	ExecutaVerificacaoNomesTenentes.main(null); 
                pressioneEnterParaContinuar();
          	
            } else if (opcao.equals("7")) 
            {
                System.out.print("\nPrograma finalizado.");
                pressioneEnterParaContinuar();
                
            } else 
            {
                System.out.println(String.format("Opcao '%s' nao existe.", opcao));
                System.out.println("Por favor escolha uma opcao entre 1 - 7.\n");
            }

            System.out.println();

        } while (!opcao.equals("7"));

    }
    
    private void pressioneEnterParaContinuar()
    { 
           System.out.println("\nPressione Enter para continuar...");
           try
           {
               System.in.read();
           }  
           catch(Exception e)
           {
           
           }  
    }
    
    
    private void verificarEstadoDosNos() throws IOException 
    {
      
        System.out.println();

        Socket testeSocket = null;
        InputStream inputStream = null;
        OutputStream outputStream = null;
        Exception erro = null;
        boolean sucessoTeste = false;
        
     	System.out.println("Status dos Nos Tenentes:\n");

        for (EnderecoTenente enderecoTenente : enderecoDosTenentes) 
        {

            try 
            {
                testeSocket = new Socket(enderecoTenente.getUrl(), enderecoTenente.getPorta());
                testeSocket.setSoTimeout(8000); // definimos timeout de 8 segundos pra não bloquear indefinidamente

                outputStream = testeSocket.getOutputStream();
                inputStream = testeSocket.getInputStream();

                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                PrintWriter writer = new PrintWriter(outputStream);

                writer.println("test");
                writer.flush();                         
               
                String linha = reader.readLine();
             
                if (linha.equals("ack"))
                {
                    System.out.printf("%s esta online.\n", enderecoTenente);
                    sucessoTeste = true;
                }

            } catch (Exception e) 
            {

                erro = e;

            } finally 
            {

                if (!sucessoTeste)
                {
                    if (erro != null) 
                    {
                        System.out.printf("%s esta offline. Problema identificado: [%s]\n", enderecoTenente, erro.getMessage());
                    } else 
                    {
                        System.out.printf("%s esta offline.\n", enderecoTenente);
                    }
                }

                sucessoTeste = false;
                erro = null;

                if (testeSocket != null) 
                {
                    testeSocket.close();
                }

            }

        }

    }

    private void enviarOrdem(String ordem) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, SignatureException, IOException 
    {

        if (this.estadoHonestidade == EstadoHonestidade.HONESTO)
        {
            // Gera a mensagem para a ordem correta
            enviarMensagemNoHonesto(ordem);

        } else if (this.estadoHonestidade == EstadoHonestidade.TRAIDOR) 
        {
            // Gera duas mensagens: uma pra ordem de atacar e outra pra ordem de recuar
            // E manda essas assinaturas alternadamente para cada nó.
            enviarMensagemNoDesonesto(ordem);

        }

    }

    private void enviarMensagemNoHonesto(String ordem) throws IOException, SignatureException, InvalidKeyException, NoSuchAlgorithmException 
    {

        // Assina a mensagem antes de envia-la
        String mensagem = novaMensagemAssinada(ordem);

        System.out.println("\nEnviando mensagem: " + mensagem);

        Socket socket = null;
        OutputStream outputStream = null;
        Exception erro = null;
        boolean sucessoEnvioMsg = false;
        
        for (EnderecoTenente enderecoTenente : enderecoDosTenentes)
        {

            try 
            {
                socket = new Socket(enderecoTenente.getUrl(), enderecoTenente.getPorta());
                socket.setSoTimeout(8000); // timeout de 8 segundos pra não bloquear indefinidamente

                outputStream = socket.getOutputStream();

                PrintWriter writer = new PrintWriter(outputStream);

                // enviando mensagem
                writer.println(mensagem);
                writer.flush();

                sucessoEnvioMsg = true;

            } catch (Exception e) 
            {
                erro = e;

            } finally 
            {

                if (!sucessoEnvioMsg) 
                {
                    if (erro != null) 
                    {
                        System.out.printf("%s esta offline. Problema identificado: [%s]\n", enderecoTenente, erro.getMessage());
                    } else 
                    {
                        System.out.printf("%s esta offline.\n", enderecoTenente);
                    }
                }

                sucessoEnvioMsg = false;
                erro = null;

                if (socket != null)
                {
                    socket.close();
                }

            }

        }

    }

    private void enviarMensagemNoDesonesto(String ordem) throws IOException, SignatureException, InvalidKeyException, NoSuchAlgorithmException 
    {

        // Neste caso temos um No General traidor, desta maneira
    	// geramos duas mensagems: de atacar e de recuar. 
    	// Entao enviamos aleatóriamente para os nós Tenentes
        
        // Cria a mensagem de atacar e assina ela
        String mensagemAtacar = novaMensagemAssinada("atacar");

        // Cria a mensagem de recuar e assina ela
        String mensagemRecuar = novaMensagemAssinada("recuar");

        System.out.println("\nEnviando mensagem: " + mensagemAtacar);
        System.out.println("Enviando mensagem: " + mensagemRecuar + "\n");

        Socket socket = null;
        OutputStream outputStream = null;
        Exception erro = null;
        boolean msgEnviadaSucesso = false;
        
        for (int i = 0; i < enderecoDosTenentes.size(); i++) 
        {

            EnderecoTenente enderecoDoTenente = enderecoDosTenentes.get(i);
            
            try 
            {
                socket = new Socket(enderecoDoTenente.getUrl(), enderecoDoTenente.getPorta());
                socket.setSoTimeout(8000); // timeout de 8 segundos pra não bloquear indefinidamente

                outputStream = socket.getOutputStream();

                PrintWriter writer = new PrintWriter(outputStream);

                // agora vamos enviar as mensagens, alternando entre atacar e recuar
                if (i % 2 == 0) 
                {
                    writer.println(mensagemAtacar);                
                } else 
                {
                    writer.println(mensagemRecuar);                      
                }

                writer.flush();

                msgEnviadaSucesso = true;

            } catch (Exception e) 
            {

                erro = e;

            } finally 
            {

                if (!msgEnviadaSucesso) 
                {
                    if (erro != null) 
                    {
                        System.out.printf("%s esta offline. Problema identificado: [%s]\n", enderecoDoTenente, erro.getMessage());
                    } else 
                    {
                        System.out.printf("%s esta offline.\n", enderecoDoTenente);
                    }
                }

                msgEnviadaSucesso = false;
                erro = null;

                if (socket != null)
                {
                    socket.close();
                }
            }
        }

    }
    
    // Metodo que cria e assina a mensagem. A assinatura tem um caráter aleatório. Para cada
    // assinatura um novo 'hash' é gerado, mesmo se for com a mesma entrada. Isso é essencial
    // para o algoritmo, uma vez que preserva a propriedade de dificuldade de criação e reprodução
    // de mensagens falsas.
    
    private String novaMensagemAssinada(String ordem) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException 
    {      
        Signature assinaturaAuxiliar = Signature.getInstance("DSA");
        assinaturaAuxiliar.initSign(this.privateKey);

        assinaturaAuxiliar.update(ordem.getBytes());
        byte[] assinatura = assinaturaAuxiliar.sign();

        // passa pra Base64 pra concatenar ao final da própria mensagem como texto.
        String signatureBase64 = DatatypeConverter.printBase64Binary(assinatura);

        String mensagem = String.format("%s:general:%s", ordem, signatureBase64);

        return mensagem;

    }
    
    static class VerificaNomes implements Tarefa
    {

    	//Caso nossas Threads (NomeTenentes) tentem acessar esse metodo sincronizado no mesmo objeto "objetoCompartilhadoNomes"
    	//somente uma thread por vez sera concedido para executa-lo
    	
        private Lock bloqueioRegiaoCritica = new ReentrantLock();
       
  	
        // Caso queira utilizar o synchronized do java
        
		public synchronized void verificaNomeTenentes() throws IOException
	    {			            	  	                            
	    	 for (int i = 1; i <= NUMERO_DE_TENENTES; i++)
	         {	               	                 	                 	            
	             try
	             { 
	                 Thread.sleep(1000); 
	                 System.out.println("Nome: tenente" + i);
	             } 
	             catch (Exception erro) 
	             { 
	            	 System.out.println("Thread interrompida.");	                
	             } 	                                  
	         }	  
	    	 System.out.println("\n");
	     }
		
        // Caso queira utilizar o ReentrantLock do java.
		// Implementa a interface Lock e prove sincronizacao ao metodo enquanto as threads 
		// realizam o acesso ao recurso compartilhado
		
		public void verificaNomeTenentes2()
	    {		
			 double temperaturaCidade = 0;
			 String url;
		     String tenente = null;
		     Properties urlsPropriedades = new Properties();
		     InputStream in = NoGeneralMain.class.getClassLoader().getResourceAsStream(NOME_NOS_ARQUIVO_PROPRIEDADE);
		    		 		     
		     try 
		     {
				urlsPropriedades.load(in);
			 }catch (IOException e) 
		     {
				e.printStackTrace();
			 }
		     
			 bloqueioRegiaoCritica.lock();
			
		try {
				System.out.println(Thread.currentThread().getName() + ": Bloqueio adquirido.");
				System.out.println("Acessando regiao critica. Imprimindo nome e localizacao dos Tenentes.");
				
				for (int i = 1; i <= NUMERO_DE_TENENTES; i++)
				 {
			           tenente = urlsPropriedades.getProperty("tenente" + i);
				       url = tenente.split(":")[0];
				     try
				     { 		
		                 temperaturaCidade = VerificaTemperatura.getTemperatura();
		                 System.out.println("Nome: tenente" + i + " - Endereco: " +url+ " - Temperatura da Cidade da Invasao: " +temperaturaCidade+ "");
		                 Thread.sleep(1000); 
				     } 
				     catch (InterruptedException erro) 
				     { 
				    	 System.out.println("Thread interrompida." + erro.getMessage());	                
				     } 	                                  
				 }	  							
			}finally
			{
		    	 System.out.println(Thread.currentThread().getName() + ": Desbloqueio realizado.\n");
				 bloqueioRegiaoCritica.unlock();
			}
	     }
    }
    
    static class VerificaTemperatura
    { 	
    	public static double getTemperatura()
    	{
    		
    	String cidade = "Goiania";
    	String siglaEstado = "GO";
    	
    	return NetClientGet.GetTemperatura(cidade, siglaEstado);   	
    	}
    }
  /*  
   static class NomeTenentes extends Thread
    {
    	VerificaNomes verificaNomes;
        	
    	NomeTenentes(VerificaNomes nomes)
    	{
    		this.verificaNomes = nomes;
    	}
    	
    	@Override
    	public void run()
    	{   	 		  		
    		verificaNomes.verificaNomeTenentes2();   		
    	}      
    }
   */
    
	  public interface Tarefa 
	  {
		  public void verificaNomeTenentes2();
	  }
	  
	  static class Trabalhador implements Runnable 
	  {
		  private Tarefa tarefa;
		  
		  public Trabalhador(Tarefa tarefa)
		  {
			  this.tarefa = tarefa;
		  }
	
		public void run() 
		{
			tarefa.verificaNomeTenentes2();
		}
	  }
   
    
    static class ExecutaVerificacaoNomesTenentes
    {
    	public static void main(String[] args) throws Exception
    	{
    		     		
	    	final int contadorThread = 4;
	    	final ExecutorService executorServico = Executors.newFixedThreadPool(contadorThread);
	    	final Tarefa tarefa = new VerificaNomes();
	    	    	
	    	for(int i = 0; i < contadorThread; i++)
	    	{
	    		executorServico.execute(new Trabalhador(tarefa));
	    	}   		    	
	    	executorServico.shutdown();	
    		
    		/*
    		
    		Para o metodo 1 da sincronizacao:
    		
    		VerificaNomes objetoCompartilhadoNomes = new VerificaNomes();
    		
    		//Aqui vamos criar 4 threads que compartilham o mesmo objeto "objetoCompartilhadoNomes"
    	  		
    		NomeTenentes tenente1 = new NomeTenentes(objetoCompartilhadoNomes);
    		NomeTenentes tenente2 = new NomeTenentes(objetoCompartilhadoNomes);
    		NomeTenentes tenente3 = new NomeTenentes(objetoCompartilhadoNomes);
    		NomeTenentes tenente4 = new NomeTenentes(objetoCompartilhadoNomes);
    		 
	    	tenente1.start();
	    	tenente2.start();
	    	tenente3.start();  	
	    	tenente4.start();	
	    	
	    	*/
    	       	
    	}
    }

}
