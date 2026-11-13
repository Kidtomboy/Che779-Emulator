using System;
using System.Drawing;
using System.Drawing.Drawing2D;
using System.Windows.Forms;

namespace Che779.Remoter
{
    /// <summary>Ban do luoi cot x hang danh so san 1..N; o dang co tab chay duoc to mau.</summary>
    internal class GridPreview : Control
    {
        public int Cols = 4;
        public int Rows = 2;
        /// <summary>0 = trong, 1 = co tab (dang dung), 2 = tab dang chay.</summary>
        public int[] States = new int[0];
        public Color EmptyColor = Color.Gainsboro;
        public Color StoppedColor = Color.Silver;
        public Color RunColor = Color.HotPink;
        public Color TextColor = Color.Black;
        public Color RunTextColor = Color.White;
        public Color BackFill = Color.White;

        public GridPreview()
        {
            SetStyle(ControlStyles.UserPaint | ControlStyles.AllPaintingInWmPaint
                | ControlStyles.OptimizedDoubleBuffer | ControlStyles.ResizeRedraw, true);
        }

        protected override void OnPaint(PaintEventArgs e)
        {
            Graphics g = e.Graphics;
            g.SmoothingMode = SmoothingMode.AntiAlias;
            using (SolidBrush bg = new SolidBrush(BackFill)) g.FillRectangle(bg, ClientRectangle);
            int cols = Math.Max(1, Math.Min(Cols, 24));
            int rows = Math.Max(1, Math.Min(Rows, 24));
            int cell = Math.Max(6, Math.Min((Width - 2) / cols, (Height - 2) / rows));
            int gw = cell * cols, gh = cell * rows;
            int ox = (Width - gw) / 2, oy = (Height - gh) / 2;
            float fs = Math.Max(6f, Math.Min(10f, cell / 3.2f));
            bool numbers = cell >= 16;
            using (Font f = new Font("Segoe UI", fs, FontStyle.Bold))
            using (StringFormat sf = new StringFormat())
            {
                sf.Alignment = StringAlignment.Center;
                sf.LineAlignment = StringAlignment.Center;
                for (int i = 0; i < cols * rows; i++)
                {
                    int c = i % cols, r = i / cols;
                    Rectangle cr = new Rectangle(ox + c * cell + 1, oy + r * cell + 1, cell - 3, cell - 3);
                    int st = i < States.Length ? States[i] : 0;
                    Color fill = st == 2 ? RunColor : (st == 1 ? StoppedColor : EmptyColor);
                    using (SolidBrush b = new SolidBrush(fill)) g.FillRectangle(b, cr);
                    if (numbers)
                    {
                        using (SolidBrush tb = new SolidBrush(st == 2 ? RunTextColor : TextColor))
                            g.DrawString((i + 1).ToString(), f, tb, cr, sf);
                    }
                }
            }
        }
    }
}
