package org.primefaces.ultima.bean;



import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import org.primefaces.model.chart.BarChartModel;
import org.primefaces.model.chart.ChartSeries;

/**
 *
 * @author hvarona
 */
@ManagedBean(name = "utilBean")
@SessionScoped
public class UtilBean {


    private static final int MAX_WEEEKS = 8;

    private BarChartModel salesChart = null;
    private ChartSeries salesSeries;
    long lastWeeksSales = 0;

    private BarChartModel rechargesChart = null;
    private ChartSeries rechargesSeries;
    long lastWeeksRecharges = 0;

    private BarChartModel withdrawChart = null;
    private ChartSeries withdrawSeries;
    long lastWeeksWithdraws = 0;


    @PostConstruct
    public void init() {
 
    }

  

  
    public int getOperatorAmount() {
        
            return 125;
        
    }

}
