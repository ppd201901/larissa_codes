using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Classes
{
    public class Exercicios
    {
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
    }
}
