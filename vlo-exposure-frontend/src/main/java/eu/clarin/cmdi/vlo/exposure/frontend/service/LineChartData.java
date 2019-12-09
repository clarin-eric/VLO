package eu.clarin.cmdi.vlo.exposure.frontend.service;

import org.wicketstuff.googlecharts.AbstractChartData;

public class LineChartData extends AbstractChartData {
    private String title;
    private String [] labels;
    private double[] values;
    private int YaxisPoints = 8;
    private int XaxisPoints = 5;

    public LineChartData(double max, String[] labels, double[] values) {
        super(max);
        this.labels = labels;
        this.values = values;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String[] getLabels() {
        return labels;
    }

    public double[] getValues() {
        return values;
    }

    public void setValues(double[] values) {
        this.values = values;
    }

    public int getYaxisPoints() {
        return YaxisPoints;
    }

    public void setYaxisPoints(int yaxisPoints) {
        YaxisPoints = yaxisPoints;
    }

    public int getXaxisPoints() {
        return XaxisPoints;
    }

    public void setXaxisPoints(int xaxisPoints) {
        XaxisPoints = xaxisPoints;
    }

    @Override
    public double[][] getData() {
        return new double[][]{ this.values};
    }

    public String[] getXAxisLabels(){
        return this.labels;
    }

    public String[] getYAxisLabels(){

        double step =  (getMax() / YaxisPoints);
        String[] yLabels = new String[YaxisPoints +1];
        yLabels[0] = "0";
        double j = 0;
        for(int i=1 ; i < YaxisPoints ;i++){
            j += step;
            yLabels[i] = String.valueOf((int)j) ;
        }

        yLabels[YaxisPoints] = String.valueOf((int)getMax());
        return  new String[]{ "0", Double.toString( (int)getMax()/3),  Double.toString( (int)getMax()*2/3),  Double.toString(getMax())};
    }

    public void setLabels(String[] labels){
        this.labels = labels;
    }

}
