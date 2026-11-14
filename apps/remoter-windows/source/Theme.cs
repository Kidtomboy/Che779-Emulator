using System.Drawing;

namespace Che779.Remoter
{
    internal class Theme
    {
        public Color Bg, Card, Text, Muted, Accent, AccentText, Border, Field, GridAlt, Danger;

        public static Theme Dark()
        {
            Theme t = new Theme();
            t.Bg = Color.FromArgb(0x1B, 0x1C, 0x21);
            t.Card = Color.FromArgb(0x25, 0x27, 0x2E);
            t.Text = Color.FromArgb(0xE8, 0xEA, 0xEF);
            t.Muted = Color.FromArgb(0x9A, 0xA0, 0xAC);
            t.Accent = Color.FromArgb(0xFF, 0x7A, 0xA8);
            t.AccentText = Color.FromArgb(0x25, 0x10, 0x18);
            t.Border = Color.FromArgb(0x3A, 0x3D, 0x47);
            t.Field = Color.FromArgb(0x1F, 0x21, 0x27);
            t.GridAlt = Color.FromArgb(0x29, 0x2B, 0x33);
            t.Danger = Color.FromArgb(0xFF, 0x6B, 0x6B);
            return t;
        }

        public static Theme Light()
        {
            Theme t = new Theme();
            t.Bg = Color.FromArgb(0xF3, 0xF4, 0xF7);
            t.Card = Color.White;
            t.Text = Color.FromArgb(0x22, 0x25, 0x2B);
            t.Muted = Color.FromArgb(0x6B, 0x72, 0x80);
            t.Accent = Color.FromArgb(0xE8, 0x58, 0x8B);
            t.AccentText = Color.White;
            t.Border = Color.FromArgb(0xD9, 0xDC, 0xE3);
            t.Field = Color.White;
            t.GridAlt = Color.FromArgb(0xF8, 0xF9, 0xFB);
            t.Danger = Color.FromArgb(0xD6, 0x3B, 0x3B);
            return t;
        }
    }
}
