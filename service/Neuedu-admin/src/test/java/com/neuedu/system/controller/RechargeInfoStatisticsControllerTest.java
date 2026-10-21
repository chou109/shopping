package com.neuedu.system.controller;

import com.neuedu.common.core.domain.AjaxResult;
import com.neuedu.system.service.IRechargeInfoService;
import org.junit.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

public class RechargeInfoStatisticsControllerTest {
    @Test
    public void statisticsForwardValidatedInclusiveDateFilters() {
        Map<String, Object> statistics = Collections.singletonMap("successCount", 3);
        AtomicReference<String[]> dates = new AtomicReference<>();
        RechargeInfoController controller = controllerFor((methodName, args) -> {
            if ("selectRechargeStatistics".equals(methodName)) {
                dates.set(new String[]{(String) args[0], (String) args[1]});
                return statistics;
            }
            return null;
        });

        AjaxResult result = controller.getStatistics("2026-10-01", "2026-10-31");

        assertEquals(200, result.get("code"));
        assertSame(statistics, result.get("data"));
        assertEquals("2026-10-01", dates.get()[0]);
        assertEquals("2026-10-31", dates.get()[1]);
    }

    @Test
    public void statisticsRejectMalformedAndReversedDates() {
        RechargeInfoController controller = controllerFor((methodName, args) -> {
            throw new AssertionError("Invalid date range must not query statistics");
        });

        assertEquals(400, controller.getStatistics("2026-02-30", "2026-03-01").get("code"));
        assertEquals(400, controller.getStatistics("2026-10-31", "2026-10-01").get("code"));
    }

    @Test
    public void statisticsAllowAnUnboundedDateRange() {
        AtomicReference<String[]> dates = new AtomicReference<>();
        RechargeInfoController controller = controllerFor((methodName, args) -> {
            if ("selectRechargeStatistics".equals(methodName)) {
                dates.set(new String[]{(String) args[0], (String) args[1]});
                return Collections.emptyMap();
            }
            return null;
        });

        AjaxResult result = controller.getStatistics(null, null);

        assertEquals(200, result.get("code"));
        assertNull(dates.get()[0]);
        assertNull(dates.get()[1]);
    }

    private RechargeInfoController controllerFor(ServiceCall call) {
        InvocationHandler handler = (instance, method, args) -> call.invoke(method.getName(), args);
        IRechargeInfoService service = IRechargeInfoService.class.cast(Proxy.newProxyInstance(
                IRechargeInfoService.class.getClassLoader(), new Class<?>[]{IRechargeInfoService.class}, handler));
        return new RechargeInfoController(service);
    }

    private interface ServiceCall {
        Object invoke(String methodName, Object[] args);
    }
}
