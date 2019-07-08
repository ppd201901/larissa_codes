package guilhermeSchults.generaisBizantinos.GeradorDeChaves;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;

public class GeradorChaves 
{

	public static void main(String[] args) throws NoSuchAlgorithmException, NoSuchProviderException, FileNotFoundException, IOException, InvalidKeyException, SignatureException
	{
		System.out.println("Criando novo par de chaves (Privada e Publica):\n");
        KeyPairGenerator geradorChaves = KeyPairGenerator.getInstance("DSA");
        
        geradorChaves.initialize(1024);
        
        KeyPair paresChaves = geradorChaves.generateKeyPair();
        PrivateKey chavePrivada = paresChaves.getPrivate();
        PublicKey chavePublica = paresChaves.getPublic();
        
        String caminhoAtualOS = new File("").getAbsolutePath();

        System.out.printf("%s%schave-Publica\n", caminhoAtualOS, File.separator);        
        outputToFile(chavePublica.getEncoded(), "chave-Publica");
        System.out.printf("%s%schave-Privada\n", caminhoAtualOS, File.separator);
        outputToFile(chavePrivada.getEncoded(), "chave-Privada");
        System.out.println("\nFinalizado.");       

	}
	
	 private static void outputToFile(byte[] array, String nomeArquivo) throws FileNotFoundException, IOException
	 {
	    FileOutputStream output = new FileOutputStream(nomeArquivo);
	    output.write(array);
	    output.close();
	 }

}

