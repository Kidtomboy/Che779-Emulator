using System;
using System.Collections.Generic;
using System.IO;

namespace Che779.Remoter
{
    /// <summary>Luu cau hinh dang key=value (file mac dinh canh exe, hoac file ho so tuy chon).</summary>
    internal class Settings
    {
        private readonly string path = Path.Combine(AppInfo.Ensure(AppInfo.RemoterDir), "che779_remoter.ini");
        private readonly Dictionary<string, string> map = new Dictionary<string, string>();

        public void Load()
        {
            // chuyen cau hinh cu (canh file exe) sang %APPDATA% mot lan
            try
            {
                string old = Path.Combine(AppDomain.CurrentDomain.BaseDirectory, "che779_remoter.ini");
                if (!File.Exists(path) && File.Exists(old)) File.Copy(old, path);
            }
            catch (Exception) { }
            LoadFrom(path);
        }
        public void Save() { SaveTo(path); }
        public void Clear() { map.Clear(); }

        public void LoadFrom(string file)
        {
            try
            {
                if (!File.Exists(file)) return;
                foreach (string line in File.ReadAllLines(file))
                {
                    int i = line.IndexOf('=');
                    if (i <= 0) continue;
                    map[line.Substring(0, i).Trim()] = line.Substring(i + 1);
                }
            }
            catch (Exception) { }
        }

        public void SaveTo(string file)
        {
            try
            {
                string dir = Path.GetDirectoryName(file);
                if (!string.IsNullOrEmpty(dir)) Directory.CreateDirectory(dir);
                List<string> lines = new List<string>();
                foreach (KeyValuePair<string, string> kv in map) lines.Add(kv.Key + "=" + kv.Value);
                File.WriteAllLines(file, lines.ToArray());
            }
            catch (Exception) { }
        }

        public string Get(string key, string def)
        {
            string v;
            return map.TryGetValue(key, out v) ? v : def;
        }

        public int GetInt(string key, int def)
        {
            int n;
            return int.TryParse(Get(key, ""), out n) ? n : def;
        }

        public bool GetBool(string key, bool def)
        {
            string v = Get(key, "");
            if (v == "1") return true;
            if (v == "0") return false;
            return def;
        }

        public void Set(string key, string value) { map[key] = value ?? ""; }
        public void SetInt(string key, int value) { map[key] = value.ToString(); }
        public void SetBool(string key, bool value) { map[key] = value ? "1" : "0"; }
    }
}
