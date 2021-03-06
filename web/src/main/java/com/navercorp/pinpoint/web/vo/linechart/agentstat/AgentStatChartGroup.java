/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.web.vo.linechart.agentstat;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.navercorp.pinpoint.common.bo.AgentStatCpuLoadBo;
import com.navercorp.pinpoint.common.bo.AgentStatMemoryGcBo;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.AgentStat;
import com.navercorp.pinpoint.web.vo.linechart.Chart;
import com.navercorp.pinpoint.web.vo.linechart.DataPoint;
import com.navercorp.pinpoint.web.vo.linechart.SampledTimeSeriesDoubleChartBuilder;
import com.navercorp.pinpoint.web.vo.linechart.SampledTimeSeriesLongChartBuilder;
import com.navercorp.pinpoint.web.vo.linechart.Chart.ChartBuilder;

/**
 * @author harebox
 * @author hyungil.jeong
 */
public class AgentStatChartGroup {

    private static enum ChartType {
        JVM_MEMORY_HEAP_USED,
        JVM_MEMORY_HEAP_MAX, 
        JVM_MEMORY_NON_HEAP_USED, 
        JVM_MEMORY_NON_HEAP_MAX, 
        JVM_GC_OLD_COUNT, 
        JVM_GC_OLD_TIME, 
        CPU_LOAD_JVM, 
        CPU_LOAD_SYSTEM
    }
    
    private static final int uncollectedData = -1;

    private String type;

    private final Map<ChartType, ChartBuilder<? extends Number, ? extends Number>> chartBuilders;
    
    private final Map<ChartType, Chart> charts;
    
    public AgentStatChartGroup(TimeWindow timeWindow) {
        this.chartBuilders = new EnumMap<ChartType, ChartBuilder<? extends Number, ? extends Number>>(ChartType.class);
        this.chartBuilders.put(ChartType.JVM_MEMORY_HEAP_USED, new SampledTimeSeriesLongChartBuilder(timeWindow, uncollectedData));
        this.chartBuilders.put(ChartType.JVM_MEMORY_HEAP_MAX, new SampledTimeSeriesLongChartBuilder(timeWindow, uncollectedData));
        this.chartBuilders.put(ChartType.JVM_MEMORY_NON_HEAP_USED, new SampledTimeSeriesLongChartBuilder(timeWindow, uncollectedData));
        this.chartBuilders.put(ChartType.JVM_MEMORY_NON_HEAP_MAX, new SampledTimeSeriesLongChartBuilder(timeWindow, uncollectedData));
        this.chartBuilders.put(ChartType.JVM_GC_OLD_COUNT, new SampledTimeSeriesLongChartBuilder(timeWindow, uncollectedData));
        this.chartBuilders.put(ChartType.JVM_GC_OLD_TIME, new SampledTimeSeriesLongChartBuilder(timeWindow, uncollectedData));
        this.chartBuilders.put(ChartType.CPU_LOAD_JVM, new SampledTimeSeriesDoubleChartBuilder(timeWindow, uncollectedData));
        this.chartBuilders.put(ChartType.CPU_LOAD_SYSTEM, new SampledTimeSeriesDoubleChartBuilder(timeWindow, uncollectedData));
        this.charts = new EnumMap<ChartType, Chart>(ChartType.class);
    }

    public void addAgentStats(List<AgentStat> agentStats) {
        for (AgentStat agentStat : agentStats) {
            addMemoryGcData(agentStat.getMemoryGc());
            addCpuLoadData(agentStat.getCpuLoad());
        }
    }

    public void buildCharts() {
        for (ChartType chartType : ChartType.values()) {
            this.charts.put(chartType, this.chartBuilders.get(chartType).buildChart());
        }
    }

    private void addMemoryGcData(AgentStatMemoryGcBo data) {
        if (data == null) {
            return;
        }
        this.type = data.getGcType();
        long timestamp = data.getTimestamp();
        ((SampledTimeSeriesLongChartBuilder)this.chartBuilders.get(ChartType.JVM_MEMORY_HEAP_USED)).addDataPoint(new DataPoint<Long, Long>(timestamp, data.getJvmMemoryHeapUsed()));
        ((SampledTimeSeriesLongChartBuilder)this.chartBuilders.get(ChartType.JVM_MEMORY_HEAP_MAX)).addDataPoint(new DataPoint<Long, Long>(timestamp, data.getJvmMemoryHeapMax()));
        ((SampledTimeSeriesLongChartBuilder)this.chartBuilders.get(ChartType.JVM_MEMORY_NON_HEAP_USED)).addDataPoint(new DataPoint<Long, Long>(timestamp, data.getJvmMemoryNonHeapUsed()));
        ((SampledTimeSeriesLongChartBuilder)this.chartBuilders.get(ChartType.JVM_MEMORY_NON_HEAP_MAX)).addDataPoint(new DataPoint<Long, Long>(timestamp, data.getJvmMemoryNonHeapMax()));
        ((SampledTimeSeriesLongChartBuilder)this.chartBuilders.get(ChartType.JVM_GC_OLD_COUNT)).addDataPoint(new DataPoint<Long, Long>(timestamp, data.getJvmGcOldCount()));
        ((SampledTimeSeriesLongChartBuilder)this.chartBuilders.get(ChartType.JVM_GC_OLD_TIME)).addDataPoint(new DataPoint<Long, Long>(timestamp, data.getJvmGcOldTime()));
    }

    private void addCpuLoadData(AgentStatCpuLoadBo data) {
        if (data == null) {
            return;
        }
        long timestamp = data.getTimestamp();
        double jvmCpuLoadPercentage = data.getJvmCpuLoad() * 100;
        double systemCpuLoadPercentage = data.getSystemCpuLoad() * 100;
        ((SampledTimeSeriesDoubleChartBuilder)this.chartBuilders.get(ChartType.CPU_LOAD_JVM)).addDataPoint(new DataPoint<Long, Double>(timestamp, jvmCpuLoadPercentage));
        ((SampledTimeSeriesDoubleChartBuilder)this.chartBuilders.get(ChartType.CPU_LOAD_SYSTEM)).addDataPoint(new DataPoint<Long, Double>(timestamp, systemCpuLoadPercentage));
    }

    public String getType() {
        return type;
    }

    public Map<ChartType, Chart> getCharts() {
        return charts;
    }

}