package ru.intertrust.cm.core.business.schedule;

import java.text.ParseException;

import javax.ejb.EJBContext;
import javax.ejb.SessionContext;

import org.junit.Assert;
import org.junit.Test;

import ru.intertrust.cm.core.business.api.schedule.ScheduleTask;
import ru.intertrust.cm.core.business.api.schedule.ScheduleTaskHandle;
import ru.intertrust.cm.core.business.api.schedule.ScheduleTaskParameters;
import ru.intertrust.cm.core.business.shedule.ScheduleTaskLoaderImpl;

public class TestSchedule {

    private class ScheduleTaskLoaderImplInt extends ScheduleTaskLoaderImpl {
        public String getDefaultParametersInt(ScheduleTask configuration) {
            return getDefaultParameters(configuration);
        }
    }

    @ScheduleTask(name = "TestScheduleWithDefaultParams", minute = "*/1", configClass = DefaultParameter.class)
    public class TestScheduleWithDefaultParams implements ScheduleTaskHandle {

        @Override
        public String execute(EJBContext ejbContext, SessionContext sessionContext, ScheduleTaskParameters parameters) {
            return "OK";
        }

    }

    @Test
    public void testLoadDefaultParameters() throws ParseException {
        String etalon = "<scheduleTaskConfig>\n";
        etalon += "   <parameters class=\"ru.intertrust.cm.core.business.schedule.Parameters\">\n";
        etalon += "      <parameters result=\"OK\"/>\n";
        etalon += "   </parameters>\n";
        etalon += "</scheduleTaskConfig>";

        ScheduleTaskLoaderImplInt sheduleTaskLoader = new ScheduleTaskLoaderImplInt();

        ScheduleTask configuration = TestScheduleWithDefaultParams.class.getAnnotation(ScheduleTask.class);

        String result = sheduleTaskLoader.getDefaultParametersInt(configuration);
        Assert.assertTrue("Check schedulr param", result.equals(etalon));
    }
}
