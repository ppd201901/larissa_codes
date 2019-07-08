package guilhermeSchults.generaisBizantinos.No_Tenente;

public class EnderecoTenente 
{

    private String url;
    private String porta;
    private String nomeDoNo;
    
    public EnderecoTenente(String url, String porta, String nomeDoNo)
    {
        this.url = url;
        this.porta = porta;
        this.nomeDoNo = nomeDoNo;
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

    public String getnomeDoNo() 
    {
        return nomeDoNo;
    }
    
    @Override
    public String toString() 
    {
        return "Nome = " +nomeDoNo+ ", " + "url = " + url + ", porta = " + porta + "";

    }
    
}
