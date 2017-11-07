package net.fishtron.utils;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.SeriesRenderingOrder;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.DeviationRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.UnknownKeyException;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.*;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.RefineryUtilities;

import java.awt.*;
import java.util.List;

public class MyGraph extends ApplicationFrame {

    public static void main(String[] args) {

        TimeSeriesCollection tsDataset = new TimeSeriesCollection();
        tsDataset.addSeries(mkS1_b());

        //MyGraph demo =
        new MyGraph( tsDataset , new Opts("Se s tim smiř!","time","price",true) );

        TimeSeries ts = mkS2_b();
        tsDataset.addSeries(ts);

        XYSeriesCollection xyDataset = new XYSeriesCollection();

        new MyGraph( xyDataset , new Opts("Klasický graf!","x-vole","y-pičo",true) );

        XYSeries xys1 = new XYSeries("Key1");
        XYSeries xys2 = new XYSeries("sérka2");

        xyDataset.addSeries(xys1);
        xyDataset.addSeries(xys2);


        int stepSize = 1000000;
        int steps = 100;

        for (int i = 0 ; i < steps*stepSize; i++) {
            if (i % stepSize == 0) {
                int x = i/stepSize;
                xys1.add(x, Math.random());

                if (Math.random() < 0.25) {
                    xys2.add(x,Math.random()/2);
                }
            }
        }

        new MyGraph(new XYSeries("BLAFUJU"),new Opts("NEBLAFUJ"));
        new MyGraph();
    }


    public static void copySeries(XYSeries target, XYSeries source) {
        target.clear();
        for (Object item_ : source.getItems()) {
            target.add( (XYDataItem) item_ );
        }
    }

    public static XYSeries list2series(String name,List<Double> xs) {
        return list2series(name,xs,0);
    }

    public static XYSeries list2series(String name,List<Double> xs, int start) {
        XYSeries ret = new XYSeries(name);
        int n = xs.size();

        for (int i = 0; i < n; i++) {
            ret.add( start+i , xs.get(i));
        }

        return ret;
    }

    private JFreeChart chart;
    private XYDataset dataset;

    public static class FrameOpts {
        public final int x;
        public final int y;
        public final int width;
        public final int height;

        public FrameOpts(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }

    public static class Opts {
        private String title;
        private String xLabel;
        private String yLabel;
        private boolean drawPoints;
        private FrameOpts frameOpts;

        public Opts() {this("MyGraph");}
        public Opts(String title) {this(title, false);}
        public Opts(String title,boolean drawPoints) {this(title, "x", "y", drawPoints, null);}
        public Opts(String title, String xLabel, String yLabel, boolean drawPoints) {
            this(title, xLabel, yLabel, drawPoints, null);
        }

        public Opts(String title, String xLabel, String yLabel, boolean drawPoints, FrameOpts frameOpts) {
            this.title = title;
            this.xLabel = xLabel;
            this.yLabel = yLabel;
            this.drawPoints = drawPoints;
            this.frameOpts = frameOpts;
        }

        public MyGraph mk() {
            return new MyGraph(this);
        }

        public MyGraph mk(XYDataset data) {
            return new MyGraph(data,this);
        }

        public MyGraph mk(XYSeries s) {
            XYSeriesCollection data = new XYSeriesCollection(s);
            return new MyGraph(data,this);
        }

        public String getTitle() {return title;}
        public String getLabelX() {return xLabel;}
        public String getLabelY() {return yLabel;}
        public boolean isDrawPoints() {return drawPoints;}
    }

    public MyGraph() {this(new Opts());}
    public MyGraph(Opts opts) {this(new XYSeriesCollection(),opts);}
    public MyGraph(XYSeries xys, Opts opts) {this(new XYSeriesCollection(xys),opts);}
    public MyGraph(TimeSeries ts, Opts opts) {this(new TimeSeriesCollection(ts),opts);}

    public MyGraph(XYDataset dataset, Opts opts) {
        super(opts.getTitle()); // tady to je frame-title



        this.dataset = dataset;
        ChartPanel chartPanel = (ChartPanel) mkPanel(dataset,opts);
        chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
        setContentPane(chartPanel);

        pack();

        if (opts.frameOpts != null) {
            setBounds(opts.frameOpts.x, opts.frameOpts.y, opts.frameOpts.width, opts.frameOpts.height);
        } else {
            RefineryUtilities.centerFrameOnScreen(this);
        }

        setVisible(true);
    }

    public JFreeChart getChart() {return chart;}

    public XYSeriesCollection getXYSeriesCollection() {
        if (dataset instanceof XYSeriesCollection) {return (XYSeriesCollection) dataset;}
        else {throw new Error("MyGraph has not a XYSeriesCollection dataset, sorry.");}
    }

    public MyGraph addXYSeries(XYSeries s) {
        getXYSeriesCollection().addSeries(s);
        return this;
    }

    public MyGraph changeXYSeries(XYSeries s) {
        try {
            XYSeries ss = getXYSeriesCollection().getSeries(s.getKey());
            getXYSeriesCollection().removeSeries(ss);
        } catch (UnknownKeyException e) {}
        addXYSeries(s);
        return this;
    }

    private JPanel mkPanel(XYDataset series, Opts opts) {
        chart = createChart(series, opts);
        ChartPanel panel = new ChartPanel(chart);
        panel.setFillZoomRectangle(true);
        panel.setMouseWheelEnabled(true);
        return panel;
    }

    private static TimeSeries mkS1_b() {
        TimeSeries ret = new TimeSeries("LolStar Inc.");

        ret.add(new Day(Log.parseDate("2014-11-01")), 100);
        ret.add(new Day(Log.parseDate("2014-11-02")), 110);
        ret.add(new Day(Log.parseDate("2014-11-03")), 102);
        ret.add(new Day(Log.parseDate("2014-11-04")), 150);

        return ret;
    }

    private static TimeSeries mkS2_b() {
        TimeSeries ret = new TimeSeries("ROFL, spol. s r.o.");

        ret.add(new Day(Log.parseDate("2014-11-01")), 200);
        ret.add(new Day(Log.parseDate("2014-11-02")), 210);
        ret.add(new Day(Log.parseDate("2014-11-03")), 102);
        ret.add(new Day(Log.parseDate("2014-11-04")), 10);

        return ret;
    }



    /* -- tady pokračuje "původní" kód  */

    private static final long serialVersionUID = 1L;

    static {
        // set a theme using the new shadow generator feature available in
        // 1.0.14 - for backwards compatibility it is not enabled by default
        ChartFactory.setChartTheme(new StandardChartTheme("JFree/Shadow",true));
    }



    /**
     * Creates a chart.
     *
     * @param dataset  a dataset.
     *
     * @return A chart.
     */
    private static JFreeChart createChart(XYDataset dataset, Opts opts) {

        JFreeChart chart;

        // todo udělat nějak min halabala
        if (dataset instanceof TimeSeriesCollection) {
            chart = ChartFactory.createTimeSeriesChart(
                    opts.getTitle(),    // title
                    opts.getLabelX(),   // x-axis label
                    opts.getLabelY(),   // y-axis label
                    dataset,            // data
                    true,               // create legend?
                    true,               // generate tooltips?
                    false               // generate URLs?
            );

        } /*else if(dataset instanceof YIntervalSeriesCollection) {
            chart = ChartFactory.createXYLineChart(
                    opts.getTitle(),
                    opts.getLabelX(),
                    opts.getLabelY(),
                    dataset,
                    PlotOrientation.VERTICAL,
                    true,
                    true,
                    false);
        }*/ else {
            chart = ChartFactory.createXYLineChart(
                    opts.getTitle(),
                    opts.getLabelX(),
                    opts.getLabelY(),
                    dataset,
                    PlotOrientation.VERTICAL,
                    true,
                    true,
                    false
            );
        }


        chart.setBackgroundPaint(Color.white);

        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.lightGray);
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);
        plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
        plot.setDomainCrosshairVisible(true);
        plot.setRangeCrosshairVisible(true);

        plot.setSeriesRenderingOrder(SeriesRenderingOrder.FORWARD);

        if(dataset instanceof YIntervalSeriesCollection) {
            /* todo rozahy začatek otazka esli neco nekazí - musí bejt v ifu ??? */
            DeviationRenderer deviationrenderer = new DeviationRenderer(true, false);
            //deviationrenderer.setSeriesStroke(0, new BasicStroke(3F, 1, 1));
            //deviationrenderer.setSeriesStroke(1, new BasicStroke(3F, 1, 1));
            deviationrenderer.setSeriesFillPaint(0, new Color(255, 200, 200));
            deviationrenderer.setSeriesFillPaint(1, new Color(200, 200, 255));
            plot.setRenderer(deviationrenderer);
            /*rozsahy konec*/
        }

        XYItemRenderer r = plot.getRenderer();
        if (r instanceof XYLineAndShapeRenderer) {
            XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) r;
            renderer.setBaseShapesVisible(opts.isDrawPoints());
            renderer.setBaseShapesFilled(opts.isDrawPoints());
            renderer.setDrawSeriesLineAsPath(true);
        }


        // todo tady taky halabla
        if (dataset instanceof TimeSeriesCollection) {
            DateAxis axis = (DateAxis) plot.getDomainAxis();
            axis.setDateFormatOverride(Log.dateFormat);
        }

        return chart;

    }







}
