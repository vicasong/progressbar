package me.tongfei.progressbar;

import java.text.DecimalFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.AbstractMap;
import java.util.Map;

import static me.tongfei.progressbar.StringDisplayUtils.getStringDisplayLength;
import static me.tongfei.progressbar.StringDisplayUtils.trimDisplayLength;

/**
 * Dynamic speed unit progress bar renderer (see {@link ProgressBarRenderer}).
 * @author vica
 * @since 0.9.4
 */
public class DynamicSpeedUnitProgressBarRenderer extends DefaultProgressBarRenderer {

    private SpeedDynamics speedDynamics;
    private ChronoUnit speedUnit;
    private DecimalFormat speedFormat;

    protected DynamicSpeedUnitProgressBarRenderer(ProgressBarStyle style,
                                                  String unitName,
                                                  long unitSize,
                                                  boolean isSpeedShown,
                                                  DecimalFormat speedFormat,
                                                  ChronoUnit speedUnit,
                                                  SpeedDynamics speedDynamics) {
        super(style, unitName, unitSize, isSpeedShown, speedFormat, speedUnit);
        this.speedDynamics = speedDynamics == null ? new DefaultDataSizeSpeedDynamics(): speedDynamics;
        this.speedFormat = isSpeedShown && speedFormat == null ? new DecimalFormat() : speedFormat;
        this.speedUnit = speedUnit;
    }

    @Override
    protected String speed(ProgressState progress, Duration elapsed) {
        String suffix = "/s";
        double elapsedSeconds = elapsed.getSeconds();
        double elapsedInUnit = elapsedSeconds;
        if (null != speedUnit)
            switch (speedUnit) {
                case MINUTES:
                    suffix = "/min";
                    elapsedInUnit /= 60;
                    break;
                case HOURS:
                    suffix = "/h";
                    elapsedInUnit /= (60 * 60);
                    break;
                case DAYS:
                    suffix = "/d";
                    elapsedInUnit /= (60 * 60 * 24);
                    break;
            }

        double speed = (double) (progress.current - progress.start) / elapsedInUnit;
        Map.Entry<String, Long> unit = speedDynamics.unit((long)speed);
        String unitName = unit.getKey();
        long unitSize = unit.getValue();
        if (elapsedSeconds == 0)
            return "?" + unitName + suffix;
        double speedWithUnit = speed / unitSize;
        return speedFormat.format(speedWithUnit) + unitName + suffix;
    }


    public interface SpeedDynamics {
        /** get current unitName and unitSize of value */
        Map.Entry<String, Long> unit(long value);
    }


    private static class DefaultDataSizeSpeedDynamics implements SpeedDynamics {

        static final String suffixB = "B";
        static final String suffixKB = "KB";
        static final String suffixMB = "MB";
        static final String suffixGB = "GB";
        static final String suffixTB = "TB";
        static final long unit = 1024;
        static final long unitB = 1;
        static final long unitKB = unitB * unit;
        static final long unitMB = unitKB * unit;
        static final long unitGB = unitMB * unit;
        static final long unitTB = unitGB * unit;

        @Override
        public Map.Entry<String, Long> unit(long size) {
            if (0 == size) {
                return new AbstractMap.SimpleEntry<>(suffixB, unitB);
            }
            String suffix;
            long currentUnit;
            if (size >= unitTB) {
                currentUnit = unitTB;
                suffix = suffixTB;
            } else if (size >= unitGB) {
                currentUnit = unitGB;
                suffix = suffixGB;
            } else if (size >= unitMB) {
                currentUnit = unitMB;
                suffix = suffixMB;
            } else if (size >= unitKB) {
                currentUnit = unitKB;
                suffix = suffixKB;
            } else {
                currentUnit = unitB;
                suffix = suffixB;
            }
            return new AbstractMap.SimpleEntry<>(suffix, currentUnit);
        }
    }
}
