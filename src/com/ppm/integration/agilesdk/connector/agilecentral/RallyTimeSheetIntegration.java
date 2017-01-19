
package com.ppm.integration.agilesdk.connector.agilecentral;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import com.hp.ppm.tm.model.TimeSheet;
import com.ppm.integration.agilesdk.ValueSet;
import com.ppm.integration.agilesdk.connector.agilecentral.model.HierarchicalRequirement;
import com.ppm.integration.agilesdk.connector.agilecentral.model.Iteration;
import com.ppm.integration.agilesdk.connector.agilecentral.model.Project;
import com.ppm.integration.agilesdk.connector.agilecentral.model.Subscription;
import com.ppm.integration.agilesdk.connector.agilecentral.model.Task;
import com.ppm.integration.agilesdk.connector.agilecentral.model.TimeEntryItem;
import com.ppm.integration.agilesdk.connector.agilecentral.model.TimeEntryValue;
import com.ppm.integration.agilesdk.connector.agilecentral.model.Workspace;
import com.ppm.integration.agilesdk.connector.agilecentral.ui.RallyEntityDropdown;
import com.ppm.integration.agilesdk.tm.ExternalWorkItem;
import com.ppm.integration.agilesdk.tm.ExternalWorkItemEffortBreakdown;
import com.ppm.integration.agilesdk.tm.TimeSheetIntegration;
import com.ppm.integration.agilesdk.tm.TimeSheetIntegrationContext;
import com.ppm.integration.agilesdk.ui.CheckBox;
import com.ppm.integration.agilesdk.ui.Field;
import com.ppm.integration.agilesdk.ui.LineBreaker;
import com.ppm.integration.agilesdk.ui.PasswordText;
import com.ppm.integration.agilesdk.ui.PlainText;

public class RallyTimeSheetIntegration extends TimeSheetIntegration {
    private final Logger logger = Logger.getLogger(this.getClass());

    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    protected synchronized String convertDate(Date date) {
        try {
            return dateFormat.format(date);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return "";

    }

    @Override
    public List<Field> getMappingConfigurationFields(ValueSet paramValueSet) {
        return Arrays.asList(new Field[] {
                new PlainText(Constants.KEY_USERNAME, "USERNAME", "dan@acme.com", true),
                new PasswordText(Constants.KEY_PASSWORD, "PASSWORD", "Release!", true),
                new RallyEntityDropdown(Constants.KEY_SUBSCRIPTION, "SUBSCRIPTION", true) {

                    @Override
                    public List<String> getDependencies() {
                        return Arrays.asList(new String[] {Constants.KEY_USERNAME, Constants.KEY_PASSWORD});
                    }

                    @Override
                    public List<Option> getDynamicalOptions(ValueSet values) {
                        Config config = new Config();
                        config.setProxy(values.get(Constants.KEY_PROXY_HOST), values.get(Constants.KEY_PROXY_PORT));
                        config.setBasicAuthorization(values.get(Constants.KEY_USERNAME),
                                values.get(Constants.KEY_PASSWORD));
                        RallyClient rallyClient = new RallyClient(values.get(Constants.KEY_BASE_URL), config);

                        Subscription subscription = rallyClient.getSubscription();
                        return Arrays.asList(new Option[] {new Option(subscription.getId(), subscription.getName())});
                    }

                },
                new RallyEntityDropdown(Constants.KEY_WORKSPACE, "WORKSPACE", false) {

                    @Override
                    public List<String> getDependencies() {
                        return Arrays.asList(new String[] {Constants.KEY_SUBSCRIPTION});
                    }

                    @Override
                    public List<Option> getDynamicalOptions(ValueSet values) {
                        Config config = new Config();
                        config.setProxy(values.get(Constants.KEY_PROXY_HOST), values.get(Constants.KEY_PROXY_PORT));
                        config.setBasicAuthorization(values.get(Constants.KEY_USERNAME),
                                values.get(Constants.KEY_PASSWORD));
                        RallyClient rallyClient = new RallyClient(values.get(Constants.KEY_BASE_URL), config);

                        List<Option> options = new LinkedList<Option>();
                        for (Workspace w : rallyClient.getWorkspaces(values.get(Constants.KEY_SUBSCRIPTION))) {
                            options.add(new Option(w.getId(), w.getName()));
                        }

                        return options;
                    }

                },
                new RallyEntityDropdown(Constants.KEY_PROJECT, "PROJECT", false) {

                    @Override
                    public List<String> getDependencies() {
                        return Arrays.asList(new String[] {Constants.KEY_WORKSPACE});
                    }

                    @Override
                    public List<Option> getDynamicalOptions(ValueSet values) {
                        Config config = new Config();
                        config.setProxy(values.get(Constants.KEY_PROXY_HOST), values.get(Constants.KEY_PROXY_PORT));
                        config.setBasicAuthorization(values.get(Constants.KEY_USERNAME),
                                values.get(Constants.KEY_PASSWORD));
                        RallyClient rallyClient = new RallyClient(values.get(Constants.KEY_BASE_URL), config);

                        List<Option> options = new LinkedList<Option>();
                        for (Project p : rallyClient.getProjects(values.get(Constants.KEY_WORKSPACE))) {
                            options.add(new Option(p.getId(), p.getName()));
                        }

                        return options;
                    }

                }, new RallyEntityDropdown(Constants.KEY_DATA_DETAIL_LEVEL, "DATA_DETAIL_LEVEL", true) {

                    @Override
                    public List<String> getDependencies() {
                        return Arrays.asList(new String[] {});
                    }

                    @Override
                    public List<Option> getDynamicalOptions(ValueSet values) {
                        List<Option> options = new LinkedList<Option>();
                        options.add(new Option(Constants.KEY_DATA_DETAIL_LEVEL_ITERATION, "ITERATION"));
                        options.add(new Option(Constants.KEY_DATA_DETAIL_LEVEL_USERSTORY, "USER_STORY"));
                        options.add(new Option(Constants.KEY_DATA_DETAIL_LEVEL_TASK, "TASK"));
                        return options;
                    }
                }, new LineBreaker(),
                new CheckBox(Constants.KEY_REMOVE_ITEMS, "IS_REMOVE_ITEMS_WITHOUT_TIMELOG", false), new LineBreaker()});
    }

    @Override
    public List<ExternalWorkItem> getExternalWorkItems(TimeSheetIntegrationContext context, final ValueSet values) {
        final List<ExternalWorkItem> items = getExternalWorkItemsByTasks(context, values);
        return items;
    }

    private List<ExternalWorkItem> getExternalWorkItemsByTasks(TimeSheetIntegrationContext context,
            final ValueSet values)
    {
        final List<ExternalWorkItem> items = Collections.synchronizedList(new LinkedList<ExternalWorkItem>());

        Config config = new Config();
        config.setProxy(values.get(Constants.KEY_PROXY_HOST), values.get(Constants.KEY_PROXY_PORT));
        config.setBasicAuthorization(values.get(Constants.KEY_USERNAME), values.get(Constants.KEY_PASSWORD));
        final RallyClient rallyClient = new RallyClient(values.get(Constants.KEY_BASE_URL), config);

        Subscription subscription = rallyClient.getSubscription();
        List<Workspace> workspaces = rallyClient.getWorkspaces(subscription.getId());

        TimeSheet timeSheet = context.currentTimeSheet();

        final Date startDate = timeSheet.getPeriodStartDate().toGregorianCalendar().getTime();
        final Date endDate = timeSheet.getPeriodEndDate().toGregorianCalendar().getTime();

        HashMap<String, List<TimeEntryItem>> timeEntryItemsHM = rallyClient.getTimeEntryItem();

        for (final Workspace workspace : workspaces) {

            if (!values.get(Constants.KEY_WORKSPACE).isEmpty()
                    && !workspace.getId().equals(values.get(Constants.KEY_WORKSPACE))) {
                continue;
            }

            List<Project> projects = rallyClient.getProjects(workspace.getId());
            for (final Project project : projects) {

                if (!values.get(Constants.KEY_PROJECT).isEmpty()
                        && !project.getId().equals(values.get(Constants.KEY_PROJECT))) {
                    continue;
                }

                if (values.get(Constants.KEY_DATA_DETAIL_LEVEL).equals(Constants.KEY_DATA_DETAIL_LEVEL_TASK)) {
                    // all task
                    String tag = "TA";
                    List<HierarchicalRequirement> hierarchicalRequirements = rallyClient.getHierarchicalRequirements();
                    for (HierarchicalRequirement hierarchicalRequirement : hierarchicalRequirements) {

                        if (!hierarchicalRequirement.getProjectUUID().equals(project.getUUID())) {
                            continue;
                        }

                        List<Task> tasks = rallyClient.getTasks(hierarchicalRequirement.getId());
                        for (Task task : tasks) {
                            List<TimeEntryValue> timeEntryValues = new ArrayList<>();
                            if (timeEntryItemsHM.containsKey(task.getUUID())) {
                                List<TimeEntryItem> timeEntryItems = timeEntryItemsHM.get(task.getUUID());
                                for (TimeEntryItem timeEntryItem : timeEntryItems) {
                                    List<TimeEntryValue> thisTimeEntryValues =
                                            rallyClient.getTimeEntryValue(timeEntryItem.getId());
                                    timeEntryValues.addAll(thisTimeEntryValues);
                                }
                            }

                            HashMap<String, Integer> hms = getTimeSheetData(startDate, endDate, timeEntryValues);
                            if (values.get(Constants.KEY_REMOVE_ITEMS).equals("true") && hms.size() == 0) {
                                continue;
                            }

                            items.add(new RallyExternalWorkItem(tag, project.getName(), task.getName(), hms, values,
                                    startDate, endDate));
                        }
                    }
                } else if (values.get(Constants.KEY_DATA_DETAIL_LEVEL)
                        .equals(Constants.KEY_DATA_DETAIL_LEVEL_USERSTORY)) {
                    // user story
                    String tag = "US";
                    List<HierarchicalRequirement> hierarchicalRequirements = rallyClient.getHierarchicalRequirements();
                    for (HierarchicalRequirement hierarchicalRequirement : hierarchicalRequirements) {

                        if (!hierarchicalRequirement.getProjectUUID().equals(project.getUUID())) {
                            continue;
                        }

                        List<Task> tasks = rallyClient.getTasks(hierarchicalRequirement.getId());
                        List<TimeEntryValue> timeEntryValues = new ArrayList<>();
                        for (Task task : tasks) {
                            if (timeEntryItemsHM.containsKey(task.getUUID())) {
                                List<TimeEntryItem> timeEntryItems = timeEntryItemsHM.get(task.getUUID());
                                for (TimeEntryItem timeEntryItem : timeEntryItems) {
                                    List<TimeEntryValue> thisTimeEntryValues =
                                            rallyClient.getTimeEntryValue(timeEntryItem.getId());
                                    timeEntryValues.addAll(thisTimeEntryValues);
                                }
                            }
                        }

                        HashMap<String, Integer> hms = getTimeSheetData(startDate, endDate, timeEntryValues);
                        if (values.get(Constants.KEY_REMOVE_ITEMS).equals("true") && hms.size() == 0) {
                            continue;
                        }
                        items.add(new RallyExternalWorkItem(tag, project.getName(), hierarchicalRequirement.getName(),
                                hms, values, startDate, endDate));
                    }
                } else {
                    // iteration
                    String tag = "";
                    List<Iteration> iterations = rallyClient.getIterations(project.getId());
                    for (final Iteration iteration : iterations) {

                        List<HierarchicalRequirement> hierarchicalRequirements = iteration.getHierarchicalRequirement();
                        List<TimeEntryValue> timeEntryValues = new ArrayList<>();
                        for (HierarchicalRequirement hierarchicalRequirement : hierarchicalRequirements) {

                            if (!hierarchicalRequirement.getProjectUUID().equals(project.getUUID())) {
                                continue;
                            }

                            List<Task> tasks = rallyClient.getTasks(hierarchicalRequirement.getId());
                            for (Task task : tasks) {
                                if (timeEntryItemsHM.containsKey(task.getUUID())) {
                                    List<TimeEntryItem> timeEntryItems = timeEntryItemsHM.get(task.getUUID());
                                    for (TimeEntryItem timeEntryItem : timeEntryItems) {
                                        List<TimeEntryValue> thisTimeEntryValues =
                                                rallyClient.getTimeEntryValue(timeEntryItem.getId());
                                        timeEntryValues.addAll(thisTimeEntryValues);
                                    }
                                }
                            }
                        }

                        HashMap<String, Integer> hms = getTimeSheetData(startDate, endDate, timeEntryValues);
                        if (values.get(Constants.KEY_REMOVE_ITEMS).equals("true") && hms.size() == 0) {
                            continue;
                        }
                        items.add(new RallyExternalWorkItem(tag, project.getName(), iteration.getName(), hms, values,
                                startDate, endDate));
                    }
                }
            }

        }
        return items;
    }

    private class RallyExternalWorkItem extends ExternalWorkItem {

        final String tag;

        final String project;

        final String iteration;

        String errorMessage = null;

        Date startDate;

        Date endDate;

        HashMap<String, Integer> effortList = new HashMap<>();

        public RallyExternalWorkItem(String tag, String project, String iteration, HashMap<String, Integer> hms,
                ValueSet values, Date startDate, Date endDate) {
            this.tag = tag;
            this.project = project;
            this.iteration = iteration;
            this.startDate = startDate;
            this.endDate = endDate;

            effortList.putAll(hms);
        }

        @Override
        public String getName() {
            return this.tag + " > " + this.iteration + "(" + this.project + ")";
        }

        @Override
        public Double getTotalEffort() {
            return null;
        }

        @Override
        public String getErrorMessage() {
            return errorMessage;
        }

        @Override
        public ExternalWorkItemEffortBreakdown getEffortBreakDown() {
            ExternalWorkItemEffortBreakdown effortBreakdown = new ExternalWorkItemEffortBreakdown();

            int numOfWorkDays = getDaysDiffNumber(startDate, endDate);
            if (numOfWorkDays > 0) {
                Calendar calendar = new GregorianCalendar();
                calendar.setTime(startDate);

                for (int i = 0; i < numOfWorkDays; i++) {
                    double effort = 0;
                    if (effortList.containsKey(convertDate(calendar.getTime()))) {
                        effort = effortList.get(convertDate(calendar.getTime()));
                    }
                    effortBreakdown.addEffort(calendar.getTime(), effort);
                    calendar.add(Calendar.DAY_OF_MONTH, 1);
                }
            }
            return effortBreakdown;
        }

        private int getDaysDiffNumber(Date startDate, Date endDate) {
            Calendar start = new GregorianCalendar();
            start.setTime(startDate);

            Calendar end = new GregorianCalendar();
            end.setTime(endDate);
            end.set(Calendar.HOUR_OF_DAY, 23);
            end.set(Calendar.MINUTE, 59);
            end.set(Calendar.SECOND, 59);
            end.set(Calendar.MILLISECOND, 999);

            Calendar dayDiff = Calendar.getInstance();
            dayDiff.setTime(startDate);
            int diffNumber = 0;
            while (dayDiff.before(end)) {
                diffNumber++;
                dayDiff.add(Calendar.DAY_OF_MONTH, 1);
            }
            return diffNumber;
        }
    }

    private HashMap<String, Integer> getTimeSheetData(Date startDate, Date endDate, List<TimeEntryValue> timeEntryValues)
    {
        HashMap<String, Integer> hms = new HashMap<String, Integer>();

        for (TimeEntryValue timeEntryValue : timeEntryValues) {
            Date date = timeEntryValue.getDateVal();
            if (date.getTime() <= startDate.getTime() || date.getTime() >= endDate.getTime()) {
                continue;
            }

            int hours = timeEntryValue.getHours();
            if (!hms.containsKey(convertDate(date))) {
                hms.put(convertDate(date), hours);
            } else {
                int hoursSum = hms.get(convertDate(date)) + hours;
                hms.put(convertDate(date), hoursSum);
            }
        }

        return hms;
    }

}