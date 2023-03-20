package com.mpay.configuration;

import java.util.Map;

import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;

public class MdcTaskDecorator implements TaskDecorator {

    @Override
    public Runnable decorate(Runnable runnable) {
        Map<String, String> contextMap = null;

        try {
            contextMap = MDC.getCopyOfContextMap();
        } catch (Exception e) {
            // protection against log lib raising exception and affecting application flow
        }
        Map<String, String> finalContextMap = contextMap;
        return () -> {
            try {
                try {
                    // Accommodate for the fact that MDC.getCopyOfContextMap() may return null
                    // See https://issues.apache.org/jira/browse/LOG4J2-2939
                    if (finalContextMap != null) {
                        MDC.setContextMap(finalContextMap);
                    }
                } catch (Exception e) {
                    // under certain circumstances the MCD lib is raising a NPE
                    // add protection so that the main method invocation is not affected
                }
                MDC.put("request_async", "true");
                runnable.run();
            } finally {
                MDC.clear();
            }
        };
    }
}