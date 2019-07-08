package guilhermeSchults.generaisBizantinos.No_Tenente;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import javax.xml.bind.DatatypeConverter;
import org.apache.commons.io.IOUtils;

public class NoEnderecoTenenteMain 
{

   private static final String NOME_NOS_ARQUIVO_PROPRIEDADE = "endereco_nos" + File.separator + "endereco_Nos.propriedades";
   private static final int NUMERO_DE_TENENTES = 4;

   private Map<String, PublicKey> chavesPublicas = new HashMap<String, PublicKey>();
   private Map<String, OrdemAssinatura> tabelaDeOrdens = new HashMap<String, OrdemAssinatura>();
   
   private List<EnderecoTenente> enderecoDosTenentes = new ArrayList<EnderecoTenente>();
   private List<String> nosDesonestos = new ArrayList<String>();

   private PrivateKey chavePrivada = null;

   private EstadoHonestidade estadoHonestidade = EstadoHonestidade.HONESTO;

   private ServerSocket serverSocket;

   private String nomeNO;

   private int numeroPorta;

   private List<EnderecoTenente> outrosTenentes = new ArrayList<EnderecoTenente>(); 
   // Demais Tenentes precisam trocar mensagem entre si durante a execucao do programa.
   // A ideia é que este 'outrosTenentes' possa ser extendido para uma lista de 'outrosTenentes' em um cenario com mais Tenentes.

   public NoEnderecoTenenteMain(String nomeChavePrivada, String nomeNo, String estadoHonestidade) throws FileNotFoundException, IOException, NoSuchAlgorithmException, InvalidKeySpecException
   {
       
	   this.nomeNO = nomeNo;
	   
       FileInputStream arquivoChavePrivada = new FileInputStream(nomeChavePrivada);
       byte[] chavePrivadaBytes = new byte[arquivoChavePrivada.available()];
       arquivoChavePrivada.read(chavePrivadaBytes);
       arquivoChavePrivada.close();

       KeyFactory keyFactory = KeyFactory.getInstance("DSA");

       this.chavePrivada = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(chavePrivadaBytes));

       if (nomeChavePrivada == null || nomeChavePrivada.isEmpty()) 
       {
           throw new IllegalArgumentException("Chave privada invalida.");  
       }

       if (nomeNo == null || nomeNo.isEmpty()) 
       {
           throw new IllegalArgumentException("Nome do No invalido.");
       }

       estadoHonestidade = estadoHonestidade.toLowerCase();
       if (estadoHonestidade == null || estadoHonestidade.isEmpty() || (!estadoHonestidade.equals("honesto") && !estadoHonestidade.equals("traidor")))
       {
           throw new IllegalArgumentException("Argumento de honestidade invalido. Informe se o No e 'honesto' ou 'traidor'");
       }

       if (estadoHonestidade.equals("honesto"))
       {
           this.estadoHonestidade = EstadoHonestidade.HONESTO;
       } else 
       {
           this.estadoHonestidade = EstadoHonestidade.TRAIDOR;
       }
   }

   public void inicioExecucao() throws Exception 
   {
       carregarUrlNos(); // verifica os Nos tenentes cadastros no arquivo propriedade
       criarTabelaDeOrdens(); //adiciona as ordens em uma tabela (map) para posterior encaminhamento delas.
       carregarChavesPublicas(); //sobe as chaves publicas para cada No
       serverSocket = new ServerSocket(numeroPorta); // cria conexao por socket para os Nos 
   }

   private void carregarUrlNos() throws Exception
   {

       Properties urlsPropriedades = new Properties();
       InputStream in = NoEnderecoTenenteMain.class.getClassLoader().getResourceAsStream(NOME_NOS_ARQUIVO_PROPRIEDADE);
       urlsPropriedades.load(in);

       EnderecoTenente enderecoTenente;
       String tenente;
       String url;
       String porta;
       for (int i = 1; i <= NUMERO_DE_TENENTES; i++) 
       {

           tenente = urlsPropriedades.getProperty("tenente" + i);
           url = tenente.split(":")[0];
           porta = tenente.split(":")[1];

           enderecoTenente = new EnderecoTenente(url, porta, "tenente" + i);
           this.enderecoDosTenentes.add(enderecoTenente);

           if (nomeNO.equals("tenente" + i)) 
           {
               this.numeroPorta = enderecoTenente.getPorta();
           } else 
           {
               this.outrosTenentes.add(enderecoTenente); // Temos 4 nós tenentes cadastrados, mas pode ser expandido para mais nós.
           }

       }

   }

   private void criarTabelaDeOrdens() 
   {

       tabelaDeOrdens.put("general", null);

       for (EnderecoTenente enderecoTenente : enderecoDosTenentes)
       {
           tabelaDeOrdens.put(enderecoTenente.getnomeDoNo(), null);
       }
   }

   private void carregarChavesPublicas() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException 
   {

       InputStream generalChavePublicaStream = NoEnderecoTenenteMain.class
               .getClassLoader().getResourceAsStream("chaves_publicas" + File.separator + "general");
       InputStream tenente1ChavePublicaStream = NoEnderecoTenenteMain.class
               .getClassLoader().getResourceAsStream("chaves_publicas" + File.separator + "tenente1");
       InputStream tenente2ChavePublicaStream = NoEnderecoTenenteMain.class
               .getClassLoader().getResourceAsStream("chaves_publicas" + File.separator + "tenente2");
       InputStream tenente3ChavePublicaStream = NoEnderecoTenenteMain.class
               .getClassLoader().getResourceAsStream("chaves_publicas" + File.separator + "tenente3");
       InputStream tenente4ChavePublicaStream = NoEnderecoTenenteMain.class
               .getClassLoader().getResourceAsStream("chaves_publicas" + File.separator + "tenente4");

       byte[] generalChavePublicaBytes = IOUtils.toByteArray(generalChavePublicaStream);
       byte[] tenente1ChavePublicaBytes = IOUtils.toByteArray(tenente1ChavePublicaStream);
       byte[] tenente2ChavePublicaBytes = IOUtils.toByteArray(tenente2ChavePublicaStream);
       byte[] tenente3ChavePublicaBytes = IOUtils.toByteArray(tenente3ChavePublicaStream);
       byte[] tenente4ChavePublicaBytes = IOUtils.toByteArray(tenente4ChavePublicaStream);


       KeyFactory keyFactory = KeyFactory.getInstance("DSA");

       PublicKey generalChavePublica  = keyFactory.generatePublic(new X509EncodedKeySpec(generalChavePublicaBytes));
       PublicKey tenente1ChavePublica = keyFactory.generatePublic(new X509EncodedKeySpec(tenente1ChavePublicaBytes));
       PublicKey tenente2ChavePublica = keyFactory.generatePublic(new X509EncodedKeySpec(tenente2ChavePublicaBytes));
       PublicKey tenente3ChavePublica = keyFactory.generatePublic(new X509EncodedKeySpec(tenente3ChavePublicaBytes));
       PublicKey tenente4ChavePublica = keyFactory.generatePublic(new X509EncodedKeySpec(tenente4ChavePublicaBytes));


       chavesPublicas.put("general",  generalChavePublica);
       chavesPublicas.put("tenente1", tenente1ChavePublica);
       chavesPublicas.put("tenente2", tenente2ChavePublica);
       chavesPublicas.put("tenente3", tenente3ChavePublica);
       chavesPublicas.put("tenente4", tenente4ChavePublica);


   }

   @SuppressWarnings("resource")
private void run() throws IOException, NoSuchAlgorithmException, InvalidKeyException, SignatureException 
   {
       // Espera comando do general e dos outros Tenente.
	   // Após receber o comando do general repassa ordem para os outros Nós tenentes.

       int contadorAtacar;
       int contadorRecuar;
       
       while (true) 
       {
           contadorAtacar = 0;
           contadorRecuar = 0;
           	                
	              System.out.println("|--------------------------------------------------|");
	System.out.println(String.format("| Nome: %s.                                  |", nomeNO));                  
	System.out.println(String.format("| Status Honestidade: %s.                     |",estadoHonestidade));
    System.out.println(String.format("| Aguardando comando do General em localhost: %d.|", numeroPorta)); 
				  System.out.println("| Status: Online                                   |");
	              System.out.println("|--------------------------------------------------|");



           Socket socket = null;
           InputStream in = null;
           OutputStream out = null;
           
           try 
           {
               serverSocket.setSoTimeout(0);
               socket = serverSocket.accept();

               in = socket.getInputStream();
               out = socket.getOutputStream();

               PrintWriter writer = new PrintWriter(out);
               BufferedReader reader = new BufferedReader(new InputStreamReader(in));

               String mensagemRecebida = reader.readLine();

               // Se for uma mensagem de teste responde um ack pra sinalizar que este nó está online.
               if (mensagemRecebida.equals("test")) 
               {
                   writer.println("ack");
                   writer.flush();
                   
               } else if (mensagemRecebida.startsWith("atacar") || mensagemRecebida.startsWith("recuar")) 
               {
                   // Quando recebemos como entrada "atacar" ou "recuar" temos uma mensagem real 

                   // NUMERO_DE_TENENTES: Número máximo de mensagens que virão por essa sessão (corresponde ao número de nós tenentes na rede).
                   // Considerando que o General participa desta contagem, o No atual não e a mensagem de um dos Nós que acabou de ser recebida.
            	   
                   for (int i = 1; i <= NUMERO_DE_TENENTES; i++) 
                   {

                       socket.setSoTimeout(8000); // Timeout de 8 segundos para o recebimento das demais ordens após o recebimento da primeira ordem

                       if (mensagemRecebida.split(",").length > 1) 
                       {                     
                           // Essa mensagem é composta pois possui a mensagem do general
                           // mais o repasse dos outros tenentes. Essas duas ordens precisam ser iguais, caso
                           // contrário o tenente que repassou está mentindo.

                           System.out.println("\nOrdem recebida: " + mensagemRecebida + "\n");

                           String[] mensagens = mensagemRecebida.split(",");
                           
                           String ordemComum = null;
                          
                           System.out.println("Analisando assinatura da ordem original recebida do General e dos Tenentes que a repassaram pra frente\n"); 
                           System.out.println("------------------------------------------------------------------------------------------\n"); 
                           for (int j = mensagens.length - 1; j >= 0; j--)
                           {
                           // Percorre esse cadeia de ordens a partir da ordem mais "antiga" (que corresponde a primeira ordem do general)

                               String mensagem = mensagens[j];                              
                              
                               System.out.println("Verificando ordem recebida: " + mensagem);

                               String[] split = mensagem.split(":");
                               String ordemRecebida = split[0];
                               String remetenteOrdem = split[1];
                               String assinaturaBase64 = split[2];
                               OrdemAssinatura ordemAssinada = new OrdemAssinatura(ordemRecebida, assinaturaBase64);
                               
                               System.out.println("Remetente: "+split[1]+"\nAssinatura Digital: "+split[2]+"\n");
                               
                               // checa assinatura
                               boolean verificaAssinatura = verificarAssinatura(ordemRecebida, remetenteOrdem, assinaturaBase64);

                               if (verificaAssinatura == false) 
                               {                           
                                   
                                   throw new IllegalStateException("Temos um problema aqui: analisamos a assinatura recebida com a "
                                           + " assinatura local e as mesmas nao coincidem. A mensagem pode ter sido modificada. Abortando operacao");
                               }
                               
                               System.out.println("Assinatura e valida.\n"); 
                               
                               // verifica se essa cadeia de ordens recebidas contém todos a mesma ordem (atacar ou recuar), caso contrário,
                               // o Nó que tem a mensagem diferente dos demais, é desonesto
                               
                               if (ordemComum == null) 
                               {
                                   ordemComum = ordemRecebida;
                                   
                                   // confere se a ordem do Nó já foi registrada anteriormente e se confere com a ordem
                                   // que ele está mandando agora.
                                                                  
                                   if (tabelaDeOrdens.get(remetenteOrdem) != null) 
                                   {
                                       if (!tabelaDeOrdens.get(remetenteOrdem).equals(ordemAssinada))
                                       {
                                           // assinatura válida mas voto incosistente, esse nó é desonesto
                                           nosDesonestos.add(remetenteOrdem);
                                       }
                                   } else 
                                   {
                                       tabelaDeOrdens.put(remetenteOrdem, ordemAssinada);
                                   }

                               } else 
                               {
                                   if (ordemComum.equals(ordemRecebida)) 
                                   {
                                       // Ordem consistente, ou seja, assinatura digital confere

                                       if (tabelaDeOrdens.get(remetenteOrdem) != null) 
                                       {
                                           if (!tabelaDeOrdens.get(remetenteOrdem).equals(ordemAssinada)) 
                                           {
                                               // assinatura válida mas voto incosistente, esse nó é desonesto
                                               nosDesonestos.add(remetenteOrdem);
                                           }
                                       } else 
                                       {
                                           tabelaDeOrdens.put(remetenteOrdem, ordemAssinada);
                                       }

                                   } else 
                                   {
                                       // Ordem difere do restante, entao remetente é desonesto

                                       nosDesonestos.add(remetenteOrdem);

                                   }
                               }
                           }
                           System.out.println("Fim da Verificacao das assinaturas e das ordens recebidas.\n"); 
                           System.out.println("------------------------------------------------------------------------------------------\n"); 

                       } else
                       {
                           // general

                           System.out.println("\nOrdem Recebida: " + mensagemRecebida);
                           
                           String[] split = mensagemRecebida.split(":");
                           String ordemRecebida = split[0];
                           String remetenteOrdem = split[1];
                           String assinaturaBase64 = split[2];
                                                   
                           System.out.println("Remetente: "+split[1]+"\nAssinatura Digital: "+split[2]+"\n");
                     
                           OrdemAssinatura ordemAssinada = new OrdemAssinatura(ordemRecebida, assinaturaBase64);

                           if (!remetenteOrdem.equals("general")) 
                           {
                               throw new IllegalStateException("Ops, mensagem inconsistente, tem algo errado. Ordem recebida mas nao do general: " + mensagemRecebida);
                           }

                           boolean verificaAssinatura = verificarAssinatura(ordemRecebida, remetenteOrdem, assinaturaBase64);

                           if (verificaAssinatura == false) 
                           {
                        	   throw new IllegalStateException("Temos um problema aqui: analisamos a assinatura recebida com a "
                                       + " assinatura local e as mesmas nao coincidem. A mensagem pode ter sido modificada. Abortando operacao");
                           }
                           
                           System.out.println("Assinatura e valida.\n");
                           
                           // Registrar a ordem do general na tabela de votos, verificando antes se a ordem deste general já foi registrada anteriormente
                           // e se bate com esta.
                           
                           if (tabelaDeOrdens.get(remetenteOrdem) != null) 
                           {
                               if (!tabelaDeOrdens.get(remetenteOrdem).equals(ordemAssinada))
                               {                                  
                                   nosDesonestos.add(remetenteOrdem); // assinatura válida mas voto incosistente, esse nó é desonesto
                               }
                           } else 
                           {
                               tabelaDeOrdens.put(remetenteOrdem, ordemAssinada);
                           }
                           
                           enviarOrdemRecebidaParaOutrosTenentes(ordemRecebida, mensagemRecebida);
                       }

                       if (i != NUMERO_DE_TENENTES)
                       {

                           try 
                           {
                               serverSocket.setSoTimeout(8000); // Timeout de 8 segundos pra esperar as mensagens dos outros Tenentes.
                               socket = serverSocket.accept();

                               reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                               
                           } catch (SocketTimeoutException e) 
                           {
                               throw new SocketTimeoutException("Um ou mais Nos nao enviou sua mensagem. O(s) mesmo(s) pode estar offline");
                           } 
                          
                           mensagemRecebida = reader.readLine();

                       }
                   }

               }

           } catch (SocketTimeoutException e)
           {

               System.out.println("Tivemos um problema: " + e.getMessage());

           } finally 
           {

               if (socket != null) 
               {
                   socket.close();
               }
           }

           // Ao final do recebimento das mensagens, percorremos o map de registro de ordens.
           // Precisamos contar o número de votos e decidir qual é a ordem mais "sugerida" pelos Tenentes,
           // descartando as sugestões dos Nós desonestos.
           // Se o próprio general for desonesto mantemos a "decisao" padrão de 'recuar'
           
           if (nosDesonestos.contains("general"))
           {
               System.out.println("General e traidor.");
               System.out.println("Recuar!");
               
           } else 
           {

               System.out.println("\nVerificando informacoes recebidas. Agora vamos decidir o plano de acao.\nDecidindo plano de acao.....\n");
               
               for (Entry<String, OrdemAssinatura> entradaTabela : tabelaDeOrdens.entrySet())
               {

                   String entryNomeNo = entradaTabela.getKey();
                   OrdemAssinatura entryOrdem = entradaTabela.getValue();

                   if (entryOrdem != null && !nosDesonestos.contains(entryNomeNo)) 
                   {
                       if (entryOrdem.getOrdem().equals("atacar")) 
                       {
                           contadorAtacar++;
                           
                       } else if (entryOrdem.getOrdem().equals("recuar")) 
                       {
                           contadorRecuar++;
                       }
                   }

               }

               for (String nomeNoDesonesto : nosDesonestos) 
               {
                   System.out.println(nomeNoDesonesto + " e traidor.");
               }

               if (this.estadoHonestidade == EstadoHonestidade.TRAIDOR)
               {
                   System.out.println("Eu sou traidor, portanto, nao importa qual o plano de acao. Posso enviar uma ordem para atacar ou recuar\n");
                   
               } else 
               {
                   if (contadorAtacar > contadorRecuar) 
                   {
                       System.out.println("Plano: Atacar!\n");
                   } else 
                   {
                       System.out.println("Plano: Recuar!\n");
                   }
               }

               this.tabelaDeOrdens.clear();
               this.nosDesonestos.clear();

           }

       }

   }

   public static void main(String[] args) throws Exception
   {

       // Caso o usuario queira executar pela IDE ou terminal
	   // O mesmo passa a chave privada deste nó (nome e caminho do arquivo) pela linha de comando da IDE ou pelo terminal

       boolean argumentoInvalido = false;
       if (args.length < 1) 
       {
           System.out.println("Temos um problema: nao localizamos o argumento 1. "
           		+ "Esta faltando a chave privada.\nTente novamente.");

           argumentoInvalido = true;
       }

       if (args.length < 2) 
       {
           System.out.println("Identificamos outro problema: nao localizamos o argumento 2. "
           		+ "E preciso informar o nome do No.\nTente novamente.");

           argumentoInvalido = true;
       }

       if (args.length < 3) 
       {
           System.out.println("Identificamos outro problema: nao localizamos o argumento 3."
        		   +"E preciso informar se o No e 'honesto' ou 'traidor'.\nTente novamente.");
           
           argumentoInvalido = true;
       }

       if (argumentoInvalido) 
       {
    	   
           throw new IllegalArgumentException("Utilizacao pelo terminal: java -jar [caminho]/Tenentes-exec-[$version].jar [nome-da-chave-privada] "
                   + "[nome-no:{general,tenente1,tenente2,tenente3...}] [honestidade-do-no:{honesto,traidor}].\n"
                   + "Utilizacao pela linha de comando da IDE:\n"
                   + "[nome-da-chave-privada] [nome-no:{general,tenente1,tenente2,tenente3...}][honestidade-do-no:{honesto,traidor}]");
           
       }

       NoEnderecoTenenteMain generalNode = new NoEnderecoTenenteMain(args[0], args[1], args[2]);
       generalNode.inicioExecucao();
       generalNode.run();
   }

   private void enviarOrdemMensagem(String mensagem, List<EnderecoTenente> tenentesAlvos) throws IOException
   {

       Socket socket = null;

       for (EnderecoTenente tenentes : tenentesAlvos) 
       {
           try 
           {
               if (!tenentes.getnomeDoNo().equals(this.nomeNO)) 
               {
                   socket = new Socket(tenentes.getUrl(), tenentes.getPorta());

                   PrintWriter writer = new PrintWriter(socket.getOutputStream());

                   System.out.printf("Enviando ordem: %s para %s\n", mensagem, tenentes);
                   writer.println(mensagem);
                   writer.flush();

                   writer.close();
               }

           } catch (Exception e) 
           {

               System.out.printf("Erro ao enviar a ordem para: %s. Problema identificado: [%s]\n", tenentes, e.getMessage());

           } finally 
           {
               if (socket != null) 
               {
                   socket.close();
               }
           }

       }

   }

   private boolean verificarAssinatura(String ordemRecebida, String remetenteOrdem, String assinaturaBase64) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException 
   {

       byte[] bytesAssinaturaRecebida = DatatypeConverter.parseBase64Binary(assinaturaBase64);

       // Recupera a chave pública do Nó que enviou a mensagem
       
       PublicKey chavePublica = chavesPublicas.get(remetenteOrdem);
       if (chavePublica == null) 
       {
           throw new IllegalArgumentException("Remetente invalido: " + remetenteOrdem);
       }

       Signature assinaturaLocal = Signature.getInstance("DSA");

       assinaturaLocal.initVerify(chavePublica);
       assinaturaLocal.update(ordemRecebida.getBytes());

       // Verificamos se a assinatura digital coincide
       
       boolean verificaAssinatura = assinaturaLocal.verify(bytesAssinaturaRecebida);

       return verificaAssinatura;

   }

   public void enviarOrdemRecebidaParaOutrosTenentes(String ordem, String mensagemOriginal) throws InvalidKeyException, NoSuchAlgorithmException, SignatureException, IOException 
   {

       String ordemParaEnviar;
       
       // Se o Tenente e honesto vai enviar exatamente a ordem que recebeu do general para os outros Tenentes
       
       if (this.estadoHonestidade == EstadoHonestidade.HONESTO) 
       {
           ordemParaEnviar = ordem;

       } else 
       {
       // Caso contrário vai enviar o oposto da ordem que recebeu do General para os outros Tenentes
    	   
           ordemParaEnviar = (ordem.equals("atacar")) ? "recuar" : "atacar";
       }

       // Assina a mensagem
       
       Signature assinaturaAuxiliar = Signature.getInstance("DSA");
       assinaturaAuxiliar.initSign(this.chavePrivada);

       assinaturaAuxiliar.update(ordemParaEnviar.getBytes());
       byte[] assinatura = assinaturaAuxiliar.sign();

       // Passa a assinatura pra Base64 pra concatenar ao final da mensagem como texto.
       
       String novaAssinaturaBase64 = DatatypeConverter.printBase64Binary(assinatura);

       String mensagem = String.format("%s:%s:%s:", ordemParaEnviar, this.nomeNO, novaAssinaturaBase64);
       
       // Adiciona a mensagem anterior do general na integra ao final desta mensagem
       
       mensagem += "," + mensagemOriginal;

       System.out.println("Reenviando a ordem recebida para os outros Tenentes");
       
       // manda a mensagem para os outros tenentes
       
       enviarOrdemMensagem(mensagem, this.outrosTenentes);
       
   }

}