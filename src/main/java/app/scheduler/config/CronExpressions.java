package app.scheduler.config;

public final class CronExpressions {
    
    public static final String EVERY_MINUTE = "0 * * * * *";
    public static final String DAILY_AT_9AM = "0 0 9 * * ?";
    public static final String MONTHLY_FIRST_DAY_9AM = "0 0 9 1 * ?";
    
    private CronExpressions() {
        throw new UnsupportedOperationException("Utility class");
    }
}

