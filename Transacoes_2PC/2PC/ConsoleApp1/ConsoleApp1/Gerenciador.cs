using System;
using System.Collections.Generic;

namespace TwoPhasesCommit
{
    public class Gerenciador
    {
        public void PerformTwoPhaseCommit(List<KeyValuePair<Participante, string>> listaParticipantes)
        {
            Console.WriteLine("Abrindo conexões");

            foreach (var participante in listaParticipantes)
            {
                participante.Key.OpenConnection();
            }

            Console.WriteLine("Conexões abertas");

            var sucesso = new List<Participante>();

            int i = 0;
            Console.WriteLine("Fase 1 - prepare");
            foreach (var participante in listaParticipantes)
            {
                var resultOfStart = participante.Key.Prepare(participante.Value, i++.ToString());

                if (resultOfStart)
                {
                    sucesso.Add(participante.Key);
                }
                else
                {
                    break;
                }
            }

            if (sucesso.Count == listaParticipantes.Count)
            {
                Console.WriteLine("Fase 2 - commit");
                foreach (var node in sucesso)
                {
                    node.Commit();
                }
            }
            else
            {
                foreach (var node in sucesso)
                {
                    Console.WriteLine("Rollback");
                    node.Rollback();
                }
            }


            Console.WriteLine("Fechando conexões");
            foreach (var participante in listaParticipantes)
            {
                participante.Key.CloseConnection();
            }
        }
    }
}
