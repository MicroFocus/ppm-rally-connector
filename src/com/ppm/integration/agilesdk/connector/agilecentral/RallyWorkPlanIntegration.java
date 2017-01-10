package com.ppm.integration.agilesdk.connector.agilecentral;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.hp.ppm.integration.ValueSet;
import com.hp.ppm.integration.pm.IExternalTask;
import com.hp.ppm.integration.pm.IExternalWorkPlan;
import com.hp.ppm.integration.pm.WorkPlanIntegration;
import com.hp.ppm.integration.pm.WorkPlanIntegrationContext;
import com.hp.ppm.integration.ui.DynamicalDropdown;
import com.hp.ppm.integration.ui.Field;
import com.hp.ppm.integration.ui.LineBreaker;
import com.hp.ppm.integration.ui.PasswordText;
import com.hp.ppm.integration.ui.PlainText;
import com.ppm.integration.agilesdk.connector.agilecentral.model.Iteration;
import com.ppm.integration.agilesdk.connector.agilecentral.model.Project;
import com.ppm.integration.agilesdk.connector.agilecentral.model.Release;
import com.ppm.integration.agilesdk.connector.agilecentral.model.Subscription;
import com.ppm.integration.agilesdk.connector.agilecentral.model.Workspace;

public class RallyWorkPlanIntegration implements WorkPlanIntegration {
    String strTemp = "yyyy-MM-dd HH:mm:ss";

    @Override
    public List<Field> getMappingConfigurationFields(WorkPlanIntegrationContext context, ValueSet values) {
        return Arrays.asList(new Field[] {new PlainText(Constants.KEY_USERNAME, "USERNAME", "dan@acme.com", true),
                new PasswordText(Constants.KEY_PASSWORD, "PASSWORD", "Release!", true), new LineBreaker(),
                new DynamicalDropdown(Constants.KEY_SUBSCRIPTION, "SUBSCRIPTION", true) {

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
                }, new DynamicalDropdown(Constants.KEY_WORKSPACE, "WORKSPACE", true) {

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
                }, new DynamicalDropdown(Constants.KEY_PROJECT, "PROJECT", true) {

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
                }, new DynamicalDropdown(Constants.KEY_RELEASE, "RELEASE", false) {

                    @Override
                    public List<String> getDependencies() {
                        return Arrays.asList(new String[] {"", Constants.KEY_PROJECT});
                    }

                    @Override
                    public List<Option> getDynamicalOptions(ValueSet values) {

                        Config config = new Config();
                        config.setProxy(values.get(Constants.KEY_PROXY_HOST), values.get(Constants.KEY_PROXY_PORT));
                        config.setBasicAuthorization(values.get(Constants.KEY_USERNAME),
                                values.get(Constants.KEY_PASSWORD));
                        RallyClient rallyClient = new RallyClient(values.get(Constants.KEY_BASE_URL), config);

                        List<Option> options = new LinkedList<Option>();
                        for (Release r : rallyClient.getReleases(values.get(Constants.KEY_PROJECT))) {
                            options.add(new Option(r.getId(), r.getName()));
                        }
                        return options;
                    }
                },});
    }

    @Override
    public boolean linkTaskWithExternal(WorkPlanIntegrationContext context, ValueSet values) {
        return false;
    }

    @Override
    public IExternalWorkPlan getExternalWorkPlan(WorkPlanIntegrationContext context, ValueSet values) {

        Config config = new Config();
        config.setProxy(values.get(Constants.KEY_PROXY_HOST), values.get(Constants.KEY_PROXY_PORT));
        config.setBasicAuthorization(values.get(Constants.KEY_USERNAME), values.get(Constants.KEY_PASSWORD));
        final RallyClient rallyClient = new RallyClient(values.get(Constants.KEY_BASE_URL), config);

        final String projectId = values.get(Constants.KEY_PROJECT);
        final String releaseId = values.get(Constants.KEY_RELEASE);

        IExternalWorkPlan externalWorkPlan = new IExternalWorkPlan() {

            @Override
            public List<IExternalTask> getRootTasks() {
                List<Iteration> iterations = rallyClient.getIterationsByRelease(projectId, releaseId);
                List<IExternalTask> externalTasks = new ArrayList<IExternalTask>(iterations.size());
                for (Iteration iteration : iterations) {
                    externalTasks.add(iteration);
                }
                return externalTasks;
            }
        };
        return externalWorkPlan;
    }

    @Override
    public boolean unlinkTaskWithExternal(WorkPlanIntegrationContext context, ValueSet values) {
        return false;
    }

    @Override
    public String getCustomDetailPage() {
        return null;
    }

}