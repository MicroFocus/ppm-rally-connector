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
import com.ppm.integration.agilesdk.connector.agilecentral.model.Backlog;
import com.ppm.integration.agilesdk.connector.agilecentral.model.Defect;
import com.ppm.integration.agilesdk.connector.agilecentral.model.HierarchicalRequirement;
import com.ppm.integration.agilesdk.connector.agilecentral.model.Iteration;
import com.ppm.integration.agilesdk.connector.agilecentral.model.Project;
import com.ppm.integration.agilesdk.connector.agilecentral.model.Subscription;
import com.ppm.integration.agilesdk.connector.agilecentral.model.Testset;
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
    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    private final Logger logger = Logger.getLogger(this.getClass());

    protected synchronized String convertDate(Date date) {
        try {
            return dateFormat.format(date);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return "";

    }

    @Override public List<Field> getMappingConfigurationFields(ValueSet paramValueSet) {
        return Arrays.asList(new Field[] {new PlainText(Constants.KEY_USERNAME, "USERNAME", "dan@acme.com", true),
                new PasswordText(Constants.KEY_PASSWORD, "PASSWORD", "Release!", true),
                new RallyEntityDropdown(Constants.KEY_SUBSCRIPTION, "SUBSCRIPTION", true) {

                    @Override public List<String> getDependencies() {
                        return Arrays.asList(new String[] {Constants.KEY_USERNAME, Constants.KEY_PASSWORD});
                    }

                    @Override public List<Option> getDynamicalOptions(ValueSet values) {
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

            @Override public List<String> getDependencies() {
                return Arrays.asList(new String[] {Constants.KEY_SUBSCRIPTION});
            }

            @Override public List<Option> getDynamicalOptions(ValueSet values) {
                Config config = new Config();
                config.setProxy(values.get(Constants.KEY_PROXY_HOST), values.get(Constants.KEY_PROXY_PORT));
                config.setBasicAuthorization(values.get(Constants.KEY_USERNAME), values.get(Constants.KEY_PASSWORD));
                RallyClient rallyClient = new RallyClient(values.get(Constants.KEY_BASE_URL), config);

                List<Option> options = new LinkedList<Option>();
                for (Workspace w : rallyClient.getWorkspaces(values.get(Constants.KEY_SUBSCRIPTION))) {
                    options.add(new Option(w.getId(), w.getName()));
                }

                return options;
            }

        }, new RallyEntityDropdown(Constants.KEY_PROJECT, "PROJECT", false) {

            @Override public List<String> getDependencies() {
                return Arrays.asList(new String[] {Constants.KEY_WORKSPACE});
            }

            @Override public List<Option> getDynamicalOptions(ValueSet values) {
                Config config = new Config();
                config.setProxy(values.get(Constants.KEY_PROXY_HOST), values.get(Constants.KEY_PROXY_PORT));
                config.setBasicAuthorization(values.get(Constants.KEY_USERNAME), values.get(Constants.KEY_PASSWORD));
                RallyClient rallyClient = new RallyClient(values.get(Constants.KEY_BASE_URL), config);

                List<Option> options = new LinkedList<Option>();
                for (Project p : rallyClient.getProjects(values.get(Constants.KEY_WORKSPACE))) {
                    options.add(new Option(p.getId(), p.getName()));
                }

                return options;
            }

        }, new RallyEntityDropdown(Constants.KEY_DATA_DETAIL_LEVEL, "DATA_DETAIL_LEVEL", true) {

            @Override public List<String> getDependencies() {
                return Arrays.asList(new String[] {});
            }

            @Override public List<Option> getDynamicalOptions(ValueSet values) {
                List<Option> options = new LinkedList<Option>();
                        options.add(new Option(Constants.KEY_DATA_DETAIL_LEVEL_ITERATION, "Iteration"));
                        options.add(new Option(Constants.KEY_DATA_DETAIL_LEVEL_USERSTORY, "Work Item"));
                return options;
            }
                }, new LineBreaker(),
                new CheckBox(Constants.KEY_REMOVE_ITEMS, "IS_REMOVE_ITEMS_WITHOUT_TIMELOG", true),
                new LineBreaker()});
    }

    @Override public List<ExternalWorkItem> getExternalWorkItems(TimeSheetIntegrationContext context,
            final ValueSet values)
    {
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
        HashMap<String, List<TimeEntryValue>> timeEntryValuesHM = rallyClient.getTimeEntryValue();
        HashMap<String, List<HierarchicalRequirement>> hierarchicalRequirementHM =
                rallyClient.getHierarchicalRequirement();
        HashMap<String, List<Defect>> defectHM = rallyClient.getDefect();
        HashMap<String, List<Testset>> testsetHM = rallyClient.getTestset();
        for (final Workspace workspace : workspaces) {

            if (!values.get(Constants.KEY_WORKSPACE).isEmpty() && !workspace.getId()
                    .equals(values.get(Constants.KEY_WORKSPACE))) {
                continue;
            }

            List<Project> projects = rallyClient.getProjects(workspace.getId());
            for (final Project project : projects) {

                if (!values.get(Constants.KEY_PROJECT).isEmpty() && !project.getId()
                        .equals(values.get(Constants.KEY_PROJECT))) {
                    continue;
                }
                String tag = "";
                if (values.get(Constants.KEY_DATA_DETAIL_LEVEL)
                        .equals(Constants.KEY_DATA_DETAIL_LEVEL_USERSTORY)) {
                    // work_items
                    List<Backlog> backlogs = new ArrayList<>();
                    backlogs.addAll(rallyClient.getHierarchicalRequirements());
                    backlogs.addAll(rallyClient.getDefects());
                    backlogs.addAll(rallyClient.getTestsets());

                    for (Backlog backlog : backlogs) {
                        if (backlog.getType().equals("Defect")) {
                            tag = "DE";
                        } else if (backlog.getType().equals("TestSet")) {
                            tag = "TS";
                        } else {
                            tag = "US";
                        }
                        if (!backlog.getProjectUUID().equals(project.getUUID())) {
                            continue;
                        }

                        List<TimeEntryValue> timeEntryValues = new ArrayList<>();
                        String backlogUUID = backlog.getUUID();
                        if (timeEntryItemsHM.containsKey(backlogUUID)) {
                            List<TimeEntryItem> timeEntryItems = timeEntryItemsHM.get(backlogUUID);
                            String timeEntryItemUUID = "";
                            for (TimeEntryItem timeEntryItem : timeEntryItems) {
                                timeEntryItemUUID = timeEntryItem.getUUID();
                                if (timeEntryValuesHM.containsKey(timeEntryItemUUID)) {
                                    timeEntryValues.addAll(timeEntryValuesHM.get(timeEntryItemUUID));
                                }

                            }
                        }

                        HashMap<String, Integer> hms = getTimeSheetData(startDate, endDate, timeEntryValues);
                        if (values.get(Constants.KEY_REMOVE_ITEMS).equals("true") && hms.size() == 0) {
                            continue;
                        }
                        items.add(new RallyExternalWorkItem(tag, project.getName(), backlog.getName(),
                                hms, values, startDate, endDate));
                    }
                } else {
                    // iteration
                    List<Iteration> iterations = rallyClient.getIterations(project.getId());
                    for (final Iteration iteration : iterations) {
                        String iterationUUID = iteration.getUUID();
                        List<Backlog> backlogs = new ArrayList<>();

                        if (hierarchicalRequirementHM.containsKey(iterationUUID)) {
                            backlogs.addAll(hierarchicalRequirementHM.get(iterationUUID));
                        }
                        if (defectHM.containsKey(iterationUUID)) {
                            backlogs.addAll(defectHM.get(iterationUUID));
                        }
                        if (testsetHM.containsKey(iterationUUID)) {
                            backlogs.addAll(testsetHM.get(iterationUUID));
                        }

                        List<TimeEntryValue> timeEntryValues = new ArrayList<>();
                        for (Backlog backlog : backlogs) {

                            if (!backlog.getProjectUUID().equals(project.getUUID())) {
                                continue;
                            }

                            String backlogUUID = backlog.getUUID();
                            if (timeEntryItemsHM.containsKey(backlogUUID)) {
                                List<TimeEntryItem> timeEntryItems = timeEntryItemsHM.get(backlogUUID);
                                String timeEntryItemUUID = "";
                                for (TimeEntryItem timeEntryItem : timeEntryItems) {
                                    timeEntryItemUUID = timeEntryItem.getUUID();
                                    if (timeEntryValuesHM.containsKey(timeEntryItemUUID)) {
                                        timeEntryValues.addAll(timeEntryValuesHM.get(timeEntryItemUUID));
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

    private HashMap<String, Integer> getTimeSheetData(Date startDate, Date endDate,
            List<TimeEntryValue> timeEntryValues)
    {
        HashMap<String, Integer> hms = new HashMap<String, Integer>();

        for (TimeEntryValue timeEntryValue : timeEntryValues) {
            String date = timeEntryValue.getDateVal().split("T")[0];

            int hours = timeEntryValue.getHours();
            if (!hms.containsKey(date)) {
                hms.put(date, hours);
            } else {
                int hoursSum = hms.get(date) + hours;
                hms.put(date, hoursSum);
            }
        }

        return hms;
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
                ValueSet values, Date startDate, Date endDate)
        {
            this.tag = tag;
            this.project = project;
            this.iteration = iteration;
            this.startDate = startDate;
            this.endDate = endDate;

            effortList.putAll(hms);
        }

        @Override public String getName() {
            return this.tag + " > " + this.iteration + "(" + this.project + ")";
        }

        @Override public Double getTotalEffort() {
            return null;
        }

        @Override public String getErrorMessage() {
            return errorMessage;
        }

        @Override public ExternalWorkItemEffortBreakdown getEffortBreakDown() {
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

}