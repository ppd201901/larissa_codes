using Npgsql;
using System;
using System.Collections.Generic;
using System.Data;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Transactions;

namespace TwoPhasesCommit
{
   public class Participante
    {
        public bool Erro = false;
        private const string connectionStringTemplate = "Server={0}; Port = {1}; User Id = {2}; Password = {3}; Database={4}";
        private NpgsqlConnection Connection;
        private string ConnectionString;
        private string Description;


        public Participante(string server, string port, string userId, string userPassword, string databaseName)
        {
            this.ConnectionString = String.Format(connectionStringTemplate, server, port, userId, userPassword, databaseName);
            this.Connection = new NpgsqlConnection(this.ConnectionString);
        }

        public void OpenConnection()
        {
            Console.WriteLine("{0}: conexão aberta", this.ConnectionString);
            this.Connection.Open();
        }

        public bool Prepare(string query, string transactionName)
        {
            try
            {
                Console.WriteLine("Preparando transação {0} em {1}", transactionName, this.ConnectionString);
                var queryToPerform = String.Format("BEGIN; {0}; PREPARE TRANSACTION '{1}';", query, transactionName);
                this.Description = transactionName;
                var command = new NpgsqlCommand(queryToPerform, this.Connection);
                command.ExecuteNonQuery();
                Console.WriteLine("Transação {0} em {1}", this.Description, this.ConnectionString);
                return true;
            }
            catch (Exception ex)
            {
                Console.WriteLine("ERRO: {0}", ex.Message);
                return false;
            }
        }

        public void Rollback()
        {
            if (Erro)
            {
                Console.WriteLine("Participante {0} retornou erro. Nenhuma transação em andamento", this.ConnectionString);
                return;
            }
            if (String.IsNullOrEmpty(this.Description))
            {
                Console.WriteLine("Nenhuma transação em andamento {0}", this.ConnectionString);
                throw new Exception("Nenhuma transação em andamento");
            }
            var command = new NpgsqlCommand(String.Format("ROLLBACK PREPARED '{0}'", this.Description), this.Connection);
            Console.WriteLine("Transação {0} cancelada em {1}", this.Description, this.ConnectionString);
            this.Description = String.Empty;
            command.ExecuteNonQuery();
        }

        public void Commit()
        {
            if (Erro)
            {
                Console.WriteLine("Participante {0} retornou erro. Nenhuma transação em andamento", this.ConnectionString);
                return;
            }
            if (String.IsNullOrEmpty(this.Description))
            {
                Console.WriteLine("Nenhuma transação em andamento {0}", this.ConnectionString);
                throw new Exception("Nenhuma transação em andamento");
            }
            var command = new NpgsqlCommand(String.Format("COMMIT PREPARED '{0}'", this.Description), this.Connection);
            Console.WriteLine("Transação {0} commitada em {1}", this.Description, this.ConnectionString);
            this.Description = String.Empty;
            command.ExecuteNonQuery();
        }

        public void CloseConnection()
        {
            Console.WriteLine("{0}: conexão fechada", this.ConnectionString);
            this.Connection.Close();
        }
    }
}
