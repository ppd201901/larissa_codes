package guilhermeSchults.generaisBizantinos.No_Tenente;


import java.util.Objects;

/*
 * Metodo que faz a verificacao da ordem enviada atraves da assinatura
 */

public class OrdemAssinatura {


	    private String ordem;
	    private String assinatura; // em Base64

	    public OrdemAssinatura(String ordem, String assinatura) 
	    {
	        this.ordem = ordem;
	        this.assinatura = assinatura;
	    }

	    public String getOrdem() 
	    {
	        return ordem;
	    }

	    public void setOrdem(String ordem)
	    {
	        this.ordem = ordem;
	    }

	    public String getAssinatura() 
	    {
	        return assinatura;
	    }

	    public void setAssinatura(String assinatura) 
	    {
	        this.assinatura = assinatura;
	    }

	    @Override
	    public int hashCode() 
	    {
	        int hash = 5;
	        hash = 41 * hash + Objects.hashCode(this.ordem);
	        hash = 41 * hash + Objects.hashCode(this.assinatura);
	        return hash;
	    }

	    @Override
	    public boolean equals(Object obj)
	    {
	        if (this == obj) 
	        {
	            return true;
	        }
	        if (obj == null)
	        {
	            return false;
	        }
	        if (getClass() != obj.getClass()) 
	        {
	            return false;
	        }
	        final OrdemAssinatura outro = (OrdemAssinatura) obj;
	        if (!Objects.equals(this.ordem, outro.ordem))
	        {
	            return false;
	        }
	        if (!Objects.equals(this.assinatura, outro.assinatura))
	        {
	            return false;
	        }
	        return true;
	    }    

}
