using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Transactions;

namespace TwoPhasesCommit
{
    public class Program
    {
        private static QueryBuilder queryBuilder = new QueryBuilder();

        public static KeyValuePair<Participante, string> primeiraTransacao()
        {
            var firstPostgresProxy = new Participante("localhost", "5432", "2pc", "123456", "mestrado");
            var insertForFirst = queryBuilder.GetInsertScript("disciplina", new List<string>()
            {
                "nome",
                @"id",
                @"professor"
            },
           new List<string>()
           {
                "Programação Paralela e Distribuída",
                "2",
                "Sérgio"
           });
            return new KeyValuePair<Participante, string>(firstPostgresProxy, insertForFirst);
        }

        public static KeyValuePair<Participante, string> segundaTransacao()
        {
            var secondPostgresConnection = new Participante("localhost", "5432", "2pc", "123456", "mestrado");
            var insertForSecond = queryBuilder.GetInsertScript("provas", new List<string>()
            {
                "data",
                "materia",
                "id"
            },
            new List<string>()
            {
                "2019-07-16",
                "Programação Paralela e Distribuída",
                "1"
            });

            return new KeyValuePair<Participante, string>(secondPostgresConnection, insertForSecond);
        }

        public static KeyValuePair<Participante, string> terceiraTransacao()
        {
            var thirdPostgresConnection = new Participante("localhost", "5432", "2pc", "123456", "mestrado");
            var insertForThird = "delete from provas where materia ilike '%Programação Paralela e Distribuída%';";
            return new KeyValuePair<Participante, string>(thirdPostgresConnection, insertForThird);
        }

        static void Main(string[] args)
        {
            var manager = new Gerenciador();
            var param = new List<KeyValuePair<Participante, string>>();
            param.Add(primeiraTransacao());
            param.Add(segundaTransacao());
            param.Add(terceiraTransacao());
            manager.PerformTwoPhaseCommit(param);
            Console.ReadLine();
        }
     
    }
}
