using System;
using System.IO;
using System.Net.Sockets;
using System.Text;

namespace Che779.Remoter
{
    /// <summary>Ket noi socket localhost toi 1 Che779 Emulator. Moi dong = 1 lenh JSON, tra ve 1 dong JSON.</summary>
    internal class IpcClient
    {
        private readonly object gate = new object();
        private TcpClient cli;
        private StreamReader rd;
        private StreamWriter wr;
        public int Port;

        public IpcClient(int port) { Port = port; }

        /// <returns>Dong JSON tra loi, hoac null neu that bai.</returns>
        public string Send(string json)
        {
            lock (gate)
            {
                for (int attempt = 0; attempt < 2; attempt++)
                {
                    try
                    {
                        Ensure();
                        wr.WriteLine(json);
                        wr.Flush();
                        return rd.ReadLine();
                    }
                    catch (Exception)
                    {
                        Close();
                    }
                }
                return null;
            }
        }

        private void Ensure()
        {
            if (cli != null && cli.Connected) return;
            Close();
            cli = new TcpClient();
            cli.NoDelay = true;
            IAsyncResult ar = cli.BeginConnect("127.0.0.1", Port, null, null);
            if (!ar.AsyncWaitHandle.WaitOne(800))
            {
                cli.Close();
                cli = null;
                throw new IOException("connect timeout");
            }
            cli.EndConnect(ar);
            cli.ReceiveTimeout = 1500;
            cli.SendTimeout = 1500;
            NetworkStream ns = cli.GetStream();
            rd = new StreamReader(ns, Encoding.UTF8);
            wr = new StreamWriter(ns, new UTF8Encoding(false));
            wr.NewLine = "\n";
        }

        public void Close()
        {
            try { if (cli != null) cli.Close(); } catch (Exception) { }
            cli = null; rd = null; wr = null;
        }
    }
}
