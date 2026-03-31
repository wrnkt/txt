package org.tanchee.inngest;

public class InngestFunctionTriggers {
    public static class Event extends InngestFunctionTrigger {
        public Event(String event, String ifExpression) {
            super(event, ifExpression, null);
        }
    }
    public static class Cron extends InngestFunctionTrigger {
        public Cron(String cron) {
            super(null, null, cron);
        }
    }
}
