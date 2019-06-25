using System.Web.Services;

namespace Webservice
{
    /// <summary>
    /// PPD
    /// </summary>
    [WebService(Namespace = "http://tempuri.org/")]
    [WebServiceBinding(ConformsTo = WsiProfiles.BasicProfile1_1)]
    [System.ComponentModel.ToolboxItem(false)]
    // Para permitir que esse serviço da web seja chamado a partir do script, usando ASP.NET AJAX, remova os comentários da linha a seguir. 
    // [System.Web.Script.Services.ScriptService]

        //O protocolo SOAP utiliza XML para enviar mensagens e, geralmente, serve-se do protocolo HTTP para transportar os dados.
        //Associado ao protocolo SOAP está o documento WSDL (Web Service Definition Language) que descreve a localização do Web service
        //e as operações que dispõe.Além disso, fornece a informação necessária para que a comunicação entre sistemas seja possível.

        //Simple Object Access Protocol
        //Baseado em xml estabeleceu uma estrutura de transmissão para comunicação entre aplicações via HTTP.
        //Alternativa entre os protocolos proprietários: CORBA e DCOM
    
            public class WebService1 : System.Web.Services.WebService
    {
        [WebMethod]
        public double ReajustarSalario(string Nome, string Cargo, double Salario)
        {
            if (Cargo.ToLower() == "programador")
            {
                return Salario * 1.18;
            }
            else if (Cargo.ToLower() == "operador")
            {
                return Salario * 1.2;
            }
            else
            {
                return Salario;
            }
        }

        [WebMethod]
        public bool ChecarMaioridade(string Nome, string Sexo, int Idade)
        {
            if ((Sexo == "F" && Idade >= 21) || (Sexo == "M" && Idade >= 18))
            {
                return true;
            }
            else
            {
                return false;
            }
        }
    }
}
