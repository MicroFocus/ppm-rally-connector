
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

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;

import com.hp.ppm.tm.model.TimeSheet;
import com.ppm.integration.agilesdk.ValueSet;
import com.ppm.integration.agilesdk.connector.agilecentral.model.Project;
import com.ppm.integration.agilesdk.connector.agilecentral.model.Subscription;
import com.ppm.integration.agilesdk.connector.agilecentral.model.Task;
import com.ppm.integration.agilesdk.connector.agilecentral.model.TimeEntryItem;
import com.ppm.integration.agilesdk.connector.agilecentral.model.TimeEntryValue;
import com.ppm.integration.agilesdk.connector.agilecentral.model.Workspace;
import com.ppm.integration.agilesdk.connector.agilecentral.model.portfolio.PortfolioFeature;
import com.ppm.integration.agilesdk.connector.agilecentral.model.portfolio.PortfolioInitiative;
import com.ppm.integration.agilesdk.connector.agilecentral.model.portfolio.PortfolioItem;
import com.ppm.integration.agilesdk.connector.agilecentral.model.portfolio.PortfolioTheme;
import com.ppm.integration.agilesdk.connector.agilecentral.ui.RallyEntityDropdown;
import com.ppm.integration.agilesdk.tm.ExternalWorkItem;
import com.ppm.integration.agilesdk.tm.ExternalWorkItemEffortBreakdown;
import com.ppm.integration.agilesdk.tm.TimeSheetIntegration;
import com.ppm.integration.agilesdk.tm.TimeSheetIntegrationContext;
import com.ppm.integration.agilesdk.ui.Field;
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

    @Override
    public List<Field> getMappingConfigurationFields(ValueSet paramValueSet) {
        return Arrays.asList(new Field[] {new PlainText(Constants.KEY_USERNAME, "USERNAME", "dan@acme.com", true),
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

                }, new RallyEntityDropdown(Constants.KEY_WORKSPACE, "WORKSPACE", true) {

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
                        options.add(new Option(Constants.KEY_ALL_ITEMS, "All WorkSpaces"));
                        for (Workspace w : rallyClient.getWorkspaces(values.get(Constants.KEY_SUBSCRIPTION))) {
                            options.add(new Option(w.getId(), w.getName()));
                        }

                        return options;
                    }

                }, new RallyEntityDropdown(Constants.KEY_PROJECT, "PROJECT", true) {

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
                        options.add(new Option(Constants.KEY_ALL_ITEMS, "All Projects"));
                        for (Project p : rallyClient.getProjects(values.get(Constants.KEY_WORKSPACE))) {
                            int count = p.getChildrenCount();
                            if (count > 0) {
                                options.add(new Option(p.getId(), Constants.KEY_PARENT_PROJECT_LABEL + " "
                                        + p.getName()));
                            } else {
                                options.add(new Option(p.getId(), p.getName()));
                            }
                        }

                        return options;
                    }

                }, new RallyEntityDropdown(Constants.KEY_DATA_DETAIL_LEVEL, "Group By", true) {

                    @Override
                    public List<String> getDependencies() {
                        return Arrays.asList(new String[] {Constants.KEY_PROJECT});
                    }

                    @Override
                    public List<Option> getDynamicalOptions(ValueSet values) {
                        Config config = new Config();
                        config.setProxy(values.get(Constants.KEY_PROXY_HOST), values.get(Constants.KEY_PROXY_PORT));
                        config.setBasicAuthorization(values.get(Constants.KEY_USERNAME),
                                values.get(Constants.KEY_PASSWORD));
                        RallyClient rallyClient = new RallyClient(values.get(Constants.KEY_BASE_URL), config);

                        List<Option> options = new LinkedList<Option>();
                        options.add(new Option(Constants.KEY_DATA_DETAIL_LEVEL_USERSTORY, "Work Item"));
                        options.add(new Option(Constants.KEY_DATA_DETAIL_LEVEL_ITERATION, "Iteration"));
                        options.add(new Option(Constants.KEY_DATA_DETAIL_LEVEL_RELEASE, "Release"));
                        if (values.get(Constants.KEY_PROJECT).equals(Constants.KEY_ALL_ITEMS)) {
                            options.add(new Option(Constants.KEY_DATA_DETAIL_LEVEL_FEATURE,
                                    Constants.KEY_DATA_DETAIL_LEVEL_FEATURE));
                            options.add(new Option(Constants.KEY_DATA_DETAIL_LEVEL_INITIATIVE,
                                    Constants.KEY_DATA_DETAIL_LEVEL_INITIATIVE));
                            options.add(new Option(Constants.KEY_DATA_DETAIL_LEVEL_THEME,
                                    Constants.KEY_DATA_DETAIL_LEVEL_THEME));
                        } else {
                            HashMap<String, List<PortfolioItem>> hms =
                                    rallyClient.getPortfolioItems(values.get(Constants.KEY_PROJECT));
                            for (String key : hms.keySet()) {
                                options.add(new Option(key, key));
                            }
                        }

                        return options;
                    }
                }});
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

        // Time Entry
        HashMap<String, List<TimeEntryItem>> timeEntryItems = rallyClient.getTimeEntryItem();
        HashMap<String, List<TimeEntryValue>> timeEntryValues = rallyClient.getTimeEntryValue();
        HashMap<String, JSONObject> artifacts = new HashMap<>();
        if (!values.get(Constants.KEY_DATA_DETAIL_LEVEL).equals(Constants.KEY_DATA_DETAIL_LEVEL_USERSTORY)) {
            artifacts.putAll(rallyClient.getAllData());
        }

        for (final Workspace workspace : workspaces) {

            if (values.get(Constants.KEY_WORKSPACE).equals(Constants.KEY_ALL_ITEMS)) {

            } else if (!workspace.getId().equals(values.get(Constants.KEY_WORKSPACE))) {
                continue;
            }

            List<Project> projects = rallyClient.getProjects(workspace.getId());
            for (final Project project : projects) {

                if (values.get(Constants.KEY_PROJECT).equals(Constants.KEY_ALL_ITEMS)) {

                } else if (!project.getId().equals(values.get(Constants.KEY_PROJECT))) {
                    continue;
                }

                // To store Name_of_ITEM:TimeEntryValue
                HashMap<String, List<TimeEntryValue>> hms = new HashMap<>();

                switch (values.get(Constants.KEY_DATA_DETAIL_LEVEL)) {
                    case Constants.KEY_DATA_DETAIL_LEVEL_USERSTORY:

                        for (String key : timeEntryItems.keySet()) {

                            for (TimeEntryItem timeEntryItem : timeEntryItems.get(key)) {
                                // project
                                if (!timeEntryItem.getProjectId().equals(project.getId())) {
                                    continue;
                                }
                                // get name of Work Item
                                String name = timeEntryItem.getWorkProductDisplayString();
                                if (timeEntryValues.containsKey(timeEntryItem.getUUID())) {
                                    List<TimeEntryValue> timeEntryValue = timeEntryValues.get(timeEntryItem.getUUID());
                                    if (hms.containsKey(name)) {
                                        List<TimeEntryValue> thisTimeEntryValue = hms.get(name);
                                        thisTimeEntryValue.addAll(timeEntryValue);
                                        hms.put(name, thisTimeEntryValue);
                                    } else {
                                        List<TimeEntryValue> thisTimeEntryValue = new ArrayList<>();
                                        thisTimeEntryValue.addAll(timeEntryValue);
                                        hms.put(name, thisTimeEntryValue);
                                    }
                                }
                            }
                        }

                        break;
                    case Constants.KEY_DATA_DETAIL_LEVEL_ITERATION:

                        for (String key : timeEntryItems.keySet()) {
                            // get Task
                            Task task = new Task(artifacts.get(key));
                            if (!task.getProjectId().equals(project.getId())) {
                                continue;
                            }
                            // get name of Iteration
                            String name = task.getIterationName();

                            for (TimeEntryItem timeEntryItem : timeEntryItems.get(key)) {
                                if (timeEntryValues.containsKey(timeEntryItem.getUUID())) {
                                    List<TimeEntryValue> timeEntryValue = timeEntryValues.get(timeEntryItem.getUUID());
                                    if (hms.containsKey(name)) {
                                        List<TimeEntryValue> thisTimeEntryValue = hms.get(name);
                                        thisTimeEntryValue.addAll(timeEntryValue);
                                        hms.put(name, thisTimeEntryValue);
                                    } else {
                                        List<TimeEntryValue> thisTimeEntryValue = new ArrayList<>();
                                        thisTimeEntryValue.addAll(timeEntryValue);
                                        hms.put(name, thisTimeEntryValue);
                                    }
                                }

                            }
                        }

                        break;
                    case Constants.KEY_DATA_DETAIL_LEVEL_RELEASE:

                        for (String key : timeEntryItems.keySet()) {
                            // get Task
                            Task task = new Task(artifacts.get(key));
                            if (!task.getProjectId().equals(project.getId())) {
                                continue;
                            }
                            // get name of Release
                            String name = task.getReleaseName();

                            for (TimeEntryItem timeEntryItem : timeEntryItems.get(key)) {
                                if (timeEntryValues.containsKey(timeEntryItem.getUUID())) {
                                    List<TimeEntryValue> timeEntryValue = timeEntryValues.get(timeEntryItem.getUUID());
                                    if (hms.containsKey(name)) {
                                        List<TimeEntryValue> thisTimeEntryValue = hms.get(name);
                                        thisTimeEntryValue.addAll(timeEntryValue);
                                        hms.put(name, thisTimeEntryValue);
                                    } else {
                                        List<TimeEntryValue> thisTimeEntryValue = new ArrayList<>();
                                        thisTimeEntryValue.addAll(timeEntryValue);
                                        hms.put(name, thisTimeEntryValue);
                                    }
                                }

                            }
                        }

                        break;
                    case Constants.KEY_DATA_DETAIL_LEVEL_FEATURE:

                        for (String key : timeEntryItems.keySet()) {
                            // get Task
                            Task task = new Task(artifacts.get(key));
                            if (!task.getProjectId().equals(project.getId())) {
                                continue;
                            }
                            String type = task.getWorkProductType();
                            if (!type.equals("HierarchicalRequirement")) {
                                continue;
                            }

                            // get name of Feature
                            if (!artifacts.get(task.getWorkProductUUID()).getJSONObject("Feature").isNullObject()) {
                                String featureUUID =
                                        artifacts.get(task.getWorkProductUUID()).getJSONObject("Feature")
                                                .getString("_refObjectUUID");
                                PortfolioFeature feature = new PortfolioFeature(artifacts.get(featureUUID));
                                if (!feature.getProjectID().equals(project.getId())) {
                                    continue;
                                }
                                String name = feature.getName();

                                for (TimeEntryItem timeEntryItem : timeEntryItems.get(key)) {
                                    if (timeEntryValues.containsKey(timeEntryItem.getUUID())) {
                                        List<TimeEntryValue> timeEntryValue =
                                                timeEntryValues.get(timeEntryItem.getUUID());
                                        if (hms.containsKey(name)) {
                                            List<TimeEntryValue> thisTimeEntryValue = hms.get(name);
                                            thisTimeEntryValue.addAll(timeEntryValue);
                                            hms.put(name, thisTimeEntryValue);
                                        } else {
                                            List<TimeEntryValue> thisTimeEntryValue = new ArrayList<>();
                                            thisTimeEntryValue.addAll(timeEntryValue);
                                            hms.put(name, thisTimeEntryValue);
                                        }
                                    }

                                }
                            }

                        }

                        break;
                    case Constants.KEY_DATA_DETAIL_LEVEL_INITIATIVE:

                        for (String key : timeEntryItems.keySet()) {
                            // get Task
                            Task task = new Task(artifacts.get(key));
                            if (!task.getProjectId().equals(project.getId())) {
                                continue;
                            }
                            String type = task.getWorkProductType();
                            if (!type.equals("HierarchicalRequirement")) {
                                continue;
                            }
                            // get name of initiative
                            if (!artifacts.get(task.getWorkProductUUID()).getJSONObject("Feature").isNullObject()) {
                                String featureUUID =
                                        artifacts.get(task.getWorkProductUUID()).getJSONObject("Feature")
                                                .getString("_refObjectUUID");
                                PortfolioFeature feature = new PortfolioFeature(artifacts.get(featureUUID));
                                if (feature.getParentUUID() != null & feature.getProjectID().equals(project.getId())) {
                                    PortfolioInitiative initiative =
                                            new PortfolioInitiative(artifacts.get(feature.getParentUUID()));
                                    if (!initiative.getProjectID().equals(project.getId())) {
                                        continue;
                                    }
                                    String name = initiative.getName();

                                    for (TimeEntryItem timeEntryItem : timeEntryItems.get(key)) {
                                        if (timeEntryValues.containsKey(timeEntryItem.getUUID())) {
                                            List<TimeEntryValue> timeEntryValue =
                                                    timeEntryValues.get(timeEntryItem.getUUID());
                                            if (hms.containsKey(name)) {
                                                List<TimeEntryValue> thisTimeEntryValue = hms.get(name);
                                                thisTimeEntryValue.addAll(timeEntryValue);
                                                hms.put(name, thisTimeEntryValue);
                                            } else {
                                                List<TimeEntryValue> thisTimeEntryValue = new ArrayList<>();
                                                thisTimeEntryValue.addAll(timeEntryValue);
                                                hms.put(name, thisTimeEntryValue);
                                            }
                                        }

                                    }
                                }

                            }

                        }

                        break;
                    case Constants.KEY_DATA_DETAIL_LEVEL_THEME:

                        for (String key : timeEntryItems.keySet()) {
                            // get Task
                            Task task = new Task(artifacts.get(key));
                            if (!task.getProjectId().equals(project.getId())) {
                                continue;
                            }
                            String type = task.getWorkProductType();
                            if (!type.equals("HierarchicalRequirement")) {
                                continue;
                            }
                            // get name of theme
                            if (!artifacts.get(task.getWorkProductUUID()).getJSONObject("Feature").isNullObject()) {
                                String featureUUID =
                                        artifacts.get(task.getWorkProductUUID()).getJSONObject("Feature")
                                                .getString("_refObjectUUID");
                                PortfolioFeature feature = new PortfolioFeature(artifacts.get(featureUUID));
                                if (feature.getParentUUID() != null & feature.getProjectID().equals(project.getId())) {
                                    PortfolioInitiative initiative =
                                            new PortfolioInitiative(artifacts.get(feature.getParentUUID()));
                                    if (initiative.getParentUUID() != null
                                            & initiative.getProjectID().equals(project.getId())) {
                                        PortfolioTheme theme =
                                                new PortfolioTheme(artifacts.get(initiative.getParentUUID()));
                                        if (!theme.getProjectID().equals(project.getId())) {
                                            continue;
                                        }
                                        String name = theme.getName();

                                        for (TimeEntryItem timeEntryItem : timeEntryItems.get(key)) {
                                            if (timeEntryValues.containsKey(timeEntryItem.getUUID())) {
                                                List<TimeEntryValue> timeEntryValue =
                                                        timeEntryValues.get(timeEntryItem.getUUID());
                                                if (hms.containsKey(name)) {
                                                    List<TimeEntryValue> thisTimeEntryValue = hms.get(name);
                                                    thisTimeEntryValue.addAll(timeEntryValue);
                                                    hms.put(name, thisTimeEntryValue);
                                                } else {
                                                    List<TimeEntryValue> thisTimeEntryValue = new ArrayList<>();
                                                    thisTimeEntryValue.addAll(timeEntryValue);
                                                    hms.put(name, thisTimeEntryValue);
                                                }
                                            }

                                        }
                                    }

                                }

                            }

                        }

                        break;

                }

                for (String key : hms.keySet()) {
                    HashMap<String, Integer> thisHms = getTimeSheetData(startDate, endDate, hms.get(key));
                    items.add(new RallyExternalWorkItem(project.getName(), key, thisHms, values, startDate, endDate));
                }
            }

        }

        return items;
    }

    private HashMap<String, Integer> getTimeSheetData(Date startDate, Date endDate, List<TimeEntryValue> timeEntryValues)
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

        final String project;

        final String iteration;

        String errorMessage = null;

        Date startDate;

        Date endDate;

        HashMap<String, Integer> effortList = new HashMap<>();

        public RallyExternalWorkItem(String project, String iteration, HashMap<String, Integer> hms, ValueSet values,
                Date startDate, Date endDate) {
            this.project = project;
            this.iteration = iteration;
            this.startDate = startDate;
            this.endDate = endDate;

            effortList.putAll(hms);
        }

        @Override
        public String getName() {
            return this.iteration + "(" + this.project + ")";
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

}