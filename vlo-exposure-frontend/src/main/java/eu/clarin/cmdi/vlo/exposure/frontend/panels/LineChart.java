package eu.clarin.cmdi.vlo.exposure.frontend.panels;

import eu.clarin.cmdi.vlo.exposure.frontend.service.LineChartData;
import org.apache.wicket.markup.html.panel.Panel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wicketstuff.googlecharts.*;

import java.awt.*;


public class LineChart extends Panel {

    private static final String CHART_WICKET_ID = "CHART_PANEL";

    private final static Logger logger = LoggerFactory.getLogger(LineChart.class);

    public LineChart(String id, LineChartData chart) {
        super(id);
        createChart(chart);
    }

    private void createChart(LineChartData chart){
        try {
            ChartProvider provider = new ChartProvider(new Dimension(500, 250), ChartType.LINE,
                    chart);
            ChartAxis dateAxis = new ChartAxis(ChartAxisType.BOTTOM);
            dateAxis.setLabels(chart.getXAxisLabels());
            provider.addAxis(dateAxis);

            ChartAxis queryAxis = new ChartAxis(ChartAxisType.LEFT);
            queryAxis.setLabels(chart.getYAxisLabels());
            provider.addAxis(queryAxis);

            provider.setBackgroundFill(new SolidFill(Color.decode("#EFEFEF")));
            provider.addShapeMarker(new ShapeMarker(MarkerType.SQUARE, Color.decode("#cc6600"), 0, -1, 5));

            provider.setTitle(chart.getTitle());

            add(new Chart(CHART_WICKET_ID, provider));
        }catch(Exception ex) {
            logger.error(ex.getMessage());
        }
    }
}
