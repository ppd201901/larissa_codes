package guilhermeSchults.generaisBizantinos.No_General;

public class EnderecoTenente 
{
	
	private String url;
    private String porta;
    
    private String nome;

    public EnderecoTenente(String url, String porta, String nome) 
    {
        this.url = url;
        this.porta = porta;
        
        this.nome = nome;
    }
    
    public String getnomeDoNo() 
    {
        return nome;
    }
  
    public String getUrl() 
    {
        return url;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    public int getPorta() 
    {
        return Integer.parseInt(porta);
    }

    public void setPorta(String porta) 
    {
        this.porta = porta;
    }  
    @Override
    public String toString() 
    {
        return "Nome = " +nome+ ", " + "url = " + url + ", porta = " + porta + "";

    }

}
