using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Hangfire;
using Hangfire.PostgreSql;
using System.Diagnostics;


namespace Server
{
    class Program
    {
        static void Main(string[] args)
        {
            var connectionString = "Server=localhost;Port=5432;User Id=fila;Password=123456;Database=hangfire;Pooling=true;";

            var options = new BackgroundJobServerOptions()
            {
                WorkerCount = 10,
            };

            var storage = new PostgreSqlStorage(connectionString, new PostgreSqlStorageOptions()
            {
                InvisibilityTimeout = TimeSpan.FromMinutes(1),
                PrepareSchemaIfNecessary = true
            });

            using (var server = new BackgroundJobServer(options, storage))
            {
                Console.Title = "Server";
                Console.WriteLine("Pressione qualquer tecla para encerrar.");
                Console.ReadKey();
            }
        }
    }
}
