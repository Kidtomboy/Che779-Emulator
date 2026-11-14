using System;
using System.Drawing;
using System.Drawing.Drawing2D;
using System.Windows.Forms;

namespace Che779.Remoter
{
    /// <summary>The bo goc co huy hieu so (1, 2, 3...) va tieu de.</summary>
    internal class CardPanel : Panel
    {
        public int Number;
        public string Title = "";
        public Color CardColor = Color.White;
        public Color BorderColor = Color.Gray;
        public Color BadgeColor = Color.HotPink;
        public Color BadgeTextColor = Color.White;
        public Color TitleColor = Color.Black;
        public Color OuterColor = Color.WhiteSmoke;

        public CardPanel(int number, string title)
        {
            Number = number;
            Title = title;
            SetStyle(ControlStyles.UserPaint | ControlStyles.AllPaintingInWmPaint
                | ControlStyles.OptimizedDoubleBuffer | ControlStyles.ResizeRedraw, true);
            Padding = new Padding(12, 44, 12, 10);
        }

        protected override void OnPaint(PaintEventArgs e)
        {
            Graphics g = e.Graphics;
            g.SmoothingMode = SmoothingMode.AntiAlias;
            using (SolidBrush outer = new SolidBrush(OuterColor)) g.FillRectangle(outer, ClientRectangle);
            Rectangle r = new Rectangle(0, 0, Width - 1, Height - 1);
            using (GraphicsPath gp = RoundRect(r, 12))
            {
                using (SolidBrush b = new SolidBrush(CardColor)) g.FillPath(b, gp);
                using (Pen p = new Pen(BorderColor)) g.DrawPath(p, gp);
            }
            Rectangle badge = new Rectangle(12, 10, 26, 26);
            using (SolidBrush bb = new SolidBrush(BadgeColor)) g.FillEllipse(bb, badge);
            using (Font f = new Font("Segoe UI", 10f, FontStyle.Bold))
            using (StringFormat sf = new StringFormat())
            using (SolidBrush tb = new SolidBrush(BadgeTextColor))
            {
                sf.Alignment = StringAlignment.Center;
                sf.LineAlignment = StringAlignment.Center;
                g.DrawString(Number.ToString(), f, tb, new RectangleF(badge.X, badge.Y + 1, badge.Width, badge.Height), sf);
            }
            using (Font f2 = new Font("Segoe UI Semibold", 10.5f))
            using (SolidBrush tb2 = new SolidBrush(TitleColor))
            {
                g.DrawString(Title, f2, tb2, 46, 12);
            }
        }

        private static GraphicsPath RoundRect(Rectangle r, int rad)
        {
            GraphicsPath p = new GraphicsPath();
            int d = rad * 2;
            p.AddArc(r.X, r.Y, d, d, 180, 90);
            p.AddArc(r.Right - d, r.Y, d, d, 270, 90);
            p.AddArc(r.Right - d, r.Bottom - d, d, d, 0, 90);
            p.AddArc(r.X, r.Bottom - d, d, d, 90, 90);
            p.CloseFigure();
            return p;
        }
    }
}
