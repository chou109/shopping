package com.neuedu.system.controller;

import com.neuedu.common.core.domain.AjaxResult;
import com.neuedu.system.domain.Activity;
import com.neuedu.system.service.IActivityService;
import org.junit.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertNotNull;

public class ActivityControllerTest {
    @Test
    public void timeRangeFindsActivitiesAndIncludesTheWholeEndDate() {
        Activity activity = new Activity();
        AtomicReference<Date[]> dates = new AtomicReference<>();
        ActivityController controller = controllerFor((methodName, args) -> {
            if ("selectActivityByTimeRange".equals(methodName)) {
                dates.set(new Date[]{(Date) args[0], (Date) args[1]});
                return Collections.singletonList(activity);
            }
            return null;
        });

        AjaxResult result = controller.getByTimeRange("2026-10-01", "2026-10-03");

        assertEquals(200, result.get("code"));
        assertSame(activity, ((java.util.List<?>) result.get("data")).get(0));
        assertEquals(3L * 24 * 60 * 60 * 1000 - 1, dates.get()[1].getTime() - dates.get()[0].getTime());
    }

    @Test
    public void timeRangeRejectsInvalidDatesAndReversedRanges() {
        ActivityController controller = controllerFor((methodName, args) -> {
            throw new AssertionError("Invalid range must not query the service");
        });

        assertEquals(400, controller.getByTimeRange("2026-02-30", "2026-03-01").get("code"));
        assertEquals(400, controller.getByTimeRange("2026-10-03", "2026-10-01").get("code"));
    }

    @Test
    public void validateTimeReturnsValidityForIsoDates() {
        ActivityController controller = controllerFor((methodName, args) ->
            "validateActivityTime".equals(methodName) ? Boolean.TRUE : null);

        AjaxResult result = controller.validateTime("2026-10-01", "2026-10-03");

        assertEquals(200, result.get("code"));
        assertEquals(Boolean.TRUE, result.get("data"));
    }

    @Test
    public void activityMutationsRequireEditPermission() throws Exception {
        assertPermission("updateStatus", new Class<?>[]{Long.class}, "system:activity:edit");
        assertPermission("cancelActivity", new Class<?>[]{Long.class}, "system:activity:edit");
        assertPermission("batchUpdateStatus", new Class<?>[0], "system:activity:edit");
        assertPermission("getByTimeRange", new Class<?>[]{String.class, String.class},
                "@ss.hasPermi('system:activity:list')");
        assertPermission("validateTime", new Class<?>[]{String.class, String.class},
                "@ss.hasAnyPermi('system:activity:add,system:activity:edit')");
    }

    private void assertPermission(String methodName, Class<?>[] parameterTypes, String expected) throws Exception {
        PreAuthorize permission = ActivityController.class.getMethod(methodName, parameterTypes)
                .getAnnotation(PreAuthorize.class);
        assertNotNull(permission);
        assertEquals(expected.startsWith("@ss.") ? expected : "@ss.hasPermi('" + expected + "')",
                permission.value());
    }

    private ActivityController controllerFor(ServiceCall call) {
        InvocationHandler handler = (instance, method, args) -> call.invoke(method.getName(), args);
        IActivityService service = IActivityService.class.cast(Proxy.newProxyInstance(
                IActivityService.class.getClassLoader(), new Class<?>[]{IActivityService.class}, handler));
        return new ActivityController(service);
    }

    private interface ServiceCall {
        Object invoke(String methodName, Object[] args);
    }
}
